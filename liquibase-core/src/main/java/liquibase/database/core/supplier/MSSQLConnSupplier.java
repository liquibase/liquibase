package liquibase.database.core.supplier;

import liquibase.sdk.supplier.database.ConnectionSupplier;

public class MSSQLConnSupplier extends ConnectionSupplier {
    @Override
    public String getDatabaseShortName() {
        return "mssql";
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
    public String getVagrantBaseBoxName() {
        return "windows";
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:sqlserver://"+ getIpAddress() +":1433;databaseName=liquibase";
    }
}
