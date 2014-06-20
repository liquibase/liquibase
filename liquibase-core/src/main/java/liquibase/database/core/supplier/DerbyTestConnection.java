package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.DerbyDatabase;
import liquibase.sdk.supplier.database.JdbcTestConnection;

public class DerbyTestConnection extends JdbcTestConnection {

    @Override
    public boolean supports(Database database) {
        return database instanceof DerbyDatabase;
    }

    @Override
    protected String getUrl() {
        return "jdbc:derby:memory:liquibase;create=true";
    }
}
