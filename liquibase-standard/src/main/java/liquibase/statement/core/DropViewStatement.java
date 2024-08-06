package liquibase.statement.core;

import liquibase.statement.AbstractSqlStatement;
import lombok.Getter;

public class DropViewStatement extends AbstractSqlStatement {

    @Getter
    private final String catalogName;
    @Getter
    private final String schemaName;
    @Getter
    private final String viewName;
    private final Boolean ifExists;

    public DropViewStatement(String catalogName, String schemaName, String viewName) {
        this(catalogName, schemaName, viewName, null);
    }

    public DropViewStatement(String catalogName, String schemaName, String viewName, Boolean ifExists) {
        this.catalogName = catalogName;
        this.schemaName = schemaName;
        this.viewName = viewName;
        this.ifExists = ifExists;
    }

    public Boolean isIfExists() {
        return ifExists;
    }
}
