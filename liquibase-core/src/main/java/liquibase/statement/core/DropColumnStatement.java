package liquibase.statement.core;

import liquibase.statement.SqlStatement;

public class DropColumnStatement implements SqlStatement {

    private String schemaName;
    private String tableName;
    private String columnName;

    public DropColumnStatement(String schemaName, String tableName, String columnName) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
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

}
