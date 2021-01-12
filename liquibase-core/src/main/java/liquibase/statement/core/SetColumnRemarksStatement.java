package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class SetColumnRemarksStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String remarks;
    private String columnDataType;
    private Object defaultValue;

    public SetColumnRemarksStatement(String catalogName,
                                     String schemaName,
                                     String tableName,
                                     String columnName,
                                     String remarks,
                                     String columnDataType,
                                     Object defaultValue) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.remarks = remarks;
        this.columnDataType = columnDataType;
        this.defaultValue = defaultValue;
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

    public String getColumnName() {
        return columnName;
    }

    public String getRemarks() {
        return remarks;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
