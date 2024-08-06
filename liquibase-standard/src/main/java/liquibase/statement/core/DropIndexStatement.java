package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import lombok.Getter;

@Getter
public class DropIndexStatement extends AbstractSqlStatement {

    private final String indexName;
    private final String tableCatalogName;
    private final String tableSchemaName;
    private final String tableName;
    private String associatedWith;

    public DropIndexStatement(String indexName, String tableCatalogName, String tableSchemaName, String tableName, String associatedWith) {
        this.tableCatalogName = tableCatalogName;
        this.tableSchemaName = tableSchemaName;
        this.indexName = indexName;
        this.tableName = tableName;
        this.associatedWith = associatedWith;
    }

    public void setAssociatedWith(String associatedWith) {
        this.associatedWith = associatedWith;
    }
}
