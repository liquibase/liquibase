package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class ModifyDataTypeStatement extends AbstractSqlStatement {
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String newDataType;

    public ModifyDataTypeStatement(String catalogName, String schemaName, String tableName, String columnName, String newDataType) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.newDataType = newDataType;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
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
