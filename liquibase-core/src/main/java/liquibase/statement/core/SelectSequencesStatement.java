package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class SelectSequencesStatement extends AbstractSqlStatement {
    private String schemaName;

    public SelectSequencesStatement(String schemaName) {
        this.schemaName = schemaName;
    }

    public String getSchemaName() {
        return schemaName;
    }
}
