/**
 *
 * This is an example of a custom Executor class.
 * Specifying the change set attribute "runWith=<executor name>"
 * instructs Liquibase to execute the changes in the change set
 * with the Executor.
 *
 */
package liquibase.executor.jvm;

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
import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;

/**
 *
 * This is an example of a custom <a href="#{@link}">{@link Executor}</a> implemention which can be specified
 * in a changelog with the "runWith" attribute
 *
 */
public class CustomExecutor extends JdbcExecutor {
    private Logger log = LogService.getLog(getClass());
    private static ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");
    protected static final String MSG_UNABLE_TO_VALIDATE_ROLLBACK_CHANGE = coreBundle.getString("unable.to.validate.rollback.change");
    protected static final String MSG_UNABLE_TO_VALIDATE_CHANGE_SET = coreBundle.getString("unable.to.validate.changeset");

    /**
     *
     * Constructor
     *
     */
    public CustomExecutor() {
        log.info("Constructed a CustomExecutor");
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
            validateChange(changeSet, validationErrors, change, MSG_UNABLE_TO_VALIDATE_CHANGE_SET);
        }
        if (changeSet.getRollback() != null) {
            RollbackContainer container = changeSet.getRollback();
            List<Change> rollbackChanges = container.getChanges();
            for (Change change : rollbackChanges) {
                log.info("Validating rollback change " + change.getDescription());
                validateChange(changeSet, validationErrors, change, MSG_UNABLE_TO_VALIDATE_ROLLBACK_CHANGE);
            }
        }
        return validationErrors;
    }

    private void validateChange(ChangeSet changeSet,
                                ValidationErrors validationErrors,
                                Change change,
                                String msgUnableToValidateChange) {
        log.info("Validating change " + change.getDescription());
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
        log.info("Executing with the '" + getName() + "' executor");
        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(action, database);
        try {
            for (Sql sql : sqls) {
                String actualSqlString = sql.toSql();
                for (SqlVisitor visitor : sqlVisitors) {
                    visitor.modifySql(actualSqlString, database);
                }
                log.info("Generated SQL for change is " + actualSqlString);
            }
        }
        catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}
