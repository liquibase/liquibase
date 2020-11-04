package liquibase.database;

import liquibase.Scope;
import liquibase.exception.DatabaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.servicelocator.PrioritizedService;
import liquibase.servicelocator.ServiceLocator;

import java.sql.Driver;
import java.util.*;

public class ConnectionServiceFactory {
    private static ConnectionServiceFactory instance;

    private List<DatabaseConnection> databaseConnections = new ArrayList<>();

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
        DatabaseConnection databaseConnection = getDatabaseConnection();
        try {
            databaseConnection.open(url, driverObject, driverProperties);
        }
        catch (Exception sqle) {
            throw new DatabaseException(sqle);
        }
        return databaseConnection;
    }

    public DatabaseConnection getDatabaseConnection() {
        SortedSet<DatabaseConnection> sortedConnections = new TreeSet<>(new Comparator<DatabaseConnection>() {
            @Override
            public int compare(DatabaseConnection o1, DatabaseConnection o2) {
                return -1 * Integer.valueOf(o1.getPriority()).compareTo(o2.getPriority());
            }
        });

        sortedConnections.addAll(databaseConnections);
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
        Collections.sort(databaseConnections, PrioritizedService.COMPARATOR);
    }
}
