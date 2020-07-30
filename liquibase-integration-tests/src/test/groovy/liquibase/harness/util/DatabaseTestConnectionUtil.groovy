package liquibase.harness.util

import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.exception.DatabaseException;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.Logger;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.test.DatabaseTestContext


class DatabaseTestConnectionUtil {
    private static Logger logger = Scope.getCurrentScope().getLog(getClass());

    static Database initializeDatabase(String dbName) {
        Database database = DatabaseFactory.getInstance().getDatabase(dbName);
        Properties properties = new Properties();
        File propertiesFile = new File("src/test/resources/harness/harness.db.properties")
        propertiesFile.withInputStream { properties.load(it) }
        String url = properties.getProperty(database.getShortName() + ".url");
        String username = properties.getProperty(database.getShortName() + ".username");
        String password = properties.getProperty(database.getShortName() + ".password");
        try {
            database = openConnection(url, username, password);
        }
        catch (Exception e) {
            logger.severe("Unable to initialize database connection", e);
            return null;
        }
        init(database);
        return database;
    }

    private static Database openConnection(String url, String username, String password) throws Exception {
        DatabaseConnection connection = DatabaseTestContext.getInstance().getConnection(url, username, password);
        return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);

    }

    private static void init(Database database) throws DatabaseException {

//        if (database.supportsTablespaces()) {
//            database.setLiquibaseTablespaceName(DatabaseTestContext.ALT_TABLESPACE);
//        }
        if (!database.getConnection().getAutoCommit()) {
            database.rollback();
        }

        SnapshotGeneratorFactory.resetAll();
        LockServiceFactory.getInstance().resetAll();
        LockServiceFactory.getInstance().getLockService(database).init();
        ChangeLogHistoryServiceFactory.getInstance().resetAll();
    }

}