package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.sdk.supplier.database.JdbcTestConnection;

public class PostgresqlTestConnection extends JdbcTestConnection {

    @Override
    public boolean supports(Database database) {
        return database instanceof PostgresDatabase;
    }

    @Override
    protected String getUrl() {
        return "jdbc:postgresql://"+ getIpAddress() +"/"+getPrimaryCatalog();
    }
}
