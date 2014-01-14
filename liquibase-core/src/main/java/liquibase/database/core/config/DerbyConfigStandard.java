package liquibase.database.core.config;

import liquibase.sdk.supplier.database.ConnectionConfiguration;

public class DerbyConfigStandard extends ConnectionConfiguration {
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
