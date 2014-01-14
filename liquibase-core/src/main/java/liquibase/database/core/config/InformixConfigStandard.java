package liquibase.database.core.config;

import liquibase.sdk.supplier.database.ConnectionConfiguration;

public class InformixConfigStandard extends ConnectionConfiguration {
    @Override
    public String getDatabaseShortName() {
        return "informix";
    }

    @Override
    public String getConfigurationName() {
        return NAME_STANDARD;
    }

    @Override
    public String getUrl() {
        return "jdbc:informix-sqli://" + getHostname() + ":9088/liquibase:informixserver=ol_ids_1150_1";
    }
}
