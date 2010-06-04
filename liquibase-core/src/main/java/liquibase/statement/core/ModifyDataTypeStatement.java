package liquibase.statement.core;

import liquibase.statement.SqlStatement;

public class ModifyDataTypeStatement implements SqlStatement {
    private String schemaName;
    private String tableName;
    private String columnName;
    private String newDataType;
    private Boolean nullable;
    private Boolean autoIncrement;
    private Boolean primaryKey;

    public ModifyDataTypeStatement(String schemaName, String tableName, String columnName, String newDataType, Boolean nullable, Boolean primaryKey, Boolean autoIncrement) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.newDataType = newDataType;
        this.nullable = nullable;
        this.primaryKey = primaryKey;
        this.autoIncrement = autoIncrement;
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

    public Boolean isNullable() {
        return nullable;
    }

    public void setNullable(Boolean nullable) {
        this.nullable = nullable;
    }

    public Boolean getAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(Boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public Boolean getPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(Boolean primaryKey) {
        this.primaryKey = primaryKey;
    }
}
