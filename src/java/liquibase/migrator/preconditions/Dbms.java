package liquibase.migrator.preconditions;

import liquibase.migrator.Migrator;

import java.sql.SQLException;

public class Dbms {
    private String type;

    public Dbms() {
        type = "";
    }

    public String getType() {
        return type;
    }

    public void setType(String atype) {
        this.type = atype.toLowerCase();
    }

    public boolean checkDatabaseType(Migrator migrator) throws SQLException {

        String product = migrator.getDatabase().getConnection().getMetaData().getDatabaseProductName();

        return type.equals(product.toLowerCase());
    }
}
