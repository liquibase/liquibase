package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.HsqlDatabase;
import liquibase.sdk.supplier.database.JdbcTestConnection;

public class HsqlTestConnection extends JdbcTestConnection {

    @Override
    public boolean supports(Database database) {
        return database instanceof HsqlDatabase;
    }

    @Override
    protected String getUrl() {
        return "jdbc:hsqldb:mem:liquibase";
    }
}
