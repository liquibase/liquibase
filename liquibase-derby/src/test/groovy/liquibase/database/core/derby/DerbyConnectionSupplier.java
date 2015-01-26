package liquibase.database.core.derby;

import liquibase.database.ConnectionSupplier;

public class DerbyConnectionSupplier extends ConnectionSupplier {
    @Override
    public String getDatabaseShortName() {
        return "derby";
    }

//    @Override
//    public ConfigTemplate getPuppetTemplate(Map<String, Object> context) {
//        return null;
//    }

    @Override
    public String getAdminUsername() {
        return null;
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:derby:liquibase;create=true";
    }
}
