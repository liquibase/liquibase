package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class AddDefaultValueStatement extends AbstractSqlStatement {
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String columnDataType;
    private Object defaultValue;

    private String defaultValueConstraintName;

    public AddDefaultValueStatement(String catalogName, String schemaName, String tableName, String columnName, String columnDataType) {
        this(catalogName, schemaName, tableName, columnName, columnDataType, null);
    }

    public AddDefaultValueStatement(String catalogName, String schemaName, String tableName, String columnName, String columnDataType, Object defaultValue) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.columnDataType = columnDataType;
        this.defaultValue = defaultValue;
    }

    public String getColumnName() {
        return columnName;
    }
    
    public String getColumnDataType() {
        return columnDataType;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getDefaultValueConstraintName() {
        return defaultValueConstraintName;
    }

    public void setDefaultValueConstraintName(String defaultValueConstraintName) {
        this.defaultValueConstraintName = defaultValueConstraintName;
    }
}
