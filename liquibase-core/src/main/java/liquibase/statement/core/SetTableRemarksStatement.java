package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;

/**
 * Saves remarks to table metadata.
 */
public class SetTableRemarksStatement extends AbstractTableStatement {

    public static final String REMARKS = "remarks";

    public SetTableRemarksStatement() {
    }

    public SetTableRemarksStatement(String catalogName, String schemaName, String tableName, String remarks) {
        super(catalogName, schemaName, tableName);
        setRemarks(remarks);
    }
    public String getRemarks() {
        return getAttribute(REMARKS, String.class);
    }

    public SetTableRemarksStatement setRemarks(String remarks) {
        return (SetTableRemarksStatement) setAttribute(REMARKS, remarks);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new Table().setName(getTableName()).setSchema(getCatalogName(), getSchemaName())
        };
    }
}
