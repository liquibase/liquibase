package liquibase.database.core.config;

import liquibase.sdk.supplier.database.ConnectionConfiguration;

public class SybaseASAConfigStandard extends ConnectionConfiguration {
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
