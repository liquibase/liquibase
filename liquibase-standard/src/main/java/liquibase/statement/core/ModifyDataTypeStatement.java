package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class ModifyDataTypeStatement extends AbstractSqlStatement {
    private String columnName;
    private String newDataType;
    private DatabaseTableIdentifier databaseTableIdentifier = new DatabaseTableIdentifier(null, null, null);

    public ModifyDataTypeStatement(String catalogName, String schemaName, String tableName, String columnName, String newDataType) {
        this.databaseTableIdentifier.setCatalogName(catalogName);
        this.databaseTableIdentifier.setSchemaName(schemaName);
        this.databaseTableIdentifier.setTableName(tableName);
        this.columnName = columnName;
        this.newDataType = newDataType;
    }

    public String getCatalogName() {
        return databaseTableIdentifier.getCatalogName();
    }

    public String getSchemaName() {
        return databaseTableIdentifier.getSchemaName();
    }

    public void setSchemaName(String schemaName) {
        this.databaseTableIdentifier.setSchemaName(schemaName);
    }

    public String getTableName() {
        return databaseTableIdentifier.getTableName();
    }

    public void setTableName(String tableName) {
        this.databaseTableIdentifier.setTableName(tableName);
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public String getNewDataType() {
        return newDataType;
    }

    public void setNewDataType(String newDataType) {
        this.newDataType = newDataType;
    }   
}
