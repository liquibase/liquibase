package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import lombok.Getter;

@Getter
public class DropProcedureStatement extends AbstractSqlStatement {

    private final String catalogName;
    private final String schemaName;
    private final String procedureName;

    public DropProcedureStatement(String catalogName, String schemaName, String procedureName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.procedureName = procedureName;
    }

}
