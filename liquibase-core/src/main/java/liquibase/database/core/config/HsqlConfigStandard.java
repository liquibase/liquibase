package liquibase.database.core.config;

import liquibase.sdk.supplier.database.ConnectionConfiguration;

public class HsqlConfigStandard extends ConnectionConfiguration {
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
