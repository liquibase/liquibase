package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.SybaseASADatabase;
import liquibase.sdk.supplier.database.JdbcTestConnection;

public class SybaseASATestConnection extends JdbcTestConnection {

    @Override
    protected String getUrl() {
        return "jdbc:sybase:Tds:"+ getIpAddress() +":9810/servicename=prior";
    }

    @Override
    public Database getCorrectDatabase() {
        return new SybaseASADatabase();
    }

    @Override
    public String describe() {
        return "Standard Sybase ASA connection";
    }

}
