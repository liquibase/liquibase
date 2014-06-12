package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.SybaseASADatabase;
import liquibase.sdk.supplier.database.AbstractTestConnection;
import liquibase.sdk.supplier.database.JdbcTestConnection;

import java.util.Map;

public class SybaseASATestConnection extends JdbcTestConnection {
    @Override
    public boolean supports(Database database) {
        return database instanceof SybaseASADatabase;
    }

    @Override
    protected String getUrl() {
        return "jdbc:sybase:Tds:"+ getIpAddress() +":9810/servicename=prior";
    }
}
