package liquibase.database.core.supplier;

import liquibase.sdk.supplier.database.ConnectionSupplier;

public class MSSQLSupplier extends ConnectionSupplier {
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
