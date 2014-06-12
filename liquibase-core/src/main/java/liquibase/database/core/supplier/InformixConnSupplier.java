package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.sdk.supplier.database.AbstractTestConnection;
import liquibase.sdk.supplier.database.JdbcTestConnection;

import java.util.Map;

public class InformixConnSupplier extends JdbcTestConnection {
    @Override
    public boolean supports(Database database) {
        return database instanceof InformixDatabase;
    }

    @Override
    protected String getUrl() {
        return "jdbc:informix-sqli://" + getIpAddress() + ":9088/liquibase:informixserver=ol_ids_1150_1";
    }
}
