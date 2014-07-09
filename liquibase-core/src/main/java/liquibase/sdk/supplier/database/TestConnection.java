package liquibase.sdk.supplier.database;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;

import java.util.List;

/**
 * Interface for classes that are able to create connections for test.
 */
public interface TestConnection {

    Database getCorrectDatabase();

    public void init() throws Exception;

    DatabaseConnection getConnection();

    String describe();

    Class<? extends DatabaseConnection> getConnectionClass();

    boolean connectionIsAvailable();

    List<String[]> getTestCatalogsAndSchemas();

    Database getConnectedDatabase();
}
