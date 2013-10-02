package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class AddPrimaryKeyStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String tableName;
    private String tablespace;
    private String columnNames;
    private String constraintName;

    public AddPrimaryKeyStatement(String catalogName, String schemaName, String tableName, String columnNames, String constraintName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnNames = columnNames;
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

    public String getTablespace() {
        return tablespace;
    }

    public AddPrimaryKeyStatement setTablespace(String tablespace) {
        this.tablespace = tablespace;
        return this;
    }

    public String getColumnNames() {
        return columnNames;
    }

    public String getConstraintName() {
        return constraintName;
    }
}
