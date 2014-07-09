package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.StoredProcedure;

public class DropProcedureStatement extends AbstractProcedureStatement {

    public DropProcedureStatement() {
    }

    public DropProcedureStatement(String catalogName, String schemaName, String procedureName) {
        super(catalogName, schemaName, procedureName);
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
                new StoredProcedure().setName(getProcedureName()).setSchema(new Schema(getCatalogName(), getSchemaName()))
        };
    }
}
