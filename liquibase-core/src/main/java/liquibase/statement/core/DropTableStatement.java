package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Table;

public class DropTableStatement extends AbstractTableStatement {

    public static final String CASCADE_CONSTRAINTS = "cascadeConstraints";

    public DropTableStatement() {
    }

    public DropTableStatement(String catalogName, String schemaName, String tableName, boolean cascadeConstraints) {
        super(catalogName, schemaName, tableName);
        setCascadeConstraints(cascadeConstraints);
    }

    /**
     * Return if constraints should be removed when the table is dropped.
     * Defaults to false.
     */
    public boolean isCascadeConstraints() {
        return getAttribute(CASCADE_CONSTRAINTS, false);
    }

    public DropTableStatement setCascadeConstraints(boolean cascade) {
        return (DropTableStatement) setAttribute(CASCADE_CONSTRAINTS, cascade);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new Table().setName(getTableName()).setSchema(getCatalogName(), getSchemaName())
        };
    }
}
