package liquibase.harness.util

import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.exception.DatabaseException
import liquibase.harness.config.TestInput;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.Logger;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.test.DatabaseTestContext


class DatabaseTestConnectionUtil {
    private static Logger logger = Scope.getCurrentScope().getLog(getClass());

    static Database initializeDatabase(TestInput testInput) {
        try {
            Database database = openConnection(testInput.getUrl(), testInput.getUsername(), testInput.getPassword());
             return init(database);
        }
        catch (Exception e) {
            logger.severe("Unable to initialize database connection", e);
            return null;
        }
    }

    private static Database openConnection(String url, String username, String password) throws Exception {
        DatabaseConnection connection = DatabaseTestContext.getInstance().getConnection(url, username, password);
        return DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);

    }

    private static Database init(Database database) throws DatabaseException {
        if (!database.getConnection().getAutoCommit()) {
            database.rollback();
        }

        SnapshotGeneratorFactory.resetAll();
        LockServiceFactory.getInstance().resetAll();
        LockServiceFactory.getInstance().getLockService(database).init();
        ChangeLogHistoryServiceFactory.getInstance().resetAll();
        return database
    }

}