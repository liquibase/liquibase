package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import lombok.Getter;

@Getter
public class RenameViewStatement extends AbstractSqlStatement {

    private final String catalogName;
    private final String schemaName;
    private final String oldViewName;
    private final String newViewName;

    public RenameViewStatement(String catalogName, String schemaName, String oldViewName, String newViewName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.oldViewName = oldViewName;
        this.newViewName = newViewName;
    }


}
