package liquibase.database.statement.generator;

import liquibase.database.statement.SqlStatement;
import liquibase.database.statement.syntax.Sql;
import liquibase.database.Database;
import liquibase.exception.JDBCException;
import liquibase.PluginUtil;

import java.util.*;
import java.lang.reflect.Type;
import java.lang.reflect.ParameterizedType;

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
            classes = PluginUtil.getClasses("liquibase.database.statement.generator", SqlGenerator.class);

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


    /**
     * Internal method for retrieving all generators.  Mainly exists for unit testing.
     */
    protected List<SqlGenerator> getGenerators() {
        return generators;
    }

    public SortedSet<SqlGenerator> getAllGenerators(SqlStatement statement, Database database) {
        SortedSet<SqlGenerator> validGenerators = new TreeSet<SqlGenerator>(new Comparator<SqlGenerator>() {
            public int compare(SqlGenerator o1, SqlGenerator o2) {
                return -1 * new Integer(o1.getSpecializationLevel()).compareTo(o2.getSpecializationLevel());
            }
        });

        for (SqlGenerator generator : getGenerators()) {
            Class clazz = generator.getClass();
            while (clazz != null) {
                for (Type type : clazz.getGenericInterfaces()) {
                    if (type instanceof ParameterizedType
                            && Arrays.asList(((ParameterizedType) type).getActualTypeArguments()).contains(statement.getClass())) {
                        if (generator.isValidGenerator(statement, database)) {
                            validGenerators.add(generator);
                        }
                    } else if (type.equals(SqlGenerator.class)) {
                        if (generator.isValidGenerator(statement, database)) {
                            validGenerators.add(generator);
                        }
                    }
                }
                clazz = clazz.getSuperclass();
            }
        }
        return validGenerators;
    }

    public SqlGenerator getBestGenerator(SqlStatement statement, Database database) {
        SortedSet<SqlGenerator> validGenerators = getAllGenerators(statement, database);

        if (validGenerators.size() == 0) {
            return null;
        } else {
            return validGenerators.first();
        }
    }

    public Sql[] generateSql(SqlStatement statement, Database database) throws JDBCException {
        return getBestGenerator(statement, database).generateSql(statement, database);
    }

    public boolean statementSupported(SqlStatement statement, Database database) {
        return getBestGenerator(statement, database)  != null;
    }
}
