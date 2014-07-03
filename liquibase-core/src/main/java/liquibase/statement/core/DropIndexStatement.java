package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;

public class DropIndexStatement extends AbstractIndexStatement {

    public static final String ASSOCIATED_WITH = "associatedWith";

    public DropIndexStatement() {
    }

    public DropIndexStatement(String indexName, String tableCatalogName, String tableSchemaName, String tableName, String associatedWith) {
        super(indexName, tableCatalogName, tableSchemaName, tableName);
        setAssociatedWith(associatedWith);
    }

    public String getAssociatedWith() {
        return getAttribute(ASSOCIATED_WITH, String.class);
    }

    public DropIndexStatement setAssociatedWith(String associatedWith) {
        return (DropIndexStatement) setAttribute(ASSOCIATED_WITH, associatedWith);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        Table table = null;
        if (getTableName() != null) {
            table = (Table) new Table().setName(getTableName()).setSchema(getTableCatalogName(), getTableSchemaName());
        }

        return new DatabaseObject[]{
                new Index().setName(getIndexName()).setTable(table)
        };
    }
}
