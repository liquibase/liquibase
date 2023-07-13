package liquibase.sqlgenerator;

import liquibase.Scope;
import liquibase.change.Change;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.servicelocator.PrioritizedService;
import liquibase.sql.Sql;
import liquibase.statement.SqlStatement;
import liquibase.structure.DatabaseObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static liquibase.sqlgenerator.SqlGenerator.EMPTY_SQL;

/**
 * SqlGeneratorFactory is a singleton registry of SqlGenerators.
 * Use the register(SqlGenerator) method to add custom SqlGenerators,
 * and the getBestGenerator() method to retrieve the SqlGenerator that should be used for a given SqlStatement.
 */
public class SqlGeneratorFactory {

    private static SqlGeneratorFactory instance;
    //caches for expensive reflection based calls that slow down Liquibase initialization: CORE-1207
    private final Map<Class<?>, Type[]> genericInterfacesCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, Type> genericSuperClassCache = new ConcurrentHashMap<>();
    private List<SqlGenerator> generators;
    private final Map<String, List<SqlGenerator>> generatorsByKey = new ConcurrentHashMap<>();
    public static final String GENERATED_SQL_ARRAY_SCOPE_KEY = "generatedSqlArray";

    private SqlGeneratorFactory() {
        try {
            generators = new ArrayList<>();
            for (SqlGenerator generator : Scope.getCurrentScope().getServiceLocator().findInstances(SqlGenerator.class)) {
                register(generator);
            }
            generators = new CopyOnWriteArrayList<>(generators);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return singleton SqlGeneratorFactory
     */
    public static synchronized SqlGeneratorFactory getInstance() {
        if (instance == null) {
            instance = new SqlGeneratorFactory();
        }
        return instance;
    }

    public static synchronized void reset() {
        instance = new SqlGeneratorFactory();
    }


    public void register(SqlGenerator generator) {
        if (this.generators.size() == 0) {
            //handle case in tests wher we clear out the generators
            this.generatorsByKey.clear();
        }
        generators.add(generator);
    }

    public void unregister(SqlGenerator generator) {
        generators.remove(generator);
        for (Iterator<Map.Entry<String, List<SqlGenerator>>> iterator = generatorsByKey.entrySet().iterator(); iterator.hasNext(); ) {
            List<SqlGenerator> specificGenerators = iterator.next().getValue();
            specificGenerators.remove(generator);
            if (specificGenerators.isEmpty()) {
                iterator.remove();
            }
        }
    }

    public void unregister(Class generatorClass) {
        SqlGenerator toRemove = null;
        for (SqlGenerator existingGenerator : generators) {
            if (existingGenerator.getClass().equals(generatorClass)) {
                toRemove = existingGenerator;
            }
        }
        unregister(toRemove);
    }


    protected Collection<SqlGenerator> getGenerators() {
        return generators;
    }

    public List<SqlGenerator> getGenerators(SqlStatement statement, Database database) {
        String databaseName = null;
        if (database == null) {
            databaseName = "NULL";
        } else {
            databaseName = database.getShortName();
        }

        int version;
        if (database == null) {
            version = 0;
        } else {
            try {
                version = database.getDatabaseMajorVersion();
            } catch (Exception e) {
                version = 0;
            }
        }

        String key = statement.getClass().getName()+":"+ databaseName+":"+ version;

        List<SqlGenerator> potential = generatorsByKey.get(key);
        if (potential != null && !potential.isEmpty()) return potential;

        return generatorsByKey.compute(key, (k, existing) -> maybeSelectBetter(statement, database, existing));
    }

    private List<SqlGenerator> maybeSelectBetter(SqlStatement statement, Database database, List<SqlGenerator> existing) {
        if (existing != null && !existing.isEmpty()) return existing;

        Set<SqlGenerator> validGenerators = new HashSet<>();

        for (SqlGenerator generator : getGenerators()) {
            Class clazz = generator.getClass();
            Type classType = null;
            while (clazz != null) {
                if (classType instanceof ParameterizedType) {
                    checkType(classType, statement, generator, database, validGenerators);
                }

                for (Type type : getGenericInterfaces(clazz)) {
                    if (type instanceof ParameterizedType) {
                        checkType(type, statement, generator, database, validGenerators);
                    } else if (isTypeEqual(type, SqlGenerator.class)) {
                        //noinspection unchecked
                        if (generator.supports(statement, database)) {
                            validGenerators.add(generator);
                        }
                    }
                }
                classType = getGenericSuperclass(clazz);
                clazz = clazz.getSuperclass();
            }
        }
        return validGenerators.stream().sorted(PrioritizedService.COMPARATOR).collect(Collectors.toCollection(CopyOnWriteArrayList::new));
    }

    private Type[] getGenericInterfaces(Class<?> clazz) {
        return genericInterfacesCache.computeIfAbsent(clazz, Class::getGenericInterfaces);
    }

    private Type getGenericSuperclass(Class<?> clazz) {
        return genericSuperClassCache.computeIfAbsent(clazz, Class::getGenericSuperclass);
    }

    private boolean isTypeEqual(Type aType, Class aClass) {
        if (aType instanceof Class) {
            return ((Class<?>) aType).isAssignableFrom(aClass);
        }
        return aType.equals(aClass);
    }

    private void checkType(Type type, SqlStatement statement, SqlGenerator generator, Database database, Collection<SqlGenerator> validGenerators) {
        for (Type typeClass : ((ParameterizedType) type).getActualTypeArguments()) {
            if (typeClass instanceof TypeVariable) {
                typeClass = ((TypeVariable) typeClass).getBounds()[0];
            }

            if (isTypeEqual(typeClass, SqlStatement.class)) {
                return;
            }

            if (((Class) typeClass).isAssignableFrom(statement.getClass())) {
                if (generator.supports(statement, database)) {
                    validGenerators.add(generator);
                }
            }
        }
    }

    private SqlGeneratorChain createGeneratorChain(SqlStatement statement, Database database) {
        Collection<SqlGenerator> sqlGenerators = getGenerators(statement, database);
        if ((sqlGenerators == null) || sqlGenerators.isEmpty()) {
            return null;
        }
        //noinspection unchecked
        return new SqlGeneratorChain(sqlGenerators);
    }

    public Sql[] generateSql(Change change, Database database) {
        SqlStatement[] sqlStatements = change.generateStatements(database);
        if (sqlStatements == null) {
            return EMPTY_SQL;
        } else {
            return generateSql(sqlStatements, database);
        }
    }

    public Sql[] generateSql(SqlStatement[] statements, Database database) {
        List<Sql> returnList = new ArrayList<>();
        SqlGeneratorFactory factory = SqlGeneratorFactory.getInstance();
        for (SqlStatement statement : statements) {
            Sql[] sqlArray = factory.generateSql(statement, database);
            if ((sqlArray != null) && (sqlArray.length > 0)) {
              List<Sql> sqlList = Arrays.asList(sqlArray);
              returnList.addAll(sqlList);
            }
        }
        return putSqlArrayInScope(returnList.toArray(EMPTY_SQL));
    }

    public Sql[] generateSql(SqlStatement statement, Database database) {
        SqlGeneratorChain generatorChain = createGeneratorChain(statement, database);
        if (generatorChain == null) {
            throw new IllegalStateException("Cannot find generators for database " + database.getClass() + ", statement: " + statement
                    + ". Either Liquibase or the database platform does not support the type of statement being generated. Please check your database documentation for more information.");
        }
        return putSqlArrayInScope(generatorChain.generateSql(statement, database));
    }

    /**
     * Save the generated SQL in the scope.
     * @param sqls the generated SQL
     * @return the generated SQL
     */
    private Sql[] putSqlArrayInScope(Sql[] sqls) {
        AtomicReference<Sql[]> sqlsReference = Scope.getCurrentScope().get(GENERATED_SQL_ARRAY_SCOPE_KEY, AtomicReference.class);
        if (sqlsReference != null) {
            sqlsReference.set(sqls);
        }
        return sqls;
    }

    /**
     * Return true if the SqlStatement class queries the database in any way to determine Statements to execute.
     * If the statement queries the database, it cannot be used in updateSql type operations
     */
    public boolean generateStatementsVolatile(SqlStatement statement, Database database) {
        for (SqlGenerator generator : getGenerators(statement, database)) {
            if (generator.generateStatementsIsVolatile(database)) {
                return true;
            }
        }
        return false;
    }

    public boolean generateRollbackStatementsVolatile(SqlStatement statement, Database database) {
        for (SqlGenerator generator : getGenerators(statement, database)) {
            if (generator.generateRollbackStatementsIsVolatile(database)) {
                return true;
            }
        }
        return false;
    }

    public boolean supports(SqlStatement statement, Database database) {
        return !getGenerators(statement, database).isEmpty();
    }

    public ValidationErrors validate(SqlStatement statement, Database database) {
        //noinspection unchecked
        SqlGeneratorChain generatorChain = createGeneratorChain(statement, database);
        if (generatorChain == null) {
            throw new UnexpectedLiquibaseException("Unable to create generator chain for "+statement.getClass().getName()+" on "+database.getShortName());
        }
        return generatorChain.validate(statement, database);
    }

    public Warnings warn(SqlStatement statement, Database database) {
        //noinspection unchecked
        final SqlGeneratorChain generatorChain = createGeneratorChain(statement, database);
        if (generatorChain != null) {
            return generatorChain.warn(statement, database);
        }
        return
           new Warnings().addWarning("No generator chain created for SQL Statement associated with database " + database.getShortName());
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects(SqlStatement statement, Database database) {
        Set<DatabaseObject> affectedObjects = new HashSet<>();

        SqlGeneratorChain sqlGeneratorChain = createGeneratorChain(statement, database);
        if (sqlGeneratorChain != null) {
            //noinspection unchecked
            Sql[] sqls = sqlGeneratorChain.generateSql(statement, database);
            if (sqls != null) {
                for (Sql sql : sqls) {
                    affectedObjects.addAll(sql.getAffectedDatabaseObjects());
                }
            }
        }
        return affectedObjects;
    }
}
