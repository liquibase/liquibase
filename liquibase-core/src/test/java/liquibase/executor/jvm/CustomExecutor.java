
package liquibase.executor.jvm;

import liquibase.change.AbstractSQLChange;
import liquibase.change.Change;
import liquibase.change.core.RawSQLChange;
import liquibase.change.core.SQLFileChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RollbackContainer;
import liquibase.database.DatabaseConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import liquibase.license.LicenseServiceUtils;
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

public class CustomExecutor extends JdbcExecutor {
    private Logger log = LogService.getLog(getClass());
    private int timeout = 1800;
    private ChangeSet changeSet;
    private static ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");
    protected static final String MSG_UNABLE_TO_VALIDATE_ROLLBACK_CHANGE = coreBundle.getString("unable.to.validate.rollback.change");
    protected static final String MSG_UNABLE_TO_VALIDATE_CHANGE_SET = coreBundle.getString("unable.to.validate.changeset");
    protected static final String MSG_UNABLE_TO_VALIDATE_LICENSE = coreBundle.getString("no.executor.pro.license.found");
    protected static final String MSG_SPLIT_STATEMENTS_NOT_SET = coreBundle.getString("split.statements.not.set");

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
        return "custom";
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
     * Validate if the change set can be executed by this Executor
     *
     * @param   changeSet The change set to validate
     * @return  boolean   True if all changes can be executed by sqlplus else false
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
        // This Executor only support SQLFileChange and RawSQLChange types
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
        this.changeSet = changeSet;
        return validationErrors;
    }

    private void validateChange(ChangeSet changeSet,
                                ValidationErrors validationErrors,
                                Change change,
                                String msgUnableToValidateChange) {
        log.info("Validating change " + change.getDescription());
    }

    @Override
    public void execute(SqlStatement action, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        log.info("Executing with the '" + getName() + "' executor");
        Sql[] sqls = SqlGeneratorFactory.getInstance().generateSql(action, database);
        DatabaseConnection con = database.getConnection();
        try {
            for (Sql sql : sqls) {
                String actualSqlString = sql.toSql();
                log.info("Generated SQL for change is " + actualSqlString);
            }
            //
            // Run -v first
            //
            // SqlPlusRunner runner = new SqlPlusRunner(changeSet, null, resourceAccessor);
            // runner.addArg("-v");
            // runner.executeCommand(database);
            // log.info("Successfully validated 'sqlplus'");

            //
            // Now execute the script
            //
            // runner = new SqlPlusRunner(changeSet, sqls, resourceAccessor);
            // runner.executeCommand(database);
        }
        catch (Exception e) {
            throw new DatabaseException(e);
        }
    }
}
