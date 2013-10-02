package liquibase.sqlgenerator;

import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.structure.DatabaseObject;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.servicelocator.ServiceLocator;
import liquibase.sql.Sql;
import liquibase.statement.SqlStatement;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;

/**
 * SqlGeneratorFactory is a singleton registry of SqlGenerators.
 * Use the register(SqlGenerator) method to add custom SqlGenerators,
 * and the getBestGenerator() method to retrieve the SqlGenerator that should be used for a given SqlStatement.
 */
public class SqlGeneratorFactory {

    private static SqlGeneratorFactory instance;

    private List<SqlGenerator> generators = new ArrayList<SqlGenerator>();

    //caches for expensive reflection based calls that slow down Liquibase initialization: CORE-1207
    private final Map<Class<?>, Type[]> genericInterfacesCache = new HashMap<Class<?>, Type[]>();
    private final Map<Class<?>, Type> genericSuperClassCache = new HashMap<Class<?>, Type>();

    private SqlGeneratorFactory() {
        Class[] classes;
        try {
            classes = ServiceLocator.getInstance().findClasses(SqlGenerator.class);

            for (Class clazz : classes) {
                register((SqlGenerator) clazz.getConstructor().newInstance());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Return singleton SqlGeneratorFactory
     */
    public static SqlGeneratorFactory getInstance() {
        if (instance == null) {
            instance = new SqlGeneratorFactory();
        }
        return instance;
    }

    public static void reset() {
        instance = new SqlGeneratorFactory();
    }


    public void register(SqlGenerator generator) {
        generators.add(generator);
    }

    public void unregister(SqlGenerator generator) {
        generators.remove(generator);
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

    protected SortedSet<SqlGenerator> getGenerators(SqlStatement statement, Database database) {
        SortedSet<SqlGenerator> validGenerators = new TreeSet<SqlGenerator>(new SqlGeneratorComparator());

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
        return validGenerators;
    }

    private Type[] getGenericInterfaces(Class<?> clazz) {
        if(genericInterfacesCache.containsKey(clazz)) {
            return genericInterfacesCache.get(clazz);
        }

        Type[] genericInterfaces = clazz.getGenericInterfaces();
        genericInterfacesCache.put(clazz, genericInterfaces);
        return genericInterfaces;
    }

    private Type getGenericSuperclass(Class<?> clazz) {
        if(genericSuperClassCache.containsKey(clazz)) {
            return genericSuperClassCache.get(clazz);
        }

        Type genericSuperclass = clazz.getGenericSuperclass();
        genericSuperClassCache.put(clazz, genericSuperclass);
        return genericSuperclass;
    }

    private boolean isTypeEqual(Type aType, Class aClass) {
        if (aType instanceof Class) {
            return ((Class) aType).getName().equals(aClass.getName());
        }
        return aType.equals(aClass);
    }

    private void checkType(Type type, SqlStatement statement, SqlGenerator generator, Database database, SortedSet<SqlGenerator> validGenerators) {
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
        SortedSet<SqlGenerator> sqlGenerators = getGenerators(statement, database);
        if (sqlGenerators == null || sqlGenerators.size() == 0) {
            return null;
        }
        //noinspection unchecked
        return new SqlGeneratorChain(sqlGenerators);
    }

    public Sql[] generateSql(SqlStatement statement, Database database) {
        SqlGeneratorChain generatorChain = createGeneratorChain(statement, database);
        if (generatorChain == null) {
            throw new IllegalStateException("Cannot find generators for database " + database.getClass() + ", statement: " + statement);
        }
        return generatorChain.generateSql(statement, database);
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
        return getGenerators(statement, database).size() > 0;
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
        return createGeneratorChain(statement, database).warn(statement, database);
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects(SqlStatement statement, Database database) {
        Set<DatabaseObject> affectedObjects = new HashSet<DatabaseObject>();

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
