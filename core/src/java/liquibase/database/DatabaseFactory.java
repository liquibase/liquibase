package liquibase.database;

import liquibase.migrator.Migrator;
import liquibase.migrator.exception.JDBCException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseFactory {

    private static DatabaseFactory instance = new DatabaseFactory();

    private Logger log = Logger.getLogger(Migrator.DEFAULT_LOG_NAME);


    private DatabaseFactory() {
    }

    public static DatabaseFactory getInstance() {
        return instance;
    }

    /**
     * Returns instances of all implemented database types.
     */
    public Database[] getImplementedDatabases() {
        return new Database[]{
                new OracleDatabase(),
                new PostgresDatabase(),
                new MSSQLDatabase(),
                new MySQLDatabase(),
                new DerbyDatabase(),
                new HsqlDatabase(),
                new DB2Database(),
                new SybaseDatabase(),
                new H2Database(),
        };
    }

    public Database findCorrectDatabaseImplementation(Connection connection) throws JDBCException {
        Database database = null;

        boolean foundImplementation = false;

        Database[] implementedDatabases = getImplementedDatabases();

        for (int i = 0; i < implementedDatabases.length; i++) {
            database = implementedDatabases[i];
            if (database.isCorrectDatabaseImplementation(connection)) {
                foundImplementation = true;
                break;
            }
        }

        if (!foundImplementation) {
            try {
                log.warning("Unknown database: " + connection.getMetaData().getDatabaseProductName());
            } catch (SQLException e) {
                throw new JDBCException(e);
            }
            database = new UnsupportedDatabase();
        }

        database.setConnection(connection);
        return database;
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
