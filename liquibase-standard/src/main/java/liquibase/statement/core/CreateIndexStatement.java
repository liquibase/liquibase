package liquibase.statement.core;

import liquibase.change.AddColumnConfig;
import liquibase.statement.AbstractSqlStatement;
import liquibase.statement.CompoundStatement;
import lombok.Getter;
import lombok.Setter;

public class CreateIndexStatement extends AbstractSqlStatement implements CompoundStatement {

    @Getter
    private final String tableCatalogName;
    @Getter
    private final String tableSchemaName;
    @Getter
    private final String indexName;
    @Getter
    private final String tableName;
    @Getter
    private final AddColumnConfig[] columns;
    @Getter
    private String tablespace;
    private final Boolean unique;
    // Contain associations of index
    // for example: foreignKey, primaryKey or uniqueConstraint
    @Getter
    @Setter
    private String associatedWith;
    private Boolean clustered;
    @Getter
    private String using;

    public CreateIndexStatement(String indexName, String tableCatalogName, String tableSchemaName, String tableName, Boolean isUnique, String associatedWith, String using, AddColumnConfig... columns) {
        this(indexName, tableCatalogName, tableSchemaName, tableName, isUnique, associatedWith, columns);
        this.using = using;
    }

    public CreateIndexStatement(String indexName, String tableCatalogName, String tableSchemaName, String tableName, Boolean isUnique, String associatedWith, AddColumnConfig... columns) {
        this.indexName = indexName;
        this.tableCatalogName = tableCatalogName;
        this.tableSchemaName = tableSchemaName;
        this.tableName = tableName;
        this.columns = columns;
        this.unique = isUnique;
        this.associatedWith = associatedWith;
    }

    public CreateIndexStatement setTablespace(String tablespace) {
        this.tablespace = tablespace;

        return this;
    }

    public Boolean isUnique() {
        return unique;
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
