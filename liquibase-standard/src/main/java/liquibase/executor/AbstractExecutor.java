package liquibase.executor;

import liquibase.Scope;
import liquibase.change.AbstractSQLChange;
import liquibase.change.Change;
import liquibase.change.core.RawSQLChange;
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
    protected static final long TIMEOUT_MILLIS=2000;//2 sec timeout

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
                /*
        for function-> liquibase.executor.AbstractExecutor.execute(liquibase.change.Change, java.util.List<liquibase.sql.visitor.SqlVisitor>)
        1)sql statements are derived from the change and executed one by one,
        2)here we will maintain a timer and if the time passes , the timeout exception is thrown
        3)while handling exceptions externally in the calling class, database.rollback() is triggered, hence partial
         changes of a changeset will be rolled back


         1)we can add a accessible KeyStore in a scope which is cleaned up after exiting scope, just like MDCObject map.
         2)we can use this tag feature to add a tag in caller function(where we call this function specifically for triggering change-set while executing changelog).
         3)If this function is called internally via different functionalities, we only want a timeout in a specific liquibase command(eg update),then gis tag feature can help us.
         4)For now , we make an assumption (which is true for now) that this function is called only when changeset has to be executed and nowhere else.
         5)Timeout statically declared, but we will move that in the parameter parsing.
         6)the timeout will be approximately applied since the execution happens in main thread only, and if the per-statement function i.e. execute(statement, sqlVisitors);
         is running, while timeout passes, then we will wait till this execution completes, and timeout will happen when the time check is visited again.
         This can be mitigated in two ways
         a)Use multithreading, and whenever a statement is executed, we trigger it in one thread and the other thread checks the timer, there is a way to cancel
         PreparedStatement in this manner, and it can give us better resolution, but complex changes
         b)add a statement timeout(query level timeout= Ts), so even if the slowest query is causing the transaction timeout to occur, the timeout will be delayed by atmost Ts time
         c)close the connection when timeout happens

         */
        long startTime=System.currentTimeMillis();
        SqlStatement[] sqlStatements = change.generateStatements(database);
        if (sqlStatements != null) {
            for (SqlStatement statement : sqlStatements) {
                if (statement.skipOnUnsupported() && !SqlGeneratorFactory.getInstance().supports(statement, database)) {
                    continue;
                }
                if (change instanceof RawSQLChange) {
                    Scope.getCurrentScope().getLog(getClass()).fine("Executing Statement: " +
                            System.lineSeparator() + ((RawSQLChange)change).getSql());
                } else {
                    Scope.getCurrentScope().getLog(getClass()).fine("Executing Statement: " +
                            System.lineSeparator() + statement);
                }
                try {
                    if(System.currentTimeMillis()-startTime>TIMEOUT_MILLIS)
                        throw new DatabaseException("Timeout Exception");
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
