package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class SetColumnRemarksStatement extends AbstractSqlStatement {

    private String schemaName;
    private String tableName;
    private String columnName;
    private String remarks;

    public SetColumnRemarksStatement(String schemaName, String tableName, String columnName, String remarks) {
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
        this.remarks = remarks;
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
}
