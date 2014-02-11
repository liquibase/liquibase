package liquibase.database.core.supplier;

import liquibase.sdk.supplier.database.ConnectionSupplier;

public class DerbyConnSupplier extends ConnectionSupplier {
    @Override
    public String getDatabaseShortName() {
        return "derby";
    }

    @Override
    public String getAdminUsername() {
        return null;
    }

    @Override
    public String getConfigurationName() {
        return CONFIG_NAME_STANDARD;
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:derby:liquibase;create=true";
    }
}
