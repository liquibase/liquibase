package liquibase.statement.core;

import liquibase.structure.DatabaseObject;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;

/**
 * Adds a primary key to an existing table.
 */
public class AddPrimaryKeyStatement extends AbstractPrimaryKeyStatement {

    private static final String TABLESPACE = "tablespace";
    private static final String COLUMN_NAMES = "columnNames";

    public AddPrimaryKeyStatement() {
    }

    public AddPrimaryKeyStatement(String catalogName, String schemaName, String tableName, String columnNames, String constraintName) {
        super(constraintName, catalogName, schemaName, tableName);
        setColumnNames(columnNames);
    }

    public String getTablespace() {
        return getAttribute(TABLESPACE, String.class);
    }

    public AddPrimaryKeyStatement setTablespace(String tablespace) {
        return (AddPrimaryKeyStatement) setAttribute(TABLESPACE, tablespace);
    }

    public String getColumnNames() {
        return getAttribute(COLUMN_NAMES, String.class);
    }

    public AddPrimaryKeyStatement setColumnNames(String columnNames) {
        return (AddPrimaryKeyStatement) setAttribute(COLUMN_NAMES, columnNames);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new PrimaryKey().setTable((Table) new Table().setName(getTableName()).setSchema(getCatalogName(), getSchemaName()))
        };
    }
}
