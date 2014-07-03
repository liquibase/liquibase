package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Sequence;

public class DropSequenceStatement extends AbstractSequenceStatement {

    public DropSequenceStatement() {
    }

    public DropSequenceStatement(String catalogName, String schemaName, String sequenceName) {
        super(catalogName, schemaName, sequenceName);
    }

    @Override
    public boolean skipOnUnsupported() {
        return true;
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
            new Sequence().setName(getSequenceName()).setSchema(getCatalogName(), getSchemaName())
        };
    }
}
