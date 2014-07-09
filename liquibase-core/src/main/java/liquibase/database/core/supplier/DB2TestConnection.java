package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.DB2Database;
import liquibase.sdk.supplier.database.JdbcTestConnection;

public class DB2TestConnection extends JdbcTestConnection {

    @Override
    protected String getUrl() {
        return "jdbc:db2://"+ getIpAddress() +":50000/"+getPrimaryCatalog().toLowerCase();
    }

    @Override
    public Database getCorrectDatabase() {
        return new DB2Database();
    }

    @Override
    public String describe() {
        return "Standard DB2 connection";
    }
}
