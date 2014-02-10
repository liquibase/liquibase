package liquibase.database.core.supplier;

import liquibase.sdk.supplier.database.ConnectionSupplier;

public class FirebirdConnSupplier extends ConnectionSupplier {
    @Override
    public String getDatabaseShortName() {
        return "firebird";
    }

    @Override
    public String getAdminUsername() {
        return null;
    }

    @Override
    public String getConfigurationName() {
        return CONFIG_NAME_STANDARD;
    }

    @Override
    public String getUrl() {
        return "jdbc:firebirdsql:"+ getDatabaseShortName() +"/3050:c:\\firebird\\liquibase.fdb";
    }
}
