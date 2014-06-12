package liquibase.actiongenerator;

import liquibase.action.Action;
import liquibase.database.Database;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.servicelocator.PrioritizedService;
import liquibase.statement.SqlStatement;

public interface ActionGenerator <StatementType extends SqlStatement> extends PrioritizedService {

    /**
     * Does this generator support the given statement/database combination? Do not validate the statement with this method, only return if it <i>can</i> suppot it.
     */
    public boolean supports(StatementType statement, Database database);

    /**
     * Validate the data contained in the SqlStatement.  If there are no errors, return an empty ValidationErrors object, not a null value.
     * Liquibase will inspect the ValidationErrors result before attempting to call generateSql.
     */
    public ValidationErrors validate(StatementType statement, Database database, ActionGeneratorChain chain);

    public Warnings warn(StatementType statementType, Database database, ActionGeneratorChain chain);

    /**
     * Generate the actual Sql for the given statement and database.
     */
    public Action[] generateActions(StatementType statement, Database database, ActionGeneratorChain chain);
}
