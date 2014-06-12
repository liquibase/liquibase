package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.FirebirdDatabase;
import liquibase.sdk.supplier.database.AbstractTestConnection;
import liquibase.sdk.supplier.database.JdbcTestConnection;

import java.util.Map;

public class FirebirdTestConnection extends JdbcTestConnection {

    @Override
    public boolean supports(Database database) {
        return database instanceof FirebirdDatabase;
    }

    @Override
    protected String getUrl() {
        return "jdbc:firebirdsql:"+ getIpAddress() +"/3050:c:\\firebird\\liquibase.fdb";
    }
}
