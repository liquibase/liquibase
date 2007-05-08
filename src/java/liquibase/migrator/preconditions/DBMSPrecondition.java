package liquibase.migrator.preconditions;

import liquibase.database.MSSQLDatabase;
import liquibase.database.MySQLDatabase;
import liquibase.database.OracleDatabase;
import liquibase.database.PostgresDatabase;
import liquibase.migrator.Migrator;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class DBMSPrecondition implements Precondition {
    private String type;
    private Map<String, String> productToTypeMap;


    public DBMSPrecondition() {
        productToTypeMap = new HashMap<String, String>();
        productToTypeMap.put(OracleDatabase.PRODUCT_NAME.toLowerCase(), "oracle");
        productToTypeMap.put(MySQLDatabase.PRODUCT_NAME.toLowerCase(), "mysql");
        productToTypeMap.put(PostgresDatabase.PRODUCT_NAME.toLowerCase(), "postgresql");
        productToTypeMap.put(MSSQLDatabase.PRODUCT_NAME.toLowerCase(), "mssql");
    }

    public String getType() {
        return type;
    }

    public void setType(String atype) {
        this.type = atype.toLowerCase();
    }

    public boolean checkDatabaseType(Migrator migrator) {
        try {
            String product = migrator.getDatabase().getConnection().getMetaData().getDatabaseProductName();
            String expectedType = productToTypeMap.get(product.toLowerCase());
            return type.equals(expectedType);
        } catch (SQLException e) {
            throw new RuntimeException("Cannot determine database type");
        }
    }
}
