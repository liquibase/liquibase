package liquibase.database.core.supplier;

import liquibase.sdk.supplier.database.ConnectionSupplier;

public class H2ConnSupplier extends ConnectionSupplier {

    @Override
    public String getConfigurationName() {
        return CONFIG_NAME_STANDARD;
    }

    @Override
    public String getAdminUsername() {
        return null;
    }

    @Override
    public String getDatabaseShortName() {
        return "h2";
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:h2:mem:liquibase";
    }
}
