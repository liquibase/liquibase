package liquibase.database;

import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.servicelocator.ServiceLocator;
import liquibase.database.core.UnsupportedDatabase;

import java.util.ArrayList;
import java.util.List;
import liquibase.logging.Logger;

public class DatabaseFactory {
    private static DatabaseFactory instance;
    private List<Database> implementedDatabases = new ArrayList<Database>();

    protected DatabaseFactory() {
        try {
            Class[] classes = ServiceLocator.getInstance().findClasses(Database.class);

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

    /**
     * Returns instances of all implemented database types.
     */
    public List<Database> getImplementedDatabases() {
        return implementedDatabases;
    }

    public void register(Database database) {
        implementedDatabases.add(0, database);
    }

    public Database findCorrectDatabaseImplementation(DatabaseConnection connection) throws DatabaseException {
        Database database = null;

        boolean foundImplementation = false;

        for (Database implementedDatabase : getImplementedDatabases()) {
            database = implementedDatabase;
            if (database.isCorrectDatabaseImplementation(connection)) {
                foundImplementation = true;
                break;
            }
        }

        if (!foundImplementation) {
            LogFactory.getLogger().warning("Unknown database: " + connection.getDatabaseProductName());
            database = new UnsupportedDatabase();
        }

        Database returnDatabase;
        try {
            returnDatabase = database.getClass().newInstance();
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
}
