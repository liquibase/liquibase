package liquibase.database.core.config;

import liquibase.sdk.supplier.database.ConnectionConfiguration;

public class FirebirdConfigStandard extends ConnectionConfiguration {
    @Override
    public String getDatabaseShortName() {
        return "firebird";
    }

    @Override
    public String getConfigurationName() {
        return NAME_STANDARD;
    }

    @Override
    public String getUrl() {
        return "jdbc:firebirdsql:"+ getDatabaseShortName() +"/3050:c:\\firebird\\liquibase.fdb";
    }
}
