package liquibase.database.core.config;

import liquibase.sdk.supplier.database.ConnectionConfiguration;

public class SQLiteConfigStandard extends ConnectionConfiguration {
    @Override
    public String getDatabaseShortName() {
        return "sqlite";
    }

    @Override
    public String getConfigurationName() {
        return NAME_STANDARD;
    }

    @Override
    public String getUrl() {
        return "jdbc:sqlite:sqlite/liquibase.db";
    }
}
