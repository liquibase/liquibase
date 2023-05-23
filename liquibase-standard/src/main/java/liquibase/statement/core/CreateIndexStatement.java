package liquibase.statement.core;

import liquibase.change.AddColumnConfig;
import liquibase.statement.AbstractSqlStatement;
import liquibase.statement.CompoundStatement;

public class CreateIndexStatement extends AbstractSqlStatement implements CompoundStatement {

    private final String tableCatalogName;
    private final String tableSchemaName;
    private final String indexName;
    private final String tableName;
    private final AddColumnConfig[] columns;
    private String tablespace;
    private final Boolean unique;
    // Contain associations of index
    // for example: foreignKey, primaryKey or uniqueConstraint
    private String associatedWith;
    private Boolean clustered;

    public CreateIndexStatement(String indexName, String tableCatalogName, String tableSchemaName, String tableName, Boolean isUnique, String associatedWith, AddColumnConfig... columns) {
        this.indexName = indexName;
        this.tableCatalogName = tableCatalogName;
        this.tableSchemaName = tableSchemaName;
        this.tableName = tableName;
        this.columns = columns;
        this.unique = isUnique;
        this.associatedWith = associatedWith;
    }

    public String getTableCatalogName() {
        return tableCatalogName;
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

    public AddColumnConfig[] getColumns() {
        return columns;
    }

    public String getTablespace() {
        return tablespace;
    }

    public CreateIndexStatement setTablespace(String tablespace) {
        this.tablespace = tablespace;

        return this;
    }

    public Boolean isUnique() {
        return unique;
    }

    public String getAssociatedWith() {
        return associatedWith;
    }

    public void setAssociatedWith(String associatedWith) {
        this.associatedWith = associatedWith;
    }

    public Boolean isClustered() {
        return clustered;
    }

    public CreateIndexStatement setClustered(Boolean clustered) {
        if (clustered == null) {
            this.clustered = false;
        } else {
            this.clustered = clustered;
        }
        return this;
    }
}
