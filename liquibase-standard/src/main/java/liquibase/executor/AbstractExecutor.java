package liquibase.executor;

import liquibase.Scope;
import liquibase.change.AbstractSQLChange;
import liquibase.change.Change;
import liquibase.changelog.ChangeSet;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;
import liquibase.sql.Sql;
import liquibase.sql.visitor.SqlVisitor;
import liquibase.sqlgenerator.SqlGeneratorFactory;
import liquibase.statement.SqlStatement;

import java.util.ArrayList;
import java.util.List;

/**
 * Code common to all Executor services / blueprint for Executor service classes.
 */
public abstract class AbstractExecutor implements Executor {
    protected Database database;
    protected ResourceAccessor resourceAccessor;

    /**
     *
     * Return the name of the Executor
     *
     * @return String   The Executor name
     *
     */
    @Override
    public abstract String getName();

    /**
     *
     * Return the Executor priority
     *
     * @return int      The Executor priority
     *
     */
    @Override
    public abstract int getPriority();

    /**
     *
     * Validate if the changeset can be executed by this Executor
     *
     * @param   changeSet The changeset to validate
     * @return  boolean   Always true for abstract class
     *
     */
    @Override
    public ValidationErrors validate(ChangeSet changeSet) {
        return new ValidationErrors();
    }

    /**
     *
     * Allow this Executor to make any needed changes to the changeset.
     * The base class sets splitStatements to 'true' if it is not set
     *
     * @param changeSet The changeset to operate on
     *
     */
    @Override
    public void modifyChangeSet(ChangeSet changeSet) {
        List<Change> changes = changeSet.getChanges();
        modifyChanges(changes);

        if (changeSet.getRollback() != null) {
            List<Change> rollbackChanges = changeSet.getRollback().getChanges();
            modifyChanges(rollbackChanges);
        }
    }

    private void modifyChanges(List<Change> changes) {
        for (Change change : changes) {
            if (change instanceof AbstractSQLChange) {
                AbstractSQLChange abstractSQLChange = (AbstractSQLChange)change;
                if (! abstractSQLChange.isSplitStatementsSet()) {
                    ((AbstractSQLChange) change).setSplitStatements(true);
                }
            }
        }
    }

    /**
     * Sets a {@code ResourceAccessor} object on this Executor to be used for file access.
     *
     * @param resourceAccessor the {@link ResourceAccessor} object to set
     */
    @Override
    public void setResourceAccessor(ResourceAccessor resourceAccessor) {
        this.resourceAccessor = resourceAccessor;
    }

    @Override
    public void setDatabase(Database database) {
        this.database = database;
    }

    protected String[] applyVisitors(SqlStatement statement, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        Sql[] sql = SqlGeneratorFactory.getInstance().generateSql(statement, database);
        if (sql == null) {
            return new String[0];
        }
        String[] returnSql = new String[sql.length];

        for (int i=0; i<sql.length; i++) {
            if (sql[i] == null) {
                continue;
            }
            returnSql[i] = sql[i].toSql();
            if (sqlVisitors != null) {
                for (SqlVisitor visitor : sqlVisitors) {
                    returnSql[i] = visitor.modifySql(returnSql[i], database);
                }
            }

        }
        return returnSql;
    }

    @Override
    public void execute(Change change) throws DatabaseException {
        execute(change, new ArrayList<>());
    }

    @Override
    public void execute(Change change, List<SqlVisitor> sqlVisitors) throws DatabaseException {
        SqlStatement[] sqlStatements = change.generateStatements(database);
        if (sqlStatements != null) {
            for (SqlStatement statement : sqlStatements) {
                if (statement.skipOnUnsupported() && !SqlGeneratorFactory.getInstance().supports(statement, database)) {
                    continue;
                }
                Scope.getCurrentScope().getLog(getClass()).fine("Executing Statement: " + statement);
                try {
                    execute(statement, sqlVisitors);
                } catch (DatabaseException e) {
                    if (statement.continueOnError()) {
                        Scope.getCurrentScope().getLog(getClass()).severe("Error executing statement '" + statement + "', but continuing", e);
                    } else {
                        throw e;
                    }
                }
            }
        }
    }

}
