package liquibase.migrator.preconditions;

import java.sql.SQLException;
import java.util.Map;
import java.util.HashMap;

import liquibase.migrator.Migrator;

public class DBMSPrecondition implements Precondition{
    private String type;
    private Map<String, String> productToTypeMap ;


    public DBMSPrecondition() {
        productToTypeMap = new HashMap<String, String>();
        //TODO: get real product names
        productToTypeMap.put("Oracle", "oracle");
        productToTypeMap.put("MySQL", "mysql");
        productToTypeMap.put("Postgres", "postgresql");
        productToTypeMap.put("Microsoft SQL Server", "mssql");
    }

    public String getType() {
        return type;
    }

    public void setType(String atype) {
        this.type = atype.toLowerCase();
    }

    public boolean checkDatabaseType(Migrator migrator) {

        String product;
        boolean flag = true;
        try {
            product = migrator.getDatabase().getConnection().getMetaData().getDatabaseProductName();

            if (type.equals(product.toLowerCase()))
                flag = true;
            else
                flag = false;

        } catch (SQLException e) {
            throw new RuntimeException("Cannot determine database type");
        }

        return flag;
    }
}
