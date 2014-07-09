package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.MSSQLDatabase;
import liquibase.sdk.supplier.database.JdbcTestConnection;

public class MSSQLTestConnection extends JdbcTestConnection {

    @Override
    protected String getUrl() {
        return "jdbc:sqlserver://"+ getIpAddress() +":1433;databaseName="+getPrimaryCatalog();
    }

    @Override
    public Database getCorrectDatabase() {
        return new MSSQLDatabase();
    }

    @Override
    public String describe() {
        return "Standard MS SqlServer connection";
    }

}
