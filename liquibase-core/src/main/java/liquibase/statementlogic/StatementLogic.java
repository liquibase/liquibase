package liquibase.statementlogic;

import liquibase.ExecutionEnvironment;
import liquibase.action.Action;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.servicelocator.PrioritizedService;
import liquibase.statement.SqlStatement;

public interface StatementLogic<StatementType extends SqlStatement> extends PrioritizedService {

    /**
     * Does this generator support the given statement/database combination? Do not validate the statement with this method, only return if it <i>can</i> suppot it.
     */
    public boolean supports(StatementType statement, ExecutionEnvironment env);

    /**
     * Validate the data contained in the SqlStatement.  If there are no errors, return an empty ValidationErrors object, not a null value.
     * Liquibase will inspect the ValidationErrors result before attempting to call generateSql.
     */
    public ValidationErrors validate(StatementType statement, ExecutionEnvironment env, StatementLogicChain chain);

    public Warnings warn(StatementType statementType, ExecutionEnvironment env, StatementLogicChain chain);

    /**
     * Generate the actual Sql for the given statement and database.
     */
    public Action[] generateActions(StatementType statement, ExecutionEnvironment env, StatementLogicChain chain);

    /**
     * Does this change require access to the database metadata?  If true, the change cannot be used in an updateSql-style command.
     */
    public boolean generateStatementsIsVolatile(ExecutionEnvironment env);

    public boolean generateRollbackStatementsIsVolatile(ExecutionEnvironment env);

}
