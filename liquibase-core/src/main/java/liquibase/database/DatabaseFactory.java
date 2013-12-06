package liquibase.database;

import liquibase.database.core.UnsupportedDatabase;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.servicelocator.ServiceLocator;
import liquibase.util.StringUtils;

import java.util.*;

public class DatabaseFactory {
    private static DatabaseFactory instance;
    private Map<String, SortedSet<Database>> implementedDatabases = new HashMap<String, SortedSet<Database>>();
    private Map<String, SortedSet<Database>> internalDatabases = new HashMap<String, SortedSet<Database>>();

    private DatabaseFactory() {
        try {
            Class[] classes = ServiceLocator.getInstance().findClasses(Database.class);

            //noinspection unchecked
            for (Class<? extends Database> clazz : classes) {
                register(clazz.getConstructor().newInstance());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static DatabaseFactory getInstance() {
        if (instance == null) {
            instance = new DatabaseFactory();
        }
        return instance;
    }

    public static void reset() {
        instance = new DatabaseFactory();
    }

    /**
     * Set singleton instance. Primarily used in testing
     */
    public static void setInstance(DatabaseFactory databaseFactory) {
        instance = databaseFactory;
    }

    /**
     * Returns instances of all implemented database types.
     */
    public List<Database> getImplementedDatabases() {
        List<Database> returnList = new ArrayList<Database>();
        for (SortedSet<Database> set : implementedDatabases.values()) {
            returnList.add(set.iterator().next());
        }
        return returnList;
    }

    /**
     * Returns instances of all "internal" database types.
     */
    public List<Database> getInternalDatabases() {
        List<Database> returnList = new ArrayList<Database>();
        for (SortedSet<Database> set : internalDatabases.values()) {
            returnList.add(set.iterator().next());
        }
        return returnList;
    }

    public void register(Database database) {
        Map<String, SortedSet<Database>> map = null;
        if (database instanceof InternalDatabase) {
            map = internalDatabases;
        } else {
            map = implementedDatabases;

        }

        if (!map.containsKey(database.getShortName())) {
            map.put(database.getShortName(), new TreeSet<Database>(new TreeSet<Database>(new DatabaseComparator())));
        }
        map.get(database.getShortName()).add(database);
    }

    public Database findCorrectDatabaseImplementation(DatabaseConnection connection) throws DatabaseException {

        SortedSet<Database> foundDatabases = new TreeSet<Database>(new DatabaseComparator());

        for (Database implementedDatabase : getImplementedDatabases()) {
            if (implementedDatabase.isCorrectDatabaseImplementation(connection)) {
                foundDatabases.add(implementedDatabase);
            }
        }

        if (foundDatabases.size() == 0) {
            LogFactory.getLogger().warning("Unknown database: " + connection.getDatabaseProductName());
            UnsupportedDatabase unsupportedDB = new UnsupportedDatabase();
            unsupportedDB.setConnection(connection);
            return unsupportedDB;
        }

        Database returnDatabase;
        try {
            returnDatabase = foundDatabases.iterator().next().getClass().newInstance();
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }

        returnDatabase.setConnection(connection);
        return returnDatabase;
    }

    public String findDefaultDriver(String url) {
        for (Database database : this.getImplementedDatabases()) {
            String defaultDriver = database.getDefaultDriver(url);
            if (defaultDriver != null) {
                return defaultDriver;
            }
        }

        return null;
    }

    /**
     * Removes all registered databases, even built in ones.  Useful for forcing a particular database implementation
     */
    public void clearRegistry() {
        implementedDatabases.clear();
    }

    private static class DatabaseComparator implements Comparator<Database> {
        @Override
        public int compare(Database o1, Database o2) {
            return -1 * new Integer(o1.getPriority()).compareTo(o2.getPriority());
        }
    }
}
