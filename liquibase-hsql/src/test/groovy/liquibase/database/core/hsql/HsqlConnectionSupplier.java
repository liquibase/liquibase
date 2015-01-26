package liquibase.database.core.hsql;

import liquibase.database.ConnectionSupplier;

public class HsqlConnectionSupplier extends ConnectionSupplier {
    @Override
    public String getDatabaseShortName() {
        return "hsqldb";
    }

    @Override
    public String getAdminUsername() {
        return null;
    }

//    @Override
//    public ConfigTemplate getPuppetTemplate(Map<String, Object> context) {
//        return null;
//    }
//
    @Override
    public String getJdbcUrl() {
        return "jdbc:hsqldb:mem:liquibase";
    }
}
