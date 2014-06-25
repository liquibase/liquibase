package liquibase.statementlogic;

import liquibase.ExecutionEnvironment;
import liquibase.action.Action;
import liquibase.exception.ValidationErrors;
import liquibase.exception.Warnings;
import liquibase.servicelocator.PrioritizedService;
import liquibase.statement.Statement;

/**
 * StatementLogic implementations are used to convert {@link liquibase.statement.Statement} objects into {@link liquibase.action.Action} objects which can be executed.
 * To do the actual conversion, {@link liquibase.statementlogic.StatementLogicFactory} collects all the StatementLogic implementations, sorts them by priority, then executes them with a {@link liquibase.statementlogic.StatementLogicChain}.
 * <p>
 * StatementLogic implementations MUST have a no-arg constructor.
 */
public interface StatementLogic<StatementType extends Statement> extends PrioritizedService {

    /**
     * Returns true if this generator support the given statement/database combination. Do not validate the statement with this method, only return if it <i>can</i> support it.
     */
    public boolean supports(StatementType statement, ExecutionEnvironment env);

    /**
     * Validate the data contained in the Statement.  If there are no errors, return an empty ValidationErrors object, not a null value.
     * Liquibase will inspect the ValidationErrors result before attempting to call {@link #generateActions(liquibase.statement.Statement, liquibase.ExecutionEnvironment, StatementLogicChain)}
     * and will not execute the logic if validationErrors is not empty.
     * <p>
     * This method uses a chain pattern to allow multiple StatementLogic implementations to work together.
     * This implementation should call {@link liquibase.statementlogic.StatementLogicChain#validate(liquibase.statement.Statement, liquibase.ExecutionEnvironment)} similar to how it
     * calls chain.generateActions in {@link #generateActions(liquibase.statement.Statement, liquibase.ExecutionEnvironment, StatementLogicChain)}
     *
     * @see #warn(liquibase.statement.Statement, liquibase.ExecutionEnvironment, StatementLogicChain)
     */
    public ValidationErrors validate(StatementType statement, ExecutionEnvironment env, StatementLogicChain chain);

    /**
     * Generate warnings based on the data contained in the Statement. If there are no errors, return an empty Warnings object, not a null value.
     * Liquibase will inspect the Warnings result before attempting to call {@link #generateActions(liquibase.statement.Statement, liquibase.ExecutionEnvironment, StatementLogicChain)}
     * but <b>will still</b> execute the logic, even if the warnings is not empty. This differs from validate() because Liquibase simply outputs warnings in the log and continues rather
     * than stopping execution.
     * <p>
     * This method uses a chain pattern to allow multiple StatementLogic implementations to work together.
     * This implementation should call {@link liquibase.statementlogic.StatementLogicChain#warn(liquibase.statement.Statement, liquibase.ExecutionEnvironment)} similar to how it
     * calls chain.generateActions in {@link #generateActions(liquibase.statement.Statement, liquibase.ExecutionEnvironment, StatementLogicChain)}
     *
     * @see #validate(liquibase.statement.Statement, liquibase.ExecutionEnvironment, StatementLogicChain)
     */
    public Warnings warn(StatementType statementType, ExecutionEnvironment env, StatementLogicChain chain);

    /**
     * Generate the actual Action instances for the given statement and environment. This method is called by {@link liquibase.statementlogic.StatementLogicFactory} in reverse order based on the {@link #getPriority()} result.
     * <p>
     * This method uses a chain pattern to allow multiple StatementLogic implementations to work together. If this implementation wants to modify add an additional Action, it should call {@link liquibase.statementlogic.StatementLogicChain#generateActions(liquibase.statement.Statement, liquibase.ExecutionEnvironment)} and then modify the returned result.
     * If this method wants to block the lower-priority methods from ever executing, do not call chain.generateActions.
     * If you want to continue the chain logic but selectively block a lower-priority StatementLogic from executing, call {@link liquibase.statementlogic.StatementLogicChain#block(Class)}
     */
    public Action[] generateActions(StatementType statement, ExecutionEnvironment env, StatementLogicChain chain);

    /**
     * Returns true if this StatementLogic implementation <b>may</b> require interacting with the outside environment in {@link #generateActions(liquibase.statement.Statement, liquibase.ExecutionEnvironment, StatementLogicChain)}.
     *
     * For example, the implementation needs to read all available tables, or query existing data to create the Action instances, this should return true.
     * If the implementation returns different Action objects depending on the time of day, this should return true.
     */
    public boolean generateActionsIsVolatile(ExecutionEnvironment env);

}
