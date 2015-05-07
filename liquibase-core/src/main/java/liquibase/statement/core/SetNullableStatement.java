package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class SetNullableStatement extends AbstractSqlStatement {
    private String catalogName;
    private String schemaName;
    private String tableName;
    private String columnName;
    private String columnDataType;
    private boolean nullable;
    private String constraintName;

    public SetNullableStatement(String catalogName, String schemaName, String tableName, String columnName, String columnDataType, boolean nullable) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.columnDataType = columnDataType;
        this.nullable = nullable;
    }

    public SetNullableStatement(String catalogName, String schemaName, String tableName, String columnName, String columnDataType, boolean nullable, String constraintName) {
        this(catalogName, schemaName, tableName, columnName, columnDataType, nullable);
        this.constraintName = constraintName;
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

    public String getColumnDataType() {
        return columnDataType;
    }

    public boolean isNullable() {
        return nullable;
    }

    public String getConstraintName() {
        return constraintName;
    }
}
