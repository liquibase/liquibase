package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import lombok.Getter;

@Getter
public class ClearDatabaseChangeLogTableStatement extends AbstractSqlStatement {

    private final String catalogName;
    private final String schemaName;

    public ClearDatabaseChangeLogTableStatement(String catalogName, String schemaName) {
        super();
        this.catalogName = catalogName;
        this.schemaName = schemaName;
    }

}
