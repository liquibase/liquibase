package liquibase.database.core.supplier;

import liquibase.sdk.supplier.database.ConnectionSupplier;

public class SybaseASAConnSupplier extends ConnectionSupplier {
    @Override
    public String getDatabaseShortName() {
        return "asany";
    }

    @Override
    public String getAdminUsername() {
        return "sa";
    }

    @Override
    public String getConfigurationName() {
        return CONFIG_NAME_STANDARD;
    }

    @Override
    public String getUrl() {
        return "jdbc:sybase:Tds:"+ getIpAddress() +":9810/servicename=prior";
    }
}
