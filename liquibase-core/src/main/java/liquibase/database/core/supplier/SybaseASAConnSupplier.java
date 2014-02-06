package liquibase.database.core.supplier;

import liquibase.sdk.supplier.database.ConnectionSupplier;

public class SybaseASAConnSupplier extends ConnectionSupplier {
    @Override
    public String getDatabaseShortName() {
        return "asany";
    }

    @Override
    public String getConfigurationName() {
        return NAME_STANDARD;
    }

    @Override
    public String getUrl() {
        return "jdbc:sybase:Tds:"+ getHostname() +":9810/servicename=prior";
    }
}
