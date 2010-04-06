package liquibase.statement.core;

import liquibase.statement.SqlStatement;

public class DropIndexStatement implements SqlStatement {

    private String indexName;
    private String tableSchemaName;
    private String tableName;
    private String associatedWith;

    public DropIndexStatement(String indexName, String tableSchemaName, String tableName, String associatedWith) {
        this.tableSchemaName = tableSchemaName;
        this.indexName = indexName;
        this.tableName = tableName;
        this.associatedWith = associatedWith;
    }

    public String getTableSchemaName() {
        return tableSchemaName;
    }

    public String getIndexName() {
        return indexName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getAssociatedWith() {
        return associatedWith;
    }

    public void setAssociatedWith(String associatedWith) {
        this.associatedWith = associatedWith;
    }
}
