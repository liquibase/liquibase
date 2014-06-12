package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.MySQLDatabase;
import liquibase.sdk.supplier.database.AbstractTestConnection;
import liquibase.sdk.supplier.database.JdbcTestConnection;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class MySQLTestConnection extends JdbcTestConnection {

    @Override
    public boolean supports(Database database) {
        return database instanceof MySQLDatabase;
    }

    @Override
    protected String getUrl() {
        return "jdbc:mysql://"+ getIpAddress() +"/"+getPrimaryCatalog();
    }
}
