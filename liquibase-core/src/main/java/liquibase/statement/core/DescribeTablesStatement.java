package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import liquibase.statement.SqlStatement;

public class DescribeTablesStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String tableName;

    public DescribeTablesStatement() {
    }

    public DescribeTablesStatement(String catalogName, String schemaName, String tableName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.tableName = tableName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public void setCatalogName(String catalogName) {
        this.catalogName = catalogName;
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
}
