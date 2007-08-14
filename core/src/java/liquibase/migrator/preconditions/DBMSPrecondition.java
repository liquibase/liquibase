package liquibase.migrator.preconditions;

import liquibase.migrator.Migrator;

/**
 * Precondition for specifying the type of database (oracle, mysql, etc.).
 */

public class DBMSPrecondition implements Precondition {
    private String type;


    public DBMSPrecondition() {
    }

    public String getType() {
        return type;
    }

    public void setType(String atype) {
        this.type = atype.toLowerCase();
    }

    public boolean checkDatabaseType(Migrator migrator) {
        String dbType = migrator.getDatabase().getTypeName();
        return type.equals(dbType);
    }
}
