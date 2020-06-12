/*
 *
 * This is an example of a custom Executor class.
 * Specifying the change set attribute "runWith=<executor name>"
 * instructs Liquibase to execute the changes in the change set
 * with the Executor.
 *
 */
package liquibase.executor.jvm;

import liquibase.Scope;
import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RollbackContainer;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import liquibase.executor.Executor;
import liquibase.logging.LogService;
import liquibase.logging.Logger;
import liquibase.servicelocator.PrioritizedService;
import liquibase.sql.Sql;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;

import java.util.List;

/**
 *
 * This is an example of a custom <a href="#{@link}">{@link Executor}</a> implemention which can be specified
 * in a changelog with the "runWith" attribute
 *
 */
public class ExampleExecutor extends JdbcExecutor {

    /**
     *
     * Constructor
     *
     */
    public ExampleExecutor() {
        Scope.getCurrentScope().getLog(getClass()).info("Constructed an ExampleExecutor");
    }

    /**
     *
     * Return the name of the Executor
     *
     * @return String   The Executor name
     *
     */
    @Override
    public String getName() {
        return "example";
    }

    /**
     *
     * Return the Executor priority
     *
     * @return int      The Executor priority
     *
     */
    @Override
    public int getPriority() {
        return PrioritizedService.PRIORITY_DEFAULT + 100;
    }

    /**
     *
     * Validate whether the change set can be executed by this Executor
     *
     * @param   changeSet The change set to validate
     * @return  boolean   True if all changes can be executed by the custom Executor
     *                    False if any change cannot be executed
     *
     */
    @Override
    public ValidationErrors validate(ChangeSet changeSet) {
        //
        // If no runWith setting then just go back
        //
        if (changeSet.getRunWith() == null || changeSet.getRunWith().isEmpty()) {
            return new ValidationErrors();
        }
        //
        // License check
        //
        ValidationErrors validationErrors = new ValidationErrors();

        //
        // Verify for go-forward and rollback changes
        //
        List<Change> changes = changeSet.getChanges();
        for (Change change : changes) {
            validateChange(changeSet, validationErrors, change, "");
        }
        if (changeSet.getRollback() != null) {
            RollbackContainer container = changeSet.getRollback();
            List<Change> rollbackChanges = container.getChanges();
            for (Change change : rollbackChanges) {
                Scope.getCurrentScope().getLog(getClass()).info("Validating rollback change " + change.getDescription());
                validateChange(changeSet, validationErrors, change, "rollback");
            }
        }
        return validationErrors;
    }

    private void validateChange(ChangeSet changeSet,
                                ValidationErrors validationErrors,
                                Change change,
                                String type) {
        Scope.getCurrentScope().getLog(getClass()).info("Validating " + type + " change " + change.getDescription());
    }

    /**
     *
     * Execute the SQL from the <a href="#{@link}">{@link SqlStatement}</a> parameter
     *
     * @param  action                   This is the SqlStatement object which contains
     *                                  the SQL to execute
     * @param  sqlVisitors              List of <a href="#{@link}">{@link SqlVisitor}</a> to apply to the generated SQL
     * @throws DatabaseException        Exception type thrown if an error occurs during execution
     *
     */
    @Override
    public void execute(SqlStatement action, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        Scope.getCurrentScope().getLog(getClass()).info("Executing with the '" + getName() + "' executor");
        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(action, database);
        try {
            for (Sql sql : sqls) {
                String actualSqlString = sql.toSql();
                for (SqlVisitor visitor : sqlVisitors) {
                    visitor.modifySql(actualSqlString, database);
                }
                Scope.getCurrentScope().getLog(getClass()).info("Generated SQL for change is " + actualSqlString);
            }
        }
        catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}
