package liquibase.sdk.supplier.database;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.sdk.exception.UnexpectedLiquibaseSdkException;
import liquibase.servicelocator.ServiceLocator;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ConnectionSupplier {

    private static Set<TestConnection> existingConnections = null;


    public Set<TestConnection> getConnections(Collection<Database> databases) throws Exception {
        if (existingConnections == null) {
            existingConnections = new HashSet<TestConnection>();
            for (Class testConnectionClasses : ServiceLocator.getInstance().findClasses(TestConnection.class)) {
                TestConnection testConnection = (TestConnection) testConnectionClasses.newInstance();
                existingConnections.add(testConnection);
            }
        }

        Set<TestConnection> returnConnections = new HashSet<TestConnection>();
        for (Database database : databases) {
            boolean foundConnection = false;
            for (TestConnection connection : existingConnections) {
                if (connection.supports(database)) {
                    returnConnections.add(connection);

                    Database clone  = database.getClass().newInstance();
                    connection.init(clone);
                    DatabaseConnection databaseConnection = connection.getConnection();
                    clone.setConnection(databaseConnection);

                    foundConnection = true;
                }
            }
            if (!foundConnection) {
                throw new UnexpectedLiquibaseSdkException("Found no TestConnection classes for " + database);
            }
        }
        return returnConnections;
    }
}
