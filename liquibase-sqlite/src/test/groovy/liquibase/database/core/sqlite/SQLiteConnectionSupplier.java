package liquibase.database.core.sqlite;


import liquibase.database.ConnectionSupplier;

import java.util.Map;

public class SQLiteConnectionSupplier extends ConnectionSupplier {
    @Override
    public String getDatabaseShortName() {
        return "sqlite";
    }

//    @Override
//    public ConfigTemplate getPuppetTemplate(Map<String, Object> context) {
//        return null;
//    }
//
    @Override
    public String getAdminUsername() {
        return null;
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:sqlite:sqlite/liquibase.db";
    }
}
