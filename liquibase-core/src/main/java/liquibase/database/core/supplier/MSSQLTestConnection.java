package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.sdk.supplier.database.JdbcTestConnection;

public class MSSQLTestConnection extends JdbcTestConnection {
    @Override
    public boolean supports(Database database) {
        return database instanceof MSSQLDatabase;
    }

    @Override
    protected String getUrl() {
        return "jdbc:sqlserver://"+ getIpAddress() +":1433;databaseName="+getPrimaryCatalog();
    }
}
