package liquibase.database;

import liquibase.exception.JDBCException;
import liquibase.log.LogFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class DatabaseFactory {

    private static DatabaseFactory instance = new DatabaseFactory();

    private static final Logger log = LogFactory.getLogger();

    private List<Database> implementedDatabases = new ArrayList<Database>(Arrays.asList(
            (Database) new OracleDatabase(),
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
    ));

    private DatabaseFactory() {
    }

    public static DatabaseFactory getInstance() {
        return instance;
    }

    /**
     * Returns instances of all implemented database types.
     */
    public List<Database> getImplementedDatabases() {
        return implementedDatabases;
    }

    public void addDatabaseImplementation(Database database) {
        implementedDatabases.add(0, database);
    }

    public Database findCorrectDatabaseImplementation(Connection connection) throws JDBCException {
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
                log.warning("Unknown database: " + connection.getMetaData().getDatabaseProductName());
            } catch (SQLException e) {
                throw new JDBCException(e);
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
