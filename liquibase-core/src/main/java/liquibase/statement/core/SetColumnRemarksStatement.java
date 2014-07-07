package liquibase.statement.core;

import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Column;
import liquibase.structure.core.Table;

/**
 * Saves remarks to column metadata.
 */
public class SetColumnRemarksStatement extends AbstractColumnStatement {

    public static final String REMARKS = "remarks";

    public SetColumnRemarksStatement() {
    }

    public SetColumnRemarksStatement(String catalogName, String schemaName, String tableName, String columnName, String remarks) {
        super(catalogName, schemaName, tableName, columnName);
        setRemarks(remarks);
    }

    public String getRemarks() {
        return getAttribute(REMARKS, String.class);
    }

    public SetColumnRemarksStatement setRemarks(String remarks) {
        return (SetColumnRemarksStatement) setAttribute(REMARKS, remarks);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new Column().setName(getColumnName()).setRelation(new Table().setName(getTableName()).setSchema(getCatalogName(), getSchemaName()))
        };
    }
}
