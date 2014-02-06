package liquibase.database.core.supplier;

import liquibase.sdk.supplier.database.ConnectionSupplier;

public class HsqlConnSupplier extends ConnectionSupplier {
    @Override
    public String getDatabaseShortName() {
        return "hsqldb";
    }

    @Override
    public String getConfigurationName() {
        return NAME_STANDARD;
    }

    @Override
    public String getUrl() {
        return "jdbc:hsqldb:mem:liquibase";
    }
}
