package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class DropColumnStatement extends AbstractSqlStatement {

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
