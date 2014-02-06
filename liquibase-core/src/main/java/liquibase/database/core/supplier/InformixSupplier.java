package liquibase.database.core.supplier;

import liquibase.sdk.supplier.database.ConnectionSupplier;

public class InformixSupplier extends ConnectionSupplier {
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
