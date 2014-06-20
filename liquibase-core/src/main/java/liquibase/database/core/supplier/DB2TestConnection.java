package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.sdk.supplier.database.JdbcTestConnection;

public class DB2TestConnection extends JdbcTestConnection {

    @Override
    public boolean supports(Database database) {
        return database instanceof DB2Database;
    }

    @Override
    protected String getUrl() {
        return "jdbc:db2://"+ getIpAddress() +":50000/"+getPrimaryCatalog().toLowerCase();
    }
}
