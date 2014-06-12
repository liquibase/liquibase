package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.PostgresDatabase;
import liquibase.sdk.supplier.database.AbstractTestConnection;
import liquibase.sdk.supplier.database.JdbcTestConnection;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

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
