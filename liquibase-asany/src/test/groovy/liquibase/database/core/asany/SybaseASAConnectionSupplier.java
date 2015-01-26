package liquibase.database.core.asany;


import liquibase.database.ConnectionSupplier;

public class SybaseASAConnectionSupplier extends ConnectionSupplier {
    @Override
    public String getDatabaseShortName() {
        return "asany";
    }

//    @Override
//    public ConnectionSupplier.ConfigTemplate getPuppetTemplate(Map<String, Object> context) {
//        return null;
//    }

    @Override
    public String getAdminUsername() {
        return "sa";
    }

    @Override
    public String getJdbcUrl() {
        return "jdbc:sybase:Tds:"+ getIpAddress() +":9810/servicename=prior";
    }
}
