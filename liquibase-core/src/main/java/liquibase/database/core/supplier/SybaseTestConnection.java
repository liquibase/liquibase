package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.SybaseDatabase;
import liquibase.sdk.supplier.database.JdbcTestConnection;

public class SybaseTestConnection extends JdbcTestConnection {

    @Override
    public boolean supports(Database database) {
        return database instanceof SybaseDatabase;
    }

    @Override
    protected String getUrl() {
        return "jdbc:sybase:Tds:"+ getIpAddress()+":5000/liquibase";
    }
}
