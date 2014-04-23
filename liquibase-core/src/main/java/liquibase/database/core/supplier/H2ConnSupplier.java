package liquibase.database.core.supplier;

import liquibase.sdk.supplier.database.ConnectionSupplier;

import java.util.Map;

public class H2ConnSupplier extends ConnectionSupplier {

    @Override
    public String getAdminUsername() {
        return null;
    }

    @Override
    public ConfigTemplate getPuppetTemplate(Map<String, Object> context) {
        return null;
    }

    @Override
    public String getDatabaseShortName() {
        return "h2";
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:h2:mem:liquibase";
    }
}
