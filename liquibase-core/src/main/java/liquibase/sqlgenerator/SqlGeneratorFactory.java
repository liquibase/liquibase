package liquibase.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.structure.DatabaseObject;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.servicelocator.ServiceLocator;
import liquibase.sql.Sql;
import liquibase.sqlgenerator.core.AbstractSqlGenerator;
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

                for (Type type : clazz.getGenericInterfaces()) {
                    if (type instanceof ParameterizedType) {
                        checkType(type, statement, generator, database, validGenerators);
                    } else if (isTypeEqual( type, SqlGenerator.class)) {
                        //noinspection unchecked
                        if (generator.supports(statement, database)) {
                            validGenerators.add(generator);
                        }
                    }
                }
                classType = clazz.getGenericSuperclass();
                clazz = clazz.getSuperclass();
            }
        }
        return validGenerators;
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
        
            if (isTypeEqual( typeClass, SqlStatement.class)) {
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

    public boolean requiresCurrentDatabaseMetadata(SqlStatement statement, Database database) {
        for (SqlGenerator generator : getGenerators(statement, database)) {
            if (generator.requiresUpdatedDatabaseMetadata(database)) {
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
        return createGeneratorChain(statement, database).validate(statement, database);
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
