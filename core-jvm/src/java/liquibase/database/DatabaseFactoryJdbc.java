package liquibase.database;

import liquibase.util.plugin.ClassPathScanner;
import liquibase.exception.DatabaseException;
import liquibase.database.core.UnsupportedDatabase;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseFactoryJdbc extends DatabaseFactory {

    public DatabaseFactoryJdbc() {
        
    }

    @Override
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
            try {
                log.warning("Unknown database: " + ((JdbcConnection) connection).getUnderlyingConnection().getMetaData().getDatabaseProductName());
            } catch (SQLException e) {
                throw new DatabaseException(e);
            }
            database = new UnsupportedDatabase();
        }

        Database returnDatabase;
        try {
            returnDatabase = database.getClass().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        returnDatabase.setConnection(connection);
        return returnDatabase;
    }


}
