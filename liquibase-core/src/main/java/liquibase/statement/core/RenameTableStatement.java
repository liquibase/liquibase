package liquibase.statement.core;

import com.sun.org.apache.bcel.internal.generic.NEW;
import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;

/**
 * Renames an existing table.
 */
public class RenameTableStatement extends AbstractTableStatement {
    public static final String NEW_TABLE_NAME = "newTableName";

    public RenameTableStatement() {
    }

    public RenameTableStatement(String catalogName, String schemaName, String oldTableName, String newTableName) {
        super(catalogName, schemaName, oldTableName);
        setNewTableName(newTableName);
    }

    public String getNewTableName() {
        return getAttribute(NEW_TABLE_NAME, String.class);
    }

    public RenameTableStatement setNewTableName(String newTableName) {
        return (RenameTableStatement) setAttribute(NEW_TABLE_NAME, newTableName);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new Table().setName(getNewTableName()).setSchema(getCatalogName(), getSchemaName()),
            new Table().setName(getTableName()).setSchema(getCatalogName(), getSchemaName())
        };
    }
}
