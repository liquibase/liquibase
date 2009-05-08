package liquibase.sqlgenerator;

import liquibase.database.Database;
import liquibase.database.structure.DatabaseObject;
import liquibase.exception.JDBCException;
import liquibase.exception.ValidationErrors;
import liquibase.sql.Sql;
import liquibase.statement.SqlStatement;
import liquibase.util.PluginUtil;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * SqlGeneratorFactory is a singleton registry of SqlGenerators.
 * Use the register(SqlGenerator) method to add custom SqlGenerators,
 * and the getBestGenerator() method to retrieve the SqlGenerator that should be used for a given SqlStatement.
 */
public class SqlGeneratorFactory {

    private static SqlGeneratorFactory instance = new SqlGeneratorFactory();

    private List<SqlGenerator> generators = new ArrayList<SqlGenerator>();

    private SqlGeneratorFactory() {
        Class[] classes;
        try {
            classes = PluginUtil.getClasses("liquibase.sqlgenerator", SqlGenerator.class);

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
        SortedSet<SqlGenerator> validGenerators = new TreeSet<SqlGenerator>(new Comparator<SqlGenerator>() {
            public int compare(SqlGenerator o1, SqlGenerator o2) {
                return -1 * new Integer(o1.getPriority()).compareTo(o2.getPriority());
            }
        });

        for (SqlGenerator generator : getGenerators()) {
            Class clazz = generator.getClass();
            while (clazz != null) {
                for (Type type : clazz.getGenericInterfaces()) {
                    if (type instanceof ParameterizedType
                            && Arrays.asList(((ParameterizedType) type).getActualTypeArguments()).contains(statement.getClass())) {
                        //noinspection unchecked
                        if (generator.supports(statement, database)) {
                            validGenerators.add(generator);
                        }
                    } else if (type.equals(SqlGenerator.class)) {
                        //noinspection unchecked
                        if (generator.supports(statement, database)) {
                            validGenerators.add(generator);
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }
        }
        return validGenerators;
    }

    protected SqlGenerator getGenerator(SqlStatement statement, Database database) {
        SortedSet<SqlGenerator> validGenerators = getGenerators(statement, database);

        if (validGenerators.size() == 0) {
            return null;
        } else {
            return validGenerators.first();
        }
    }

    public Sql[] generateSql(SqlStatement statement, Database database) {
        SqlGenerator sqlGenerator = getGenerator(statement, database);
        if (sqlGenerator == null) {
            return null;
        }
        //noinspection unchecked
        return sqlGenerator.generateSql(statement, database);
    }

    public boolean supports(SqlStatement statement, Database database) {
        return getGenerator(statement, database) != null;
    }

    public ValidationErrors validate(SqlStatement statement, Database database) {
        //noinspection unchecked
        return getGenerator(statement, database).validate(statement, database);
    }

    public Set<DatabaseObject> getAffectedDatabaseObjects(SqlStatement statement, Database database) {
        Set<DatabaseObject> affectedObjects = new HashSet<DatabaseObject>();

        SqlGenerator sqlGenerator = SqlGeneratorFactory.getInstance().getGenerator(statement, database);
        if (sqlGenerator != null) {
            //noinspection unchecked
            for (Sql sql : sqlGenerator.generateSql(statement, database)) {
                affectedObjects.addAll(sql.getAffectedDatabaseObjects());
            }
        }

        return affectedObjects;

    }
}
