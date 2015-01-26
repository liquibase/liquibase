package liquibase.database.core.informix;

import liquibase.database.ConnectionSupplier;

import java.util.Map;

public class InformixConnectionSupplier extends ConnectionSupplier {
    @Override
    public String getDatabaseShortName() {
        return "informix";
    }

    @Override
    public String getAdminUsername() {
        return null;
    }
//
//    @Override
//    public ConfigTemplate getPuppetTemplate(Map<String, Object> context) {
//        return null;
//    }
//
    @Override
    public String getJdbcUrl() {
        return "jdbc:informix-sqli://" + getIpAddress() + ":9088/liquibase:informixserver=ol_ids_1150_1";
    }
}
