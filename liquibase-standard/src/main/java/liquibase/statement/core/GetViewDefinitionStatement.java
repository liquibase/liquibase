package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import lombok.Getter;

@Getter
public class GetViewDefinitionStatement extends AbstractSqlStatement {
    private final String catalogName;
    private final String schemaName;
    private final String viewName;

    public GetViewDefinitionStatement(String catalogName, String schemaName, String viewName) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.viewName = viewName;
    }

}
