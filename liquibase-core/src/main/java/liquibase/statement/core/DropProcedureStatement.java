package liquibase.statement.core;

import liquibase.statement.AbstractStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
import liquibase.structure.core.StoredProcedure;

public class DropProcedureStatement extends AbstractStatement {

    private String catalogName;
    private String schemaName;
    private String procedureName;

    public DropProcedureStatement(String catalogName, String schemaName, String procedureName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.procedureName = procedureName;
    }

    public String getCatalogName() {
        return catalogName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getProcedureName() {
        return procedureName;
    }

    @Override
    protected DatabaseObject[] getBaseAffectedDatabaseObjects() {
        return new DatabaseObject[] {
                new StoredProcedure().setName(getProcedureName()).setSchema(new Schema(getCatalogName(), getSchemaName()))
        };
    }
}
