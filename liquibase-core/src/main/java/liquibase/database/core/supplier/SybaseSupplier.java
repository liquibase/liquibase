package liquibase.database.core.supplier;

import liquibase.sdk.supplier.database.ConnectionSupplier;

public class SybaseSupplier extends ConnectionSupplier {
    @Override
    public String getDatabaseShortName() {
        return "sybase";
    }

    @Override
    public String getConfigurationName() {
        return NAME_STANDARD;
    }

    @Override
    public String getUrl() {
        return "jdbc:sybase:Tds:"+ getHostname()+":5000/liquibase";
    }
}
