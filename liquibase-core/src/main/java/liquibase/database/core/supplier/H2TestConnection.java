package liquibase.database.core.supplier;

import liquibase.database.Database;
import liquibase.database.core.H2Database;
import liquibase.sdk.supplier.database.JdbcTestConnection;

public class H2TestConnection extends JdbcTestConnection {


    @Override
    public boolean supports(Database database) {
        return database instanceof H2Database;
    }

    @Override
    protected String getUrl() {
        return "jdbc:h2:mem:liquibase";
    }
}
