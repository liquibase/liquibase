package liquibase.statement.core;

import liquibase.change.core.StoredLogicArgumentChange;
import liquibase.statement.AbstractSqlStatement;

import java.util.List;

public class DropProcedureStatement extends AbstractSqlStatement {

    private String catalogName;
    private String schemaName;
    private String procedureName;
    private List<StoredLogicArgumentChange> arguments;

    public DropProcedureStatement(String catalogName, String schemaName, String procedureName, List<StoredLogicArgumentChange> arguments) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.procedureName = procedureName;
        this.arguments = arguments;
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

    public List<StoredLogicArgumentChange> getArguments() {
        return arguments;
    }
}
