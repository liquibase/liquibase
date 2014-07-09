package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.sdk.supplier.database.JdbcTestConnection;

public class PostgresqlTestConnection extends JdbcTestConnection {

    @Override
    protected String getUrl() {
        return "jdbc:postgresql://"+ getIpAddress() +"/"+getPrimaryCatalog();
    }

    @Override
    public Database getCorrectDatabase() {
        return new PostgresDatabase();
    }

    @Override
    public String describe() {
        return "Standard PostgreSQL connection";
    }

}
