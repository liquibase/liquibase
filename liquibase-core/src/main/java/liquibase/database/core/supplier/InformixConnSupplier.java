package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.InformixDatabase;
import liquibase.sdk.supplier.database.JdbcTestConnection;

public class InformixConnSupplier extends JdbcTestConnection {

    @Override
    protected String getUrl() {
        return "jdbc:informix-sqli://" + getIpAddress() + ":9088/liquibase:informixserver=ol_ids_1150_1";
    }

    @Override
    public Database getCorrectDatabase() {
        return new InformixDatabase();
    }


    @Override
    public String describe() {
        return "Standard Informix connection";
    }

}
