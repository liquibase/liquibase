package liquibase.statement.core;

import liquibase.changelog.definition.ChangeLogTableDefinition;
import liquibase.statement.AbstractSqlStatement;

public class CreateDatabaseChangeLogTableStatement extends AbstractSqlStatement {

    private final ChangeLogTableDefinition definition;

    public CreateDatabaseChangeLogTableStatement(ChangeLogTableDefinition definition) {
        this.definition = definition;
    }

    public ChangeLogTableDefinition getDefinition() {
        return definition;
    }
}
