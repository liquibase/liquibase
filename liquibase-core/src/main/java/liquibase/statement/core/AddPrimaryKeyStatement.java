package liquibase.statement.core;

import liquibase.structure.DatabaseObject;
import liquibase.structure.core.PrimaryKey;
import liquibase.structure.core.Table;

/**
 * Adds a primary key to an existing table.
 */
public class AddPrimaryKeyStatement extends AbstractTableStatement {

    private static final String TABLESPACE = "tablespace";
    private static final String COLUMN_NAMES = "columnNames";
    private static final String CONSTRAINT_NAME = "constraintName";

    public AddPrimaryKeyStatement() {
    }

    public AddPrimaryKeyStatement(String catalogName, String schemaName, String tableName, String columnNames, String constraintName) {
        super(catalogName, schemaName, tableName);
        setColumnNames(columnNames);
        setConstraintName(constraintName);
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

    public String getConstraintName() {
        return getAttribute(CONSTRAINT_NAME, String.class);
    }

    public AddPrimaryKeyStatement setConstraintName(String constraintName) {
        return (AddPrimaryKeyStatement) setAttribute(CONSTRAINT_NAME, constraintName);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new PrimaryKey().setTable((Table) new Table().setName(getTableName()).setSchema(getCatalogName(), getSchemaName()))
        };
    }
}
