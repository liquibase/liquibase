package liquibase.database.core.supplier;

import liquibase.sdk.supplier.database.ConnectionSupplier;

public class DerbyConnSupplier extends ConnectionSupplier {
    @Override
    public String getDatabaseShortName() {
        return "derby";
    }

    @Override
    public String getConfigurationName() {
        return NAME_STANDARD;
    }

    @Override
    public String getUrl() {
        return "jdbc:derby:liquibase;create=true";
    }
}
