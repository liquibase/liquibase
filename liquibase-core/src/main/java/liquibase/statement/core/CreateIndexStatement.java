package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Index;
import liquibase.structure.core.Table;

/**
 * Creates an index on an existing table
 */
public class CreateIndexStatement extends AbstractIndexStatement {

    public static final String COLUMN_NAMES ="columnNames";
    public static final String TABLESPACE = "tablespace";
    public static final String UNIQUE = "unique";
	public static final String ASSOCIATED_WITH = "associatedWith";

    public CreateIndexStatement() {
    }

    public CreateIndexStatement(String indexName, String tableCatalogName, String tableSchemaName, String tableName, Boolean isUnique, String associatedWith, String... columns) {
        super(indexName, tableCatalogName, tableSchemaName, tableName);
        setColumnNames(columns);
        setUnique(isUnique);
	    setAssociatedWith(associatedWith);
    }


    public String[] getColumnNames() {
        return getAttribute(COLUMN_NAMES, String[].class);
    }

    public CreateIndexStatement setColumnNames(String[] columns) {
        return (CreateIndexStatement) setAttribute(COLUMN_NAMES, columns);
    }

    public String getTablespace() {
        return getAttribute(TABLESPACE, String.class);
    }

    public CreateIndexStatement setTablespace(String tablespace) {
        return (CreateIndexStatement) setAttribute(TABLESPACE, tablespace);
    }

    public Boolean isUnique() {
        return getAttribute(UNIQUE, Boolean.class);
    }

    public CreateIndexStatement setUnique(Boolean unique) {
        return (CreateIndexStatement) setAttribute(UNIQUE, unique);
    }


    public String getAssociatedWith() {
        return getAttribute(ASSOCIATED_WITH, String.class);
	}

	public CreateIndexStatement setAssociatedWith(String associatedWith) {
		return (CreateIndexStatement) setAttribute(ASSOCIATED_WITH, associatedWith);
	}

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new Index().setName(getIndexName()).setTable((Table) new Table().setName(getTableName()).setSchema(getTableCatalogName(), getTableSchemaName()))
        };
    }
}