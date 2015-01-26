package liquibase.database.core.firebird;

import liquibase.database.ConnectionSupplier;

public class FirebirdConnectionSupplier extends ConnectionSupplier {
    @Override
    public String getDatabaseShortName() {
        return "firebird";
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
        return "jdbc:firebirdsql:"+ getDatabaseShortName() +"/3050:c:\\firebird\\liquibase.fdb";
    }
}
