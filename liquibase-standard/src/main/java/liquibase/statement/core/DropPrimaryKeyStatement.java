package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class DropPrimaryKeyStatement extends AbstractSqlStatement {

    private final String constraintName;
    private Boolean dropIndex;
    private DatabaseTableIdentifier databaseTableIdentifier = new DatabaseTableIdentifier(null, null, null);

    public DropPrimaryKeyStatement(String catalogName, String schemaName, String tableName, String constraintName) {
        this.databaseTableIdentifier.setCatalogName(catalogName);
        this.databaseTableIdentifier.setSchemaName(schemaName);
        this.databaseTableIdentifier.setTableName(tableName);
        this.constraintName = constraintName;
    }

    public String getCatalogName() {
        return databaseTableIdentifier.getCatalogName();
    }

    public String getSchemaName() {
        return databaseTableIdentifier.getSchemaName();
    }

    public String getTableName() {
        return databaseTableIdentifier.getTableName();
    }

    public String getConstraintName() {
        return constraintName;
    }

    public Boolean getDropIndex() {
        return dropIndex;
    }

    public void setDropIndex(Boolean dropIndex) {
        this.dropIndex = dropIndex;
    }
}
