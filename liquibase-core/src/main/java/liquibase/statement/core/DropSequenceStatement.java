package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;

public class DropSequenceStatement extends AbstractSqlStatement {

    private String schemaName;
    private String sequenceName;

    public DropSequenceStatement(String schemaName, String sequenceName) {
        this.schemaName = schemaName;
        this.sequenceName = sequenceName;
    }

    @Override
    public boolean skipOnUnsupported() {
        return true;
    }    

    public String getSchemaName() {
        return schemaName;
    }

    public String getSequenceName() {
        return sequenceName;
    }
}
