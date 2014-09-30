package liquibase.sdk.supplier.database;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;
import liquibase.sdk.database.MockDatabase;

public class OfflineTestConnection extends AbstractTestConnection {
    private String url;

    @Override
    public String getConnectionUnavailableReason() {
        return null;
    }

    @Override
    public void init() throws Exception {

    }

    protected String getUrl() {
        return url;
    }
    public String getPrimaryCatalog() {
        return "offline";
    }

    public String getPrimarySchema() {
        return "offline";
    }

    @Override
    public Database getCorrectDatabase() {
        return new MockDatabase();
    }

    @Override
    public DatabaseConnection getConnection() {
        return new OfflineConnection(url);
    }

    @Override
    public String describe() {
        return "Offline connection "+getUrl();
    }

    @Override
    public Class<? extends DatabaseConnection> getConnectionClass() {
        return OfflineConnection.class;
    }

}
