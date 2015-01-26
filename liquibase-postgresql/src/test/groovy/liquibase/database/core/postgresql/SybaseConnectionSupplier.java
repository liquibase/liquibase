package liquibase.database.core.postgresql;


import liquibase.database.ConnectionSupplier;

public class SybaseConnectionSupplier extends ConnectionSupplier {
    @Override
    public String getDatabaseShortName() {
        return "sybase";
    }

//    @Override
//    public ConfigTemplate getPuppetTemplate(Map<String, Object> context) {
//        return null;
//    }


    @Override
    public String getAdminUsername() {
        return "sa";
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:sybase:Tds:"+ getIpAddress()+":5000/liquibase";
    }
}
