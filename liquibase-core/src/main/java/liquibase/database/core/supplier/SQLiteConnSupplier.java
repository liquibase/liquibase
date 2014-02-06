package liquibase.database.core.supplier;

import liquibase.sdk.supplier.database.ConnectionSupplier;

public class SQLiteConnSupplier extends ConnectionSupplier {
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
