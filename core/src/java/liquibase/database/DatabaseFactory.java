package liquibase.database;

import liquibase.exception.JDBCException;
import liquibase.migrator.Migrator;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseFactory {

    private static DatabaseFactory instance = new DatabaseFactory();

    private static final Logger log = Logger.getLogger(Migrator.DEFAULT_LOG_NAME);


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
                new CacheDatabase(),
                new FirebirdDatabase(),
                new MaxDBDatabase()
        };
    }

    public Database findCorrectDatabaseImplementation(Connection connection) throws JDBCException {
        Database database = null;

        boolean foundImplementation = false;

        Database[] implementedDatabases = getImplementedDatabases();

        for (Database implementedDatabase : implementedDatabases) {
            database = implementedDatabase;
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

    public Database findCorrectDatabaseImplementation(DatabaseConnection connection) throws JDBCException {
        return findCorrectDatabaseImplementation(connection.getUnderlyingConnection());
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
