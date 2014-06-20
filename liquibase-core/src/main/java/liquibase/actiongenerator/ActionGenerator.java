package liquibase.actiongenerator;

import liquibase.action.Action;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.executor.ExecutionOptions;
import liquibase.servicelocator.PrioritizedService;
import liquibase.statement.SqlStatement;

public interface ActionGenerator <StatementType extends SqlStatement> extends PrioritizedService {

    /**
     * Does this generator support the given statement/database combination? Do not validate the statement with this method, only return if it <i>can</i> suppot it.
     */
    public boolean supports(StatementType statement, ExecutionOptions options);

    /**
     * Validate the data contained in the SqlStatement.  If there are no errors, return an empty ValidationErrors object, not a null value.
     * Liquibase will inspect the ValidationErrors result before attempting to call generateSql.
     */
    public ValidationErrors validate(StatementType statement, ExecutionOptions options, ActionGeneratorChain chain);

    public Warnings warn(StatementType statementType, ExecutionOptions options, ActionGeneratorChain chain);

    /**
     * Generate the actual Sql for the given statement and database.
     */
    public Action[] generateActions(StatementType statement, ExecutionOptions options, ActionGeneratorChain chain);

    /**
     * Does this change require access to the database metadata?  If true, the change cannot be used in an updateSql-style command.
     * @param options
     */
    public boolean generateStatementsIsVolatile(ExecutionOptions options);

    public boolean generateRollbackStatementsIsVolatile(ExecutionOptions options);

}
