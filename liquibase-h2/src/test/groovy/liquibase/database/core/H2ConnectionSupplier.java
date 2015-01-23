package liquibase.database.core;

import liquibase.database.ConnectionSupplier;

import java.util.Map;

public class H2ConnectionSupplier extends ConnectionSupplier {

    @Override
    public String getDatabaseShortName() {
        return "h2";
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:h2:mem:liquibase";
    }

    @Override
    public String getPrimaryCatalog() {
        return "LIQUIBASE";
    }

    @Override
    public String getPrimarySchema() {
        return "PUBLIC";
    }
}
