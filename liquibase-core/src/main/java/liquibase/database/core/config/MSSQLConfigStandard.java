package liquibase.database.core.config;

import liquibase.sdk.supplier.database.ConnectionConfiguration;

public class MSSQLConfigStandard extends ConnectionConfiguration {
    @Override
    public String getDatabaseShortName() {
        return "mssql";
    }

    @Override
    public String getConfigurationName() {
        return NAME_STANDARD;
    }

    @Override
    public String getVagrantBoxName() {
        return "windows";
    }

    @Override
    public String getUrl() {
        return "jdbc:sqlserver://"+ getHostname() +":1433;databaseName=liquibase";
    }
}
