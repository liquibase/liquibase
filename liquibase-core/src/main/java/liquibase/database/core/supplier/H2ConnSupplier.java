package liquibase.database.core.supplier;

import liquibase.sdk.supplier.database.ConnectionSupplier;

public class H2ConnSupplier extends ConnectionSupplier {

    @Override
    public String getConfigurationName() {
        return NAME_STANDARD;
    }

    @Override
    public String getDatabaseShortName() {
        return "h2";
    }

    @Override
    public String getUrl() {
        return "jdbc:h2:mem:liquibase";
    }
}
