package liquibase.database;

import liquibase.Scope;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.PrioritizedService;

import java.sql.Driver;
import java.util.*;

public class ConnectionServiceFactory {
    private static ConnectionServiceFactory instance;

    private final List<DatabaseConnection> databaseConnections = new ArrayList<>();

    public static synchronized void reset() {
        instance = new ConnectionServiceFactory();
    }

    public static synchronized ConnectionServiceFactory getInstance() {
        if (instance == null) {
            instance = new ConnectionServiceFactory();
        }

        return instance;
    }

    public DatabaseConnection create(String url, Driver driverObject, Properties driverProperties)
               throws DatabaseException {
        DatabaseConnection databaseConnection = getDatabaseConnection(url);
        try {
            databaseConnection.open(url, driverObject, driverProperties);
        }
        catch (Exception sqle) {
            throw new DatabaseException(sqle);
        }
        return databaseConnection;
    }

    /**
     * @deprecated use {@link #getDatabaseConnection(String)}
     */
    public DatabaseConnection getDatabaseConnection() {
        return getDatabaseConnection(null);
    }

    public DatabaseConnection getDatabaseConnection(String url) {
        final SortedSet<DatabaseConnection> sortedConnections = new TreeSet<>(
                (o1, o2) -> -1 * Integer.compare(o1.getPriority(), o2.getPriority()));

        databaseConnections.stream().filter(c -> c.supports(url))
                .forEach(sortedConnections::add);

        try {
            DatabaseConnection exampleService = sortedConnections.iterator().next();
            Class<? extends DatabaseConnection> aClass = exampleService.getClass();
            DatabaseConnection connection;
            try {
                aClass.getConstructor();
                connection = aClass.getConstructor().newInstance();
            } catch (NoSuchMethodException e) {
                // must have been manually added to the registry and so already configured.
                connection = exampleService;
            }

            return connection;
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    private ConnectionServiceFactory() {
        List<DatabaseConnection> connections;
        try {
            connections = Scope.getCurrentScope().getServiceLocator().findInstances(DatabaseConnection.class);

            for (DatabaseConnection connection : connections) {
                register(connection.getClass().getConstructor().newInstance());
            }
        } catch (Exception e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    public void register(DatabaseConnection databaseConnection) {
        databaseConnections.add(databaseConnection);
        databaseConnections.sort(PrioritizedService.COMPARATOR);
    }
}
