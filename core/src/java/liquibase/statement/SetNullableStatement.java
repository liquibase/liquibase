package liquibase.statement;

public class SetNullableStatement implements SqlStatement {
    private String schemaName;
    private String tableName;
    private String columnName;
    private String columnDataType;
    private boolean nullable;

    public SetNullableStatement(String schemaName, String tableName, String columnName, String columnDataType, boolean nullable) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.columnDataType = columnDataType;
        this.nullable = nullable;
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
}
