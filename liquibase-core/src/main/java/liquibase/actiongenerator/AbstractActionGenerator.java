package liquibase.actiongenerator;

import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.statement.SqlStatement;

public abstract class AbstractActionGenerator<StatementType extends SqlStatement> implements ActionGenerator<StatementType> {

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public boolean supports(StatementType statement, Database database) {
        return true;
    }

    @Override
    public ValidationErrors validate(StatementType statement, Database database, ActionGeneratorChain chain) {
        return chain.validate(statement, database);
    }

    @Override
    public Warnings warn(StatementType statementType, Database database, ActionGeneratorChain chain) {
        return chain.warn(statementType, database);
    }
}
