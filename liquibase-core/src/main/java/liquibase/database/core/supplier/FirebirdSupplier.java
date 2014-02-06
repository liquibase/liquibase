package liquibase.database.core.supplier;

import liquibase.sdk.supplier.database.ConnectionSupplier;

public class FirebirdSupplier extends ConnectionSupplier {
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
