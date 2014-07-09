package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.sdk.supplier.database.JdbcTestConnection;

public class H2TestConnection extends JdbcTestConnection {


    @Override
    protected String getUrl() {
        return "jdbc:h2:mem:lbcat;init=create schema if not exists lbschema";
    }

    @Override
    public Database getCorrectDatabase() {
        return new H2Database();
    }


    @Override
    public String describe() {
        return "Standard H2 connection";
    }

}
