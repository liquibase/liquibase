package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import lombok.Getter;

@Getter
public class DropSequenceStatement extends AbstractSqlStatement {

    private final String catalogName;
    private final String schemaName;
    private final String sequenceName;

    public DropSequenceStatement(String catalogName, String schemaName, String sequenceName) {
        this.catalogName  =catalogName;
        this.schemaName = schemaName;
        this.sequenceName = sequenceName;
    }

    @Override
    public boolean skipOnUnsupported() {
        return true;
    }

}
