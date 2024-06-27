package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class DropTableStatement extends AbstractSqlStatement {

    private final boolean cascadeConstraints;
    private DatabaseTableIdentifier databaseTableIdentifier = new DatabaseTableIdentifier(null, null, null);

    public DropTableStatement(String catalogName, String schemaName, String tableName, boolean cascadeConstraints) {
        this.databaseTableIdentifier.setCatalogName(catalogName);
        this.databaseTableIdentifier.setSchemaName(schemaName);
        this.databaseTableIdentifier.setTableName(tableName);
        this.cascadeConstraints = cascadeConstraints;
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

    public boolean isCascadeConstraints() {
        return cascadeConstraints;
    }
}
