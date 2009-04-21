package liquibase.database.sql.generator;

import liquibase.database.sql.SqlStatement;
import liquibase.database.Database;

import java.util.*;
import java.io.IOException;
import java.io.File;
import java.net.URL;
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
            classes = getClasses("liquibase.database.sql.generator");

            for (Class clazz : classes) {
                if (SqlGenerator.class.isAssignableFrom(clazz)) {
                    register((SqlGenerator) clazz.getConstructor().newInstance());
                }
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
            for (Type type : generator.getClass().getGenericInterfaces()) {
                if (type instanceof ParameterizedType && Arrays.asList(((ParameterizedType) type).getActualTypeArguments()).contains(statement.getClass())) {
                    if (generator.isValid(statement, database)) {
                        validGenerators.add(generator);
                    }
                }
            }
        }
        return validGenerators;
    }

    public SqlGenerator getBestGenerator(final SqlStatement statement, final Database database) {
        SortedSet<SqlGenerator> validGenerators = getAllGenerators(statement, database);

        if (validGenerators.size() == 0) {
            return new NotImplementedGenerator();
        } else {
            return validGenerators.first();
        }
    }

    private Class[] getClasses(String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<File>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile().replace("%20", " ")));
        }
        ArrayList<Class> classes = new ArrayList<Class>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes.toArray(new Class[classes.size()]);
    }

    private List<Class> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class> classes = new ArrayList<Class>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                Class<?> clazz = Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
                if (clazz.getName().indexOf("SqlGeneratorFactoryTest") < 0
                        && !clazz.isInterface()) {
                    classes.add(clazz);
                }


            }
        }
        return classes;
    }

}
