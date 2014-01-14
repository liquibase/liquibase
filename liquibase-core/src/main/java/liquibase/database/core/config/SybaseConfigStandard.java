package liquibase.database.core.config;

import liquibase.sdk.supplier.database.ConnectionConfiguration;

public class SybaseConfigStandard extends ConnectionConfiguration {
    @Override
    public String getDatabaseShortName() {
        return "sybase";
    }

    @Override
    public String getConfigurationName() {
        return NAME_STANDARD;
    }

    @Override
    public String getUrl() {
        return "jdbc:sybase:Tds:"+ getHostname()+":5000/liquibase";
    }
}
