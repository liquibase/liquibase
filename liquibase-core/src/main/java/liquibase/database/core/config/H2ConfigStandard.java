package liquibase.database.core.config;

import liquibase.sdk.supplier.database.ConnectionConfiguration;

public class H2ConfigStandard extends ConnectionConfiguration {

    @Override
    public String getConfigurationName() {
        return NAME_STANDARD;
    }

    @Override
    public String getDatabaseShortName() {
        return "h2";
    }

    @Override
    public String getUrl() {
        return "jdbc:h2:mem:liquibase";
    }
}
