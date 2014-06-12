package liquibase.sdk.supplier.database;

import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.OfflineConnection;

public class OfflineTestConnection extends AbstractTestConnection {
    private String url;

    @Override
    public boolean supports(Database database) {
        return true;
    }

    @Override
    public void init(Database database) {
        super.init(database);
        this.url = "offline:"+database.getShortName();
    }

    @Override
    public boolean connectionIsAvailable() {
        return true;
    }

    protected String getUrl() {
        return url;
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
