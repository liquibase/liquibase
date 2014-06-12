package liquibase.sdk.supplier.database;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;

public interface TestConnection {
    boolean supports(Database database);

    void init(Database database);

    Database getDatabase();

    DatabaseConnection getConnection() throws Exception;

    String describe();

    Class<? extends DatabaseConnection> getConnectionClass();

    boolean connectionIsAvailable();
}
