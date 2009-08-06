package liquibase.statement.core;

import liquibase.statement.SqlStatement;

public class DropSequenceStatement implements SqlStatement {

    private String schemaName;
    private String sequenceName;

    public DropSequenceStatement(String schemaName, String sequenceName) {
        this.schemaName = schemaName;
        this.sequenceName = sequenceName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getSequenceName() {
        return sequenceName;
    }
}
