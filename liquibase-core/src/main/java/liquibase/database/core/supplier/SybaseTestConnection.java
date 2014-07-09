package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.SybaseDatabase;
import liquibase.sdk.supplier.database.JdbcTestConnection;

public class SybaseTestConnection extends JdbcTestConnection {

    @Override
    protected String getUrl() {
        return "jdbc:sybase:Tds:"+ getIpAddress()+":5000/liquibase";
    }

    @Override
    public Database getCorrectDatabase() {
        return new SybaseDatabase();
    }

    @Override
    public String describe() {
        return "Standard Syabase connection";
    }

}
