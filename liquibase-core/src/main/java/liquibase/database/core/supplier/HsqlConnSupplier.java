package liquibase.database.core.supplier;

import liquibase.sdk.supplier.database.ConnectionSupplier;

import java.util.Map;

public class HsqlConnSupplier extends ConnectionSupplier {
    @Override
    public String getDatabaseShortName() {
        return "hsqldb";
    }

    @Override
    public String getAdminUsername() {
        return null;
    }

    @Override
    public ConfigTemplate getPuppetTemplate(Map<String, Object> context) {
        return null;
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:hsqldb:mem:liquibase";
    }
}
