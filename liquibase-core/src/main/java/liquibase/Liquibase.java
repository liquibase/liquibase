package liquibase;

import liquibase.change.CheckSum;
import liquibase.change.core.RawSQLChange;
import liquibase.changelog.*;
import liquibase.changelog.filter.*;
import liquibase.changelog.visitor.*;
import liquibase.command.CommandExecutionException;
import liquibase.command.CommandFactory;
import liquibase.command.core.DropAllCommand;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.core.MSSQLDatabase;
import liquibase.database.core.OracleDatabase;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.lockservice.DatabaseChangeLogLock;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.logging.Logger;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.statement.core.RawSqlStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Catalog;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.text.DateFormat;
import java.util.*;

import static java.util.ResourceBundle.getBundle;

/**
 * Primary facade class for interacting with Liquibase.
 * The built in command line, Ant, Maven and other ways of running Liquibase are wrappers around methods in this class.
 */
public class Liquibase implements AutoCloseable {

    private static final Logger LOG = LogService.getLog(Liquibase.class);
    protected static final int CHANGESET_ID_NUM_PARTS = 3;
    protected static final int CHANGESET_ID_AUTHOR_PART = 2;
    protected static final int CHANGESET_ID_CHANGESET_PART = 1;
    protected static final int CHANGESET_ID_CHANGELOG_PART = 0;
    private static ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");
    protected static final String MSG_COULD_NOT_RELEASE_LOCK = coreBundle.getString("could.not.release.lock");

    protected Database database;
    private DatabaseChangeLog databaseChangeLog;
    private String changeLogFile;
    private ResourceAccessor resourceAccessor;
    private ChangeLogParameters changeLogParameters;
    private ChangeExecListener changeExecListener;
    private ChangeLogSyncListener changeLogSyncListener;

    private boolean ignoreClasspathPrefix = true;

    /**
     * Creates a Liquibase instance for a given DatabaseConnection. The Database instance used will be found with {@link DatabaseFactory#findCorrectDatabaseImplementation(liquibase.database.DatabaseConnection)}
     *
     * @see DatabaseConnection
     * @see Database
     * @see #Liquibase(String, liquibase.resource.ResourceAccessor, liquibase.database.Database)
     * @see ResourceAccessor
     */
    public Liquibase(String changeLogFile, ResourceAccessor resourceAccessor, DatabaseConnection conn)
        throws LiquibaseException {
        this(changeLogFile, resourceAccessor, DatabaseFactory.getInstance().findCorrectDatabaseImplementation(conn));
    }

    /**
     * Creates a Liquibase instance. The changeLogFile parameter must be a path that can be resolved by the passed
     * ResourceAccessor. If windows style path separators are used for the changeLogFile, they will be standardized to
     * unix style for better cross-system compatibility.
     *
     * @see DatabaseConnection
     * @see Database
     * @see ResourceAccessor
     */
    public Liquibase(String changeLogFile, ResourceAccessor resourceAccessor, Database database) {
        if (changeLogFile != null) {
            // Convert to STANDARD / if using absolute path on windows:
            this.changeLogFile = changeLogFile.replace('\\', '/');
        }

        this.resourceAccessor = resourceAccessor;
        this.changeLogParameters = new ChangeLogParameters(database);
        this.database = database;
    }

    public Liquibase(DatabaseChangeLog changeLog, ResourceAccessor resourceAccessor, Database database) {
        this.databaseChangeLog = changeLog;

        if (changeLog != null) {
            this.changeLogFile = changeLog.getPhysicalFilePath();
        }
        if (this.changeLogFile != null) {
            // Convert to STANDARD "/" if using an absolute path on Windows:
            changeLogFile = changeLogFile.replace('\\', '/');
        }
        this.resourceAccessor = resourceAccessor;
        this.database = database;
        this.changeLogParameters = new ChangeLogParameters(database);
    }

    /**
     * Return the change log file used by this Liquibase instance.
     */
    public String getChangeLogFile() {
        return changeLogFile;
    }

    /**
     * Return the log used by this Liquibase instance.
     */
    public Logger getLog() {
        return LOG;
    }

    /**
     * Returns the ChangeLogParameters container used by this Liquibase instance.
     */
    public ChangeLogParameters getChangeLogParameters() {
        return changeLogParameters;
    }

    /**
     * Returns the Database used by this Liquibase instance.
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * Return ResourceAccessor used by this Liquibase instance.
     */
    public ResourceAccessor getResourceAccessor() {
        return resourceAccessor;
    }

    /**
     * Convience method for {@link #update(Contexts)} that constructs the Context object from the passed string.
     */
    public void update(String contexts) throws LiquibaseException {
        this.update(new Contexts(contexts));
    }
    /**
     * Executes Liquibase "update" logic which ensures that the configured {@link Database} is up to date according to
     * the configured changelog file. To run in "no context mode", pass a null or empty context object.
     */
    public void update(Contexts contexts) throws LiquibaseException {
        update(contexts, new LabelExpression());
    }

    public void update(Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        update(contexts, labelExpression, true);
    }
    public void update(Contexts contexts, LabelExpression labelExpression, boolean checkLiquibaseTables)
        throws LiquibaseException {
        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.waitForLock();

        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        try {
            DatabaseChangeLog changeLog = getDatabaseChangeLog();
            
            if (checkLiquibaseTables) {
                checkLiquibaseTables(true, changeLog, contexts, labelExpression);
            }

            ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).generateDeploymentId();

            changeLog.validate(database, contexts, labelExpression);

            ChangeLogIterator changeLogIterator = getStandardChangelogIterator(contexts, labelExpression, changeLog);

            changeLogIterator.run(createUpdateVisitor(), new RuntimeEnvironment(database, contexts, labelExpression));
        } finally {
            database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
            try {
                lockService.releaseLock();
            } catch (LockException e) {
                LOG.severe(LogType.LOG, MSG_COULD_NOT_RELEASE_LOCK, e);
            }
            resetServices();
        }
    }

    public DatabaseChangeLog getDatabaseChangeLog() throws LiquibaseException {
        if (databaseChangeLog == null) {
            ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor);
            databaseChangeLog = parser.parse(changeLogFile, changeLogParameters, resourceAccessor);
        }

        return databaseChangeLog;
    }


    protected UpdateVisitor createUpdateVisitor() {
        return new UpdateVisitor(database, changeExecListener);
    }

    protected RollbackVisitor createRollbackVisitor() {
        return new RollbackVisitor(database, changeExecListener);
    }

    protected ChangeLogIterator getStandardChangelogIterator(Contexts contexts, LabelExpression labelExpression,
                                                             DatabaseChangeLog changeLog) throws DatabaseException {
        return new ChangeLogIterator(changeLog,
                new ShouldRunChangeSetFilter(database, ignoreClasspathPrefix),
                new ContextChangeSetFilter(contexts),
                new LabelChangeSetFilter(labelExpression),
                new DbmsChangeSetFilter(database),
                new IgnoreChangeSetFilter());
    }

    public void update(String contexts, Writer output) throws LiquibaseException {
        this.update(new Contexts(contexts), output);
    }

    public void update(Contexts contexts, Writer output) throws LiquibaseException {
        update(contexts, new LabelExpression(), output);
    }

    public void update(Contexts contexts, LabelExpression labelExpression, Writer output) throws LiquibaseException {
        update(contexts, labelExpression, output, true);
    }
    
    public void update(Contexts contexts, LabelExpression labelExpression, Writer output, boolean checkLiquibaseTables)
            throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        /* We have no other choice than to save the current Executer here. */
        @SuppressWarnings("squid:S1941")
        Executor oldTemplate = ExecutorService.getInstance().getExecutor(database);
        LoggingExecutor loggingExecutor = new LoggingExecutor(
            ExecutorService.getInstance().getExecutor(database), output, database
        );
        ExecutorService.getInstance().setExecutor(database, loggingExecutor);

        outputHeader("Update Database Script");

        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.waitForLock();

        try {

            update(contexts, labelExpression, checkLiquibaseTables);

            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }

        ExecutorService.getInstance().setExecutor(database, oldTemplate);
    }

    public void update(int changesToApply, String contexts) throws LiquibaseException {
        update(changesToApply, new Contexts(contexts), new LabelExpression());
    }

    public void update(int changesToApply, Contexts contexts, LabelExpression labelExpression)
        throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.waitForLock();

        try {

            DatabaseChangeLog changeLog = getDatabaseChangeLog();

            checkLiquibaseTables(true, changeLog, contexts, labelExpression);
            ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).generateDeploymentId();

            changeLog.validate(database, contexts, labelExpression);

            ChangeLogIterator logIterator = new ChangeLogIterator(changeLog,
                    new ShouldRunChangeSetFilter(database, ignoreClasspathPrefix),
                    new ContextChangeSetFilter(contexts),
                    new LabelChangeSetFilter(labelExpression),
                    new DbmsChangeSetFilter(database),
                    new IgnoreChangeSetFilter(),
                    new CountChangeSetFilter(changesToApply));

            logIterator.run(createUpdateVisitor(), new RuntimeEnvironment(database, contexts, labelExpression));
        } finally {
            try {
                lockService.releaseLock();
            } catch (LockException e) {
                LOG.severe(LogType.LOG, MSG_COULD_NOT_RELEASE_LOCK, e);
            }
            resetServices();
        }
    }

    public void update(String tag, String contexts) throws LiquibaseException {
        update(tag, new Contexts(contexts), new LabelExpression());
    }

    public void update(String tag, Contexts contexts) throws LiquibaseException {
        update(tag, contexts, new LabelExpression());
    }

    public void update(String tag, Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        if (tag == null) {
            update(contexts, labelExpression);
            return;
        }
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.waitForLock();

        try {

            DatabaseChangeLog changeLog = getDatabaseChangeLog();

            checkLiquibaseTables(true, changeLog, contexts, labelExpression);
            changeLog.validate(database, contexts, labelExpression);

            List<RanChangeSet> ranChangeSetList = database.getRanChangeSetList();
            ChangeLogIterator logIterator = new ChangeLogIterator(changeLog,
                    new ShouldRunChangeSetFilter(database, ignoreClasspathPrefix),
                    new ContextChangeSetFilter(contexts),
                    new LabelChangeSetFilter(labelExpression),
                    new DbmsChangeSetFilter(database),
                    new IgnoreChangeSetFilter(),
                    new UpToTagChangeSetFilter(tag, ranChangeSetList));

            logIterator.run(createUpdateVisitor(), new RuntimeEnvironment(database, contexts, labelExpression));
        } finally {
            try {
                lockService.releaseLock();
            } catch (LockException e) {
                LOG.severe(LogType.LOG, MSG_COULD_NOT_RELEASE_LOCK, e);
            }
            resetServices();
        }
    }

    public void update(int changesToApply, String contexts, Writer output) throws LiquibaseException {
        this.update(changesToApply, new Contexts(contexts), new LabelExpression(), output);
    }

    public void update(int changesToApply, Contexts contexts, LabelExpression labelExpression, Writer output)
        throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        /* We have no other choice than to save the current Executer here. */
        @SuppressWarnings("squid:S1941")
        Executor oldTemplate = ExecutorService.getInstance().getExecutor(database);
        LoggingExecutor loggingExecutor = new LoggingExecutor(
            ExecutorService.getInstance().getExecutor(database), output, database
        );
        ExecutorService.getInstance().setExecutor(database, loggingExecutor);

        outputHeader("Update " + changesToApply + " Change Sets Database Script");

        update(changesToApply, contexts, labelExpression);

        try {
            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }

        resetServices();
        ExecutorService.getInstance().setExecutor(database, oldTemplate);
    }

    public void update(String tag, String contexts, Writer output) throws LiquibaseException {
        update(tag, new Contexts(contexts), new LabelExpression(), output);
    }

    public void update(String tag, Contexts contexts, Writer output) throws LiquibaseException {
        update(tag, contexts, new LabelExpression(), output);
    }

    public void update(String tag, Contexts contexts, LabelExpression labelExpression, Writer output)
        throws LiquibaseException {
        if (tag == null) {
            update(contexts, labelExpression, output);
            return;
        }
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        /* We have no other choice than to save the current Executer here. */
        @SuppressWarnings("squid:S1941")
        Executor oldTemplate = ExecutorService.getInstance().getExecutor(database);
        LoggingExecutor loggingExecutor = new LoggingExecutor(
            ExecutorService.getInstance().getExecutor(database), output, database
        );
        ExecutorService.getInstance().setExecutor(database, loggingExecutor);

        outputHeader("Update to '" + tag + "' Database Script");

        update(tag, contexts, labelExpression);

        try {
            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }

        resetServices();
        ExecutorService.getInstance().setExecutor(database, oldTemplate);
    }

    private void outputHeader(String message) throws DatabaseException {
        Executor executor = ExecutorService.getInstance().getExecutor(database);
        executor.comment("*********************************************************************");
        executor.comment(message);
        executor.comment("*********************************************************************");
        executor.comment("Change Log: " + changeLogFile);
        executor.comment("Ran at: " +
            DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date())
        );
        DatabaseConnection connection = getDatabase().getConnection();
        if (connection != null) {
            executor.comment("Against: " + connection.getConnectionUserName() + "@" + connection.getURL());
        }
        executor.comment("Liquibase version: " + LiquibaseUtil.getBuildVersion());
        executor.comment("*********************************************************************" +
            StreamUtil.getLineSeparator()
        );

        if (database instanceof OracleDatabase) {
            executor.execute(new RawSqlStatement("SET DEFINE OFF;"));
        }
        if ((database instanceof MSSQLDatabase) && (database.getDefaultCatalogName() != null)) {
            executor.execute(new RawSqlStatement("USE " +
                database.escapeObjectName(database.getDefaultCatalogName(), Catalog.class) + ";")
            );
        }
    }

    public void rollback(int changesToRollback, String contexts, Writer output) throws LiquibaseException {
        rollback(changesToRollback, null, contexts, output);
    }

    public void rollback(int changesToRollback, Contexts contexts, Writer output) throws LiquibaseException {
        rollback(changesToRollback, null, contexts, output);
    }

    public void rollback(int changesToRollback, Contexts contexts, LabelExpression labelExpression, Writer output)
        throws LiquibaseException {
        rollback(changesToRollback, null, contexts, labelExpression, output);
    }

    public void rollback(int changesToRollback, String rollbackScript, String contexts, Writer output)
        throws LiquibaseException {
        rollback(changesToRollback, rollbackScript, new Contexts(contexts), output);
    }

    public void rollback(int changesToRollback, String rollbackScript, Contexts contexts, Writer output)
        throws LiquibaseException {
        rollback(changesToRollback, rollbackScript, contexts, new LabelExpression(), output);
    }
    public void rollback(int changesToRollback, String rollbackScript, Contexts contexts,
                         LabelExpression labelExpression, Writer output) throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        /* We have no other choice than to save the current Executer here. */
        @SuppressWarnings("squid:S1941")
        Executor oldTemplate = ExecutorService.getInstance().getExecutor(database);
        ExecutorService.getInstance().setExecutor(database,
            new LoggingExecutor(ExecutorService.getInstance().getExecutor(database), output, database)
        );

        outputHeader("Rollback " + changesToRollback + " Change(s) Script");

        rollback(changesToRollback, rollbackScript, contexts, labelExpression);

        try {
            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }
        ExecutorService.getInstance().setExecutor(database, oldTemplate);
        resetServices();
    }

    public void rollback(int changesToRollback, String contexts) throws LiquibaseException {
        rollback(changesToRollback, null, contexts);
    }

    public void rollback(int changesToRollback, Contexts contexts, LabelExpression labelExpression)
        throws LiquibaseException {
        rollback(changesToRollback, null, contexts, labelExpression);
    }

    public void rollback(int changesToRollback, String rollbackScript, String contexts) throws LiquibaseException {
        rollback(changesToRollback, rollbackScript, new Contexts(contexts), new LabelExpression());
    }

    public void rollback(int changesToRollback, String rollbackScript, Contexts contexts,
                         LabelExpression labelExpression) throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.waitForLock();

        try {
            DatabaseChangeLog changeLog = getDatabaseChangeLog();
            checkLiquibaseTables(false, changeLog, contexts, labelExpression);

            changeLog.validate(database, contexts, labelExpression);
            changeLog.setIgnoreClasspathPrefix(ignoreClasspathPrefix);

            ChangeLogIterator logIterator = new ChangeLogIterator(database.getRanChangeSetList(), changeLog,
                    new AlreadyRanChangeSetFilter(database.getRanChangeSetList(), ignoreClasspathPrefix),
                    new ContextChangeSetFilter(contexts),
                    new LabelChangeSetFilter(labelExpression),
                    new DbmsChangeSetFilter(database),
                    new IgnoreChangeSetFilter(),
                    new CountChangeSetFilter(changesToRollback));

            if (rollbackScript == null) {
                logIterator.run(createRollbackVisitor(), new RuntimeEnvironment(database, contexts, labelExpression));
            }
            else {
                executeRollbackScript(rollbackScript, contexts, labelExpression);
                removeRunStatus(logIterator, contexts, labelExpression);
            }
        } finally {
            try {
                lockService.releaseLock();
            } catch (LockException e) {
                LOG.severe(LogType.LOG, "Error releasing lock", e);
            }
            resetServices();
        }
    }

    protected void removeRunStatus(ChangeLogIterator logIterator, Contexts contexts, LabelExpression labelExpression)
        throws LiquibaseException {
        logIterator.run(new ChangeSetVisitor() {
            @Override
            public Direction getDirection() {
                return Direction.REVERSE;
            }

            @Override
            public void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database,
                              Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
                database.removeRanStatus(changeSet);
                database.commit();
            }
        }, new RuntimeEnvironment(database, contexts, labelExpression));
    }

    protected void executeRollbackScript(String rollbackScript, Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        final Executor executor = ExecutorService.getInstance().getExecutor(database);
        String rollbackScriptContents;
        try {
            Set<InputStream> streams = resourceAccessor.getResourcesAsStream(rollbackScript);
            if ((streams == null) || streams.isEmpty()) {
                throw new LiquibaseException("Cannot find rollbackScript "+rollbackScript);
            } else if (streams.size() > 1) {
                throw new LiquibaseException("Found multiple rollbackScripts named "+rollbackScript);
            }
            rollbackScriptContents = StreamUtil.getStreamContents(streams.iterator().next());
        } catch (IOException e) {
            throw new LiquibaseException("Error reading rollbackScript "+executor+": "+e.getMessage());
        }

        //
        // Expand changelog properties
        //
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);
        DatabaseChangeLog changelog = getDatabaseChangeLog();
        rollbackScriptContents = changeLogParameters.expandExpressions(rollbackScriptContents, changelog);

        RawSQLChange rollbackChange = buildRawSQLChange(rollbackScriptContents);

        try {
            executor.execute(rollbackChange);
        } catch (DatabaseException e) {
            DatabaseException ex = new DatabaseException(
                "Error executing rollback script. ChangeSets will still be marked as rolled back: " + e.getMessage(),
                e
            );
            LogService.getLog(getClass()).severe(LogType.LOG, ex.getMessage());
            LOG.severe(LogType.LOG, "Error executing rollback script", ex);
            if (changeExecListener != null) {
                changeExecListener.runFailed(null, databaseChangeLog, database, ex);
            }
        }
        database.commit();
    }

    protected RawSQLChange buildRawSQLChange(String rollbackScriptContents) {
        RawSQLChange rollbackChange = new RawSQLChange(rollbackScriptContents);
        rollbackChange.setSplitStatements(true);
        rollbackChange.setStripComments(true);
        return rollbackChange;
    }

    public void rollback(String tagToRollBackTo, String contexts, Writer output) throws LiquibaseException {
        rollback(tagToRollBackTo, null, contexts, output);
    }

    public void rollback(String tagToRollBackTo, Contexts contexts, Writer output) throws LiquibaseException {
        rollback(tagToRollBackTo, null, contexts, output);
    }

    public void rollback(String tagToRollBackTo, Contexts contexts, LabelExpression labelExpression, Writer output)
        throws LiquibaseException {
        rollback(tagToRollBackTo, null, contexts, labelExpression, output);
    }

    public void rollback(String tagToRollBackTo, String rollbackScript, String contexts, Writer output)
        throws LiquibaseException {
        rollback(tagToRollBackTo, rollbackScript, new Contexts(contexts), output);
    }

    public void rollback(String tagToRollBackTo, String rollbackScript, Contexts contexts, Writer output)
        throws LiquibaseException {
        rollback(tagToRollBackTo, rollbackScript, contexts, new LabelExpression(), output);
    }

    public void rollback(String tagToRollBackTo, String rollbackScript, Contexts contexts,
                         LabelExpression labelExpression, Writer output) throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        /* We have no other choice than to save the current Executor here. */
        @SuppressWarnings("squid:S1941")
        Executor oldTemplate = ExecutorService.getInstance().getExecutor(database);
        ExecutorService.getInstance().setExecutor(database, new LoggingExecutor(
            ExecutorService.getInstance().getExecutor(database), output, database)
        );

        outputHeader("Rollback to '" + tagToRollBackTo + "' Script");

        rollback(tagToRollBackTo, contexts, labelExpression);

        try {
            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }
        ExecutorService.getInstance().setExecutor(database, oldTemplate);
        resetServices();
    }

    public void rollback(String tagToRollBackTo, String contexts) throws LiquibaseException {
        rollback(tagToRollBackTo, null, contexts);
    }

    public void rollback(String tagToRollBackTo, Contexts contexts) throws LiquibaseException {
        rollback(tagToRollBackTo, null, contexts);
    }

    public void rollback(String tagToRollBackTo, Contexts contexts, LabelExpression labelExpression)
        throws LiquibaseException {
        rollback(tagToRollBackTo, null, contexts, labelExpression);
    }

    public void rollback(String tagToRollBackTo, String rollbackScript, String contexts) throws LiquibaseException {
        rollback(tagToRollBackTo, rollbackScript, new Contexts(contexts));
    }

    public void rollback(String tagToRollBackTo, String rollbackScript, Contexts contexts) throws LiquibaseException {
        rollback(tagToRollBackTo, rollbackScript, contexts, new LabelExpression());
    }
    public void rollback(String tagToRollBackTo, String rollbackScript, Contexts contexts,
                         LabelExpression labelExpression) throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.waitForLock();

        try {

            DatabaseChangeLog changeLog = getDatabaseChangeLog();
            checkLiquibaseTables(false, changeLog, contexts, labelExpression);

            changeLog.validate(database, contexts, labelExpression);
            changeLog.setIgnoreClasspathPrefix(ignoreClasspathPrefix);

            List<RanChangeSet> ranChangeSetList = database.getRanChangeSetList();
            ChangeLogIterator logIterator = new ChangeLogIterator(ranChangeSetList, changeLog,
                    new AfterTagChangeSetFilter(tagToRollBackTo, ranChangeSetList),
                    new AlreadyRanChangeSetFilter(ranChangeSetList, ignoreClasspathPrefix),
                    new ContextChangeSetFilter(contexts),
                    new LabelChangeSetFilter(labelExpression),
                    new IgnoreChangeSetFilter(),
                    new DbmsChangeSetFilter(database));

            if (rollbackScript == null) {
                logIterator.run(createRollbackVisitor(),
                    new RuntimeEnvironment(database, contexts, labelExpression));
            } else {
                executeRollbackScript(rollbackScript, contexts, labelExpression);
                removeRunStatus(logIterator, contexts, labelExpression);
            }
        } finally {
            try {
                lockService.releaseLock();
            } catch (LockException e) {
                LOG.severe(LogType.LOG, MSG_COULD_NOT_RELEASE_LOCK, e);
            }
        }
        resetServices();
    }

    public void rollback(Date dateToRollBackTo, String contexts, Writer output) throws LiquibaseException {
        rollback(dateToRollBackTo, null, contexts, output);
    }

    public void rollback(Date dateToRollBackTo, String rollbackScript, String contexts, Writer output)
        throws LiquibaseException {
        rollback(dateToRollBackTo, new Contexts(contexts), new LabelExpression(), output);
    }
    public void rollback(Date dateToRollBackTo, Contexts contexts, LabelExpression labelExpression, Writer output)
        throws LiquibaseException {
        rollback(dateToRollBackTo, null, contexts, labelExpression, output);
    }

    public void rollback(Date dateToRollBackTo, String rollbackScript, Contexts contexts,
                         LabelExpression labelExpression, Writer output) throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        /* We have no other choice than to save the current Executer here. */
        @SuppressWarnings("squid:S1941")
        Executor oldTemplate = ExecutorService.getInstance().getExecutor(database);
        ExecutorService.getInstance().setExecutor(database,
            new LoggingExecutor(ExecutorService.getInstance().getExecutor(database), output, database));

        outputHeader("Rollback to " + dateToRollBackTo + " Script");

        rollback(dateToRollBackTo, contexts, labelExpression);

        try {
            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }
        ExecutorService.getInstance().setExecutor(database, oldTemplate);
        resetServices();
    }

    public void rollback(Date dateToRollBackTo, String contexts) throws LiquibaseException {
        rollback(dateToRollBackTo, null, contexts);
    }

    public void rollback(Date dateToRollBackTo, Contexts contexts,  LabelExpression labelExpression)
        throws LiquibaseException {
        rollback(dateToRollBackTo, null, contexts, labelExpression);
    }

    public void rollback(Date dateToRollBackTo, String rollbackScript, String contexts) throws LiquibaseException {
        rollback(dateToRollBackTo, new Contexts(contexts), new LabelExpression());
    }

    public void rollback(Date dateToRollBackTo, String rollbackScript, Contexts contexts,
                         LabelExpression labelExpression) throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.waitForLock();

        try {
            DatabaseChangeLog changeLog = getDatabaseChangeLog();
            checkLiquibaseTables(false, changeLog, contexts, labelExpression);
            changeLog.validate(database, contexts, labelExpression);
            changeLog.setIgnoreClasspathPrefix(ignoreClasspathPrefix);

            List<RanChangeSet> ranChangeSetList = database.getRanChangeSetList();
            ChangeLogIterator logIterator = new ChangeLogIterator(ranChangeSetList, changeLog,
                    new ExecutedAfterChangeSetFilter(dateToRollBackTo, ranChangeSetList),
                    new AlreadyRanChangeSetFilter(ranChangeSetList, ignoreClasspathPrefix),
                    new ContextChangeSetFilter(contexts),
                    new LabelChangeSetFilter(labelExpression),
                    new IgnoreChangeSetFilter(),
                    new DbmsChangeSetFilter(database));

            if (rollbackScript == null) {
                logIterator.run(createRollbackVisitor(),
                    new RuntimeEnvironment(database, contexts, labelExpression));
            } else {
                executeRollbackScript(rollbackScript, contexts, labelExpression);
                removeRunStatus(logIterator, contexts, labelExpression);
            }
        } finally {
            try {
                lockService.releaseLock();
            } catch (LockException e) {
                LOG.severe(LogType.LOG, MSG_COULD_NOT_RELEASE_LOCK, e);
            }
        }
        resetServices();
    }

    public void changeLogSync(String contexts, Writer output) throws LiquibaseException {
        changeLogSync(new Contexts(contexts), new LabelExpression(), output);
    }

    public void changeLogSync(Contexts contexts, LabelExpression labelExpression, Writer output)
        throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        LoggingExecutor outputTemplate = new LoggingExecutor(
            ExecutorService.getInstance().getExecutor(database), output, database
        );

        /* We have no other choice than to save the current Executer here. */
        @SuppressWarnings("squid:S1941")
        Executor oldTemplate = ExecutorService.getInstance().getExecutor(database);
        ExecutorService.getInstance().setExecutor(database, outputTemplate);

        outputHeader("SQL to add all changesets to database history table");

        changeLogSync(contexts, labelExpression);

        try {
            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }

        ExecutorService.getInstance().setExecutor(database, oldTemplate);
        resetServices();
    }

    public void changeLogSync(String contexts) throws LiquibaseException {
        changeLogSync(new Contexts(contexts), new LabelExpression());
    }

    /**
     * @deprecated use version with LabelExpression
     */
    @Deprecated
    public void changeLogSync(Contexts contexts) throws LiquibaseException {
        changeLogSync(contexts, new LabelExpression());
    }

    public void changeLogSync(Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.waitForLock();

        try {
            DatabaseChangeLog changeLog = getDatabaseChangeLog();
            checkLiquibaseTables(true, changeLog, contexts, labelExpression);
            ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).generateDeploymentId();

            changeLog.validate(database, contexts, labelExpression);

            ChangeLogIterator logIterator = new ChangeLogIterator(changeLog,
                    new NotRanChangeSetFilter(database.getRanChangeSetList()),
                    new ContextChangeSetFilter(contexts),
                    new LabelChangeSetFilter(labelExpression),
                    new IgnoreChangeSetFilter(),
                    new DbmsChangeSetFilter(database));

            logIterator.run(new ChangeLogSyncVisitor(database, changeLogSyncListener),
                new RuntimeEnvironment(database, contexts, labelExpression)
            );
        } finally {
            try {
                lockService.releaseLock();
            } catch (LockException e) {
                LOG.severe(LogType.LOG, MSG_COULD_NOT_RELEASE_LOCK, e);
            }
            resetServices();
        }
    }

    public void markNextChangeSetRan(String contexts, Writer output) throws LiquibaseException {
        markNextChangeSetRan(new Contexts(contexts), new LabelExpression(), output);
    }

    public void markNextChangeSetRan(Contexts contexts, LabelExpression labelExpression, Writer output)
        throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);


        LoggingExecutor outputTemplate = new LoggingExecutor(
            ExecutorService.getInstance().getExecutor(database), output, database
        );

        /* We have no other choice than to save the current Executer here. */
        @SuppressWarnings("squid:S1941")
        Executor oldTemplate = ExecutorService.getInstance().getExecutor(database);
        ExecutorService.getInstance().setExecutor(database, outputTemplate);

        outputHeader("SQL to add all changesets to database history table");

        markNextChangeSetRan(contexts, labelExpression);

        try {
            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }

        ExecutorService.getInstance().setExecutor(database, oldTemplate);
        resetServices();
    }

    public void markNextChangeSetRan(String contexts) throws LiquibaseException {
        markNextChangeSetRan(new Contexts(contexts), new LabelExpression());
    }

    public void markNextChangeSetRan(Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.waitForLock();

        try {
            DatabaseChangeLog changeLog = getDatabaseChangeLog();
            ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).generateDeploymentId();

            checkLiquibaseTables(false, changeLog, contexts, labelExpression);
            changeLog.validate(database, contexts, labelExpression);

            ChangeLogIterator logIterator = new ChangeLogIterator(changeLog,
                    new NotRanChangeSetFilter(database.getRanChangeSetList()),
                    new ContextChangeSetFilter(contexts),
                    new LabelChangeSetFilter(labelExpression),
                    new DbmsChangeSetFilter(database),
                    new IgnoreChangeSetFilter(),
                    new CountChangeSetFilter(1));

            logIterator.run(new ChangeLogSyncVisitor(database),
                new RuntimeEnvironment(database, contexts, labelExpression)
            );
        } finally {
            try {
                lockService.releaseLock();
            } catch (LockException e) {
                LOG.severe(LogType.LOG, MSG_COULD_NOT_RELEASE_LOCK, e);
            }
            resetServices();
        }
    }

    public void futureRollbackSQL(String contexts, Writer output) throws LiquibaseException {
        futureRollbackSQL(null, contexts, output, true);
    }
    
    public void futureRollbackSQL(Writer output) throws LiquibaseException {
        futureRollbackSQL(null, null, new Contexts(), new LabelExpression(), output);
    }

    public void futureRollbackSQL(String contexts, Writer output, boolean checkLiquibaseTables) 
           throws LiquibaseException {
        futureRollbackSQL(null, contexts, output, checkLiquibaseTables);
    }

    public void futureRollbackSQL(Integer count, String contexts, Writer output) throws LiquibaseException {
        futureRollbackSQL(count, new Contexts(contexts), new LabelExpression(), output, true);
    }
    
    public void futureRollbackSQL(Contexts contexts, LabelExpression labelExpression, Writer output)
        throws LiquibaseException {
        futureRollbackSQL(null, null, contexts, labelExpression, output);
    }

    public void futureRollbackSQL(Integer count, String contexts, Writer output, boolean checkLiquibaseTables) 
           throws LiquibaseException {
        futureRollbackSQL(count, new Contexts(contexts), new LabelExpression(), output, checkLiquibaseTables);
    }

    public void futureRollbackSQL(Integer count, Contexts contexts, LabelExpression labelExpression, Writer output)
        throws LiquibaseException {
        futureRollbackSQL(count, contexts, labelExpression, output, true);
    }

    public void futureRollbackSQL(Integer count, Contexts contexts, LabelExpression labelExpression, Writer output,
                                  boolean checkLiquibaseTables) throws LiquibaseException {
        futureRollbackSQL(count, null, contexts, labelExpression, output);
    }

    public void futureRollbackSQL(String tag, Contexts contexts, LabelExpression labelExpression, Writer output)
        throws LiquibaseException {
        futureRollbackSQL(null, tag, contexts, labelExpression, output);
    }

    protected void futureRollbackSQL(Integer count, String tag, Contexts contexts, LabelExpression labelExpression,
                                     Writer output) throws LiquibaseException {
        futureRollbackSQL(count, tag, contexts, labelExpression, output, true);
    }

    protected void futureRollbackSQL(Integer count, String tag, Contexts contexts, LabelExpression labelExpression,
                                     Writer output, boolean checkLiquibaseTables) throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        LoggingExecutor outputTemplate = new LoggingExecutor(ExecutorService.getInstance().getExecutor(database),
            output, database);
        Executor oldTemplate = ExecutorService.getInstance().getExecutor(database);
        ExecutorService.getInstance().setExecutor(database, outputTemplate);

        outputHeader("SQL to roll back currently unexecuted changes");

        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.waitForLock();

        try {
            DatabaseChangeLog changeLog = getDatabaseChangeLog();
            if (checkLiquibaseTables) {
                checkLiquibaseTables(false, changeLog, contexts, labelExpression);
            }
            ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).generateDeploymentId();

            changeLog.validate(database, contexts, labelExpression);

            ChangeLogIterator logIterator;
            if ((count == null) && (tag == null)) {
                logIterator = new ChangeLogIterator(changeLog,
                        new NotRanChangeSetFilter(database.getRanChangeSetList()),
                        new ContextChangeSetFilter(contexts),
                        new LabelChangeSetFilter(labelExpression),
                        new IgnoreChangeSetFilter(),
                        new DbmsChangeSetFilter(database));
            } else if (count != null) {
                ChangeLogIterator forwardIterator = new ChangeLogIterator(changeLog,
                        new NotRanChangeSetFilter(database.getRanChangeSetList()),
                        new ContextChangeSetFilter(contexts),
                        new LabelChangeSetFilter(labelExpression),
                        new DbmsChangeSetFilter(database),
                        new IgnoreChangeSetFilter(),
                        new CountChangeSetFilter(count));
                final ListVisitor listVisitor = new ListVisitor();
                forwardIterator.run(listVisitor, new RuntimeEnvironment(database, contexts, labelExpression));

                logIterator = new ChangeLogIterator(changeLog,
                        new NotRanChangeSetFilter(database.getRanChangeSetList()),
                        new ContextChangeSetFilter(contexts),
                        new LabelChangeSetFilter(labelExpression),
                        new DbmsChangeSetFilter(database),
                        new IgnoreChangeSetFilter(),
                        new ChangeSetFilter() {
                            @Override
                            public ChangeSetFilterResult accepts(ChangeSet changeSet) {
                                return new ChangeSetFilterResult(
                                    listVisitor.getSeenChangeSets().contains(changeSet), null, null
                                );
                            }
                        });
            } else {
                List<RanChangeSet> ranChangeSetList = database.getRanChangeSetList();
                ChangeLogIterator forwardIterator = new ChangeLogIterator(changeLog,
                        new NotRanChangeSetFilter(ranChangeSetList),
                        new ContextChangeSetFilter(contexts),
                        new LabelChangeSetFilter(labelExpression),
                        new DbmsChangeSetFilter(database),
                        new IgnoreChangeSetFilter(),
                        new UpToTagChangeSetFilter(tag, ranChangeSetList));
                final ListVisitor listVisitor = new ListVisitor();
                forwardIterator.run(listVisitor, new RuntimeEnvironment(database, contexts, labelExpression));

                logIterator = new ChangeLogIterator(changeLog,
                        new NotRanChangeSetFilter(ranChangeSetList),
                        new ContextChangeSetFilter(contexts),
                        new LabelChangeSetFilter(labelExpression),
                        new DbmsChangeSetFilter(database),
                        new IgnoreChangeSetFilter(),
                        new ChangeSetFilter() {
                            @Override
                            public ChangeSetFilterResult accepts(ChangeSet changeSet) {
                                return new ChangeSetFilterResult(
                                    listVisitor.getSeenChangeSets().contains(changeSet), null, null
                                );
                            }
                        });
            }

            logIterator.run(createRollbackVisitor(),
                new RuntimeEnvironment(database, contexts, labelExpression)
            );
        } finally {
            try {
                lockService.releaseLock();
            } catch (LockException e) {
                LOG.severe(LogType.LOG, MSG_COULD_NOT_RELEASE_LOCK, e);
            }
            ExecutorService.getInstance().setExecutor(database, oldTemplate);
            resetServices();
        }

        try {
            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }

    }

    protected void resetServices() {
        LockServiceFactory.getInstance().resetAll();
        ChangeLogHistoryServiceFactory.getInstance().resetAll();
        ExecutorService.getInstance().reset();
    }

    /**
     * Drops all database objects in the default schema.
     */
    public final void dropAll() throws DatabaseException {
        dropAll(new CatalogAndSchema(getDatabase().getDefaultCatalogName(), getDatabase().getDefaultSchemaName()));
    }

    /**
     * Drops all database objects in the passed schema(s).
     */
    public final void dropAll(CatalogAndSchema... schemas) throws DatabaseException {
        if ((schemas == null) || (schemas.length == 0)) {
            schemas = new CatalogAndSchema[] {
                new CatalogAndSchema(getDatabase().getDefaultCatalogName(), getDatabase().getDefaultSchemaName())
            };
        }

        DropAllCommand dropAll = (DropAllCommand) CommandFactory.getInstance().getCommand("dropAll");
        dropAll.setDatabase(this.getDatabase());
        dropAll.setSchemas(schemas);

        try {
            dropAll.execute();
        } catch (CommandExecutionException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * 'Tags' the database for future rollback
     */
    public void tag(String tagString) throws LiquibaseException {
        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.waitForLock();

        try {
            ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).generateDeploymentId();

            checkLiquibaseTables(false, null, new Contexts(),
                new LabelExpression());
            getDatabase().tag(tagString);
        } finally {
            try {
                lockService.releaseLock();
            } catch (LockException e) {
                LOG.severe(LogType.LOG, MSG_COULD_NOT_RELEASE_LOCK, e);
            }
        }
    }

    public boolean tagExists(String tagString) throws LiquibaseException {
        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.waitForLock();

        try {
            checkLiquibaseTables(false, null, new Contexts(),
                new LabelExpression());
            return getDatabase().doesTagExist(tagString);
        } finally {
            try {
                lockService.releaseLock();
            } catch (LockException e) {
                LOG.severe(LogType.LOG, MSG_COULD_NOT_RELEASE_LOCK, e);
            }
        }
    }

    public void updateTestingRollback(String contexts) throws LiquibaseException {
        updateTestingRollback(new Contexts(contexts), new LabelExpression());
    }

    public void updateTestingRollback(Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        updateTestingRollback(null, contexts, labelExpression);

    }
    public void updateTestingRollback(String tag, Contexts contexts, LabelExpression labelExpression)
        throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        Date baseDate = new Date();
        update(tag, contexts, labelExpression);
        rollback(baseDate, null, contexts, labelExpression);
        update(tag, contexts, labelExpression);
    }

    public void checkLiquibaseTables(boolean updateExistingNullChecksums, DatabaseChangeLog databaseChangeLog,
                                     Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        ChangeLogHistoryService changeLogHistoryService =
            ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(getDatabase());
        changeLogHistoryService.init();
        if (updateExistingNullChecksums) {
            changeLogHistoryService.upgradeChecksums(databaseChangeLog, contexts, labelExpression);
        }
        LockServiceFactory.getInstance().getLockService(getDatabase()).init();
    }

    /**
     * Returns true if it is "save" to migrate the database.
     * Currently, "safe" is defined as running in an output-sql mode or against a database on localhost.
     * It is fine to run Liquibase against a "non-safe" database, the method is mainly used to determine if the user
     * should be prompted before continuing.
     */
    public boolean isSafeToRunUpdate() throws DatabaseException {
        return getDatabase().isSafeToRunUpdate();
    }

    /**
     * Display change log lock information.
     */
    public DatabaseChangeLogLock[] listLocks() throws LiquibaseException {
        checkLiquibaseTables(false, null, new Contexts(), new LabelExpression());

        return LockServiceFactory.getInstance().getLockService(database).listLocks();
    }

    public void reportLocks(PrintStream out) throws LiquibaseException {
        DatabaseChangeLogLock[] locks = listLocks();
        out.println("Database change log locks for " + getDatabase().getConnection().getConnectionUserName()
            + "@" + getDatabase().getConnection().getURL());
        if (locks.length == 0) {
            out.println(" - No locks");
        }
        for (DatabaseChangeLogLock lock : locks) {
            out.println(" - " + lock.getLockedBy() + " at " +
                DateFormat.getDateTimeInstance().format(lock.getLockGranted()));
        }

    }

    public void forceReleaseLocks() throws LiquibaseException {
        checkLiquibaseTables(false, null, new Contexts(), new LabelExpression());

        LockServiceFactory.getInstance().getLockService(database).forceReleaseLock();
    }

    /**
     * @deprecated use version with LabelExpression
     */
    @Deprecated
    public List<ChangeSet> listUnrunChangeSets(Contexts contexts) throws LiquibaseException {
        return listUnrunChangeSets(contexts, new LabelExpression());
    }

    public List<ChangeSet> listUnrunChangeSets(Contexts contexts, LabelExpression labels) throws LiquibaseException {
        return listUnrunChangeSets(contexts, labels, true);
    }

    public List<ChangeSet> listUnrunChangeSets(Contexts contexts, LabelExpression labels, boolean checkLiquibaseTables) throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labels);

        DatabaseChangeLog changeLog = getDatabaseChangeLog();

        if (checkLiquibaseTables) {
            checkLiquibaseTables(true, changeLog, contexts, labels);
        }

        changeLog.validate(database, contexts, labels);

        ChangeLogIterator logIterator = getStandardChangelogIterator(contexts, labels, changeLog);

        ListVisitor visitor = new ListVisitor();
        logIterator.run(visitor, new RuntimeEnvironment(database, contexts, labels));
        return visitor.getSeenChangeSets();
    }

    /**
     * @deprecated use version with LabelExpression
     */
    @Deprecated
    public List<ChangeSetStatus> getChangeSetStatuses(Contexts contexts) throws LiquibaseException {
        return getChangeSetStatuses(contexts, new LabelExpression());
    }

    public List<ChangeSetStatus> getChangeSetStatuses(Contexts contexts, LabelExpression labelExpression)
        throws LiquibaseException {
        return getChangeSetStatuses(contexts, labelExpression, true);
    }
        /**
         * Returns the ChangeSetStatuses of all changesets in the change log file and history in the order they
         * would be ran.
         */
    public List<ChangeSetStatus> getChangeSetStatuses(Contexts contexts, LabelExpression labelExpression,
                                                      boolean checkLiquibaseTables) throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        DatabaseChangeLog changeLog = getDatabaseChangeLog();

        if (checkLiquibaseTables) {
            checkLiquibaseTables(true, changeLog, contexts, labelExpression);
        }

        changeLog.validate(database, contexts, labelExpression);

        ChangeLogIterator logIterator = getStandardChangelogIterator(contexts, labelExpression, changeLog);

        StatusVisitor visitor = new StatusVisitor(database);
        logIterator.run(visitor, new RuntimeEnvironment(database, contexts, labelExpression));
        return visitor.getStatuses();
    }

    public void reportStatus(boolean verbose, String contexts, Writer out) throws LiquibaseException {
        reportStatus(verbose, new Contexts(contexts), new LabelExpression(), out);
    }

    public void reportStatus(boolean verbose, Contexts contexts, Writer out) throws LiquibaseException {
        reportStatus(verbose, contexts, new LabelExpression(), out);
    }

    public void reportStatus(boolean verbose, Contexts contexts, LabelExpression labels, Writer out)
        throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labels);

        try {
            List<ChangeSet> unrunChangeSets = listUnrunChangeSets(contexts, labels, false);
            if (unrunChangeSets.isEmpty()) {
                out.append(getDatabase().getConnection().getConnectionUserName());
                out.append("@");
                out.append(getDatabase().getConnection().getURL());
                out.append(" is up to date");
                out.append(StreamUtil.getLineSeparator());
            } else {
                out.append(String.valueOf(unrunChangeSets.size()));
                out.append(" change sets have not been applied to ");
                out.append(getDatabase().getConnection().getConnectionUserName());
                out.append("@");
                out.append(getDatabase().getConnection().getURL());
                out.append(StreamUtil.getLineSeparator());
                if (verbose) {
                    for (ChangeSet changeSet : unrunChangeSets) {
                        out.append("     ").append(changeSet.toString(false))
                            .append(StreamUtil.getLineSeparator());
                    }
                }
            }

            out.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }

    }

    public Collection<RanChangeSet> listUnexpectedChangeSets(String contexts) throws LiquibaseException {
        return listUnexpectedChangeSets(new Contexts(contexts), new LabelExpression());
    }

    public Collection<RanChangeSet> listUnexpectedChangeSets(Contexts contexts, LabelExpression labelExpression)
        throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        DatabaseChangeLog changeLog = getDatabaseChangeLog();
        changeLog.validate(database, contexts, labelExpression);

        ChangeLogIterator logIterator = new ChangeLogIterator(changeLog,
                new ContextChangeSetFilter(contexts),
                new LabelChangeSetFilter(labelExpression),
                new DbmsChangeSetFilter(database),
                new IgnoreChangeSetFilter());
        ExpectedChangesVisitor visitor = new ExpectedChangesVisitor(database.getRanChangeSetList());
        logIterator.run(visitor, new RuntimeEnvironment(database, contexts, labelExpression));
        return visitor.getUnexpectedChangeSets();
    }


    public void reportUnexpectedChangeSets(boolean verbose, String contexts, Writer out) throws LiquibaseException {
        reportUnexpectedChangeSets(verbose, new Contexts(contexts), new LabelExpression(), out);
    }

    public void reportUnexpectedChangeSets(boolean verbose, Contexts contexts, LabelExpression labelExpression,
                                           Writer out) throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        try {
            Collection<RanChangeSet> unexpectedChangeSets = listUnexpectedChangeSets(contexts, labelExpression);
            if (unexpectedChangeSets.isEmpty()) {
                out.append(getDatabase().getConnection().getConnectionUserName());
                out.append("@");
                out.append(getDatabase().getConnection().getURL());
                out.append(" contains no unexpected changes!");
                out.append(StreamUtil.getLineSeparator());
            } else {
                out.append(String.valueOf(unexpectedChangeSets.size()));
                out.append(" unexpected changes were found in ");
                out.append(getDatabase().getConnection().getConnectionUserName());
                out.append("@");
                out.append(getDatabase().getConnection().getURL());
                out.append(StreamUtil.getLineSeparator());
                if (verbose) {
                    for (RanChangeSet ranChangeSet : unexpectedChangeSets) {
                        out.append("     ").append(ranChangeSet.toString()).append(StreamUtil.getLineSeparator());
                    }
                }
            }

            out.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }

    }

    /**
     * Sets checksums to null so they will be repopulated next run
     */
    public void clearCheckSums() throws LiquibaseException {
        LOG.info(LogType.LOG, "Clearing database change log checksums");
        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.waitForLock();

        try {
            checkLiquibaseTables(false, null, new Contexts(), new LabelExpression());

            UpdateStatement updateStatement = new UpdateStatement(
                getDatabase().getLiquibaseCatalogName(),
                getDatabase().getLiquibaseSchemaName(),
                getDatabase().getDatabaseChangeLogTableName()
            );
            updateStatement.addNewColumnValue("MD5SUM", null);
            ExecutorService.getInstance().getExecutor(database).execute(updateStatement);
            getDatabase().commit();
        } finally {
            try {
                lockService.releaseLock();
            } catch (LockException e) {
                LOG.severe(LogType.LOG, MSG_COULD_NOT_RELEASE_LOCK, e);
            }
        }
        resetServices();
    }

    public final CheckSum calculateCheckSum(final String changeSetIdentifier) throws LiquibaseException {
        if (changeSetIdentifier == null) {
            throw new LiquibaseException(new IllegalArgumentException("changeSetIdentifier"));
        }
        final List<String> parts = StringUtils.splitAndTrim(changeSetIdentifier, "::");
        if ((parts == null) || (parts.size() < CHANGESET_ID_NUM_PARTS)) {
            throw new LiquibaseException(
                new IllegalArgumentException("Invalid changeSet identifier: " + changeSetIdentifier)
            );
        }
        return this.calculateCheckSum(parts.get(CHANGESET_ID_CHANGELOG_PART),
            parts.get(CHANGESET_ID_CHANGESET_PART), parts.get(CHANGESET_ID_AUTHOR_PART));
    }

    public CheckSum calculateCheckSum(final String filename, final String id, final String author)
        throws LiquibaseException {
        LOG.info(LogType.LOG, String.format("Calculating checksum for changeset %s::%s::%s", filename, id, author));
        final ChangeLogParameters clParameters = this.getChangeLogParameters();
        final ResourceAccessor resourceAccessor = this.getResourceAccessor();
        final DatabaseChangeLog changeLog =
            ChangeLogParserFactory.getInstance().getParser(
                this.changeLogFile, resourceAccessor
            ).parse(this.changeLogFile, clParameters, resourceAccessor);

        // TODO: validate?

        final ChangeSet changeSet = changeLog.getChangeSet(filename, author, id);
        if (changeSet == null) {
            throw new LiquibaseException(
                new IllegalArgumentException("No such changeSet: " + filename + "::" + id + "::" + author)
            );
        }

        return changeSet.generateCheckSum();
    }

    public void generateDocumentation(String outputDirectory) throws LiquibaseException {
        // call without context
        generateDocumentation(outputDirectory, new Contexts(), new LabelExpression());
    }

    public void generateDocumentation(String outputDirectory, String contexts) throws LiquibaseException {
        generateDocumentation(outputDirectory, new Contexts(contexts), new LabelExpression());
    }

    public void generateDocumentation(String outputDirectory, Contexts contexts,
                                      LabelExpression labelExpression) throws LiquibaseException {
        LOG.info(LogType.LOG, "Generating Database Documentation");
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);
        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.waitForLock();

        try {
            DatabaseChangeLog changeLog = getDatabaseChangeLog();
            checkLiquibaseTables(false, changeLog, new Contexts(), new LabelExpression());

            changeLog.validate(database, contexts, labelExpression);

            ChangeLogIterator logIterator = new ChangeLogIterator(changeLog,
                    new DbmsChangeSetFilter(database));

            DBDocVisitor visitor = new DBDocVisitor(database);
            logIterator.run(visitor, new RuntimeEnvironment(database, contexts, labelExpression));

            visitor.writeHTML(new File(outputDirectory), resourceAccessor);
        } catch (IOException e) {
            throw new LiquibaseException(e);
        } finally {
            try {
                lockService.releaseLock();
            } catch (LockException e) {
                LOG.severe(LogType.LOG, MSG_COULD_NOT_RELEASE_LOCK, e);
            }
        }
    }

    public DiffResult diff(Database referenceDatabase, Database targetDatabase, CompareControl compareControl)
        throws LiquibaseException {
        return DiffGeneratorFactory.getInstance().compare(referenceDatabase, targetDatabase, compareControl);
    }

    /**
     * Checks changelogs for bad MD5Sums and preconditions before attempting a migration
     */
    public void validate() throws LiquibaseException {

        DatabaseChangeLog changeLog = getDatabaseChangeLog();
        changeLog.validate(database);
    }

    public void setChangeLogParameter(String key, Object value) {
        this.changeLogParameters.set(key, value);
    }

    /**
     * Add safe database properties as changelog parameters.<br/>
     * Safe properties are the ones that doesn't have side effects in liquibase state and also don't change in during the liquibase execution
     * @param database Database which propeties are put in the changelog
     * @throws DatabaseException
     */
    private void setDatabasePropertiesAsChangelogParameters(Database database) throws DatabaseException {            
            setChangeLogParameter("database.autoIncrementClause", database.getAutoIncrementClause(null, null, null, null));
            setChangeLogParameter("database.currentDateTimeFunction", database.getCurrentDateTimeFunction());
            setChangeLogParameter("database.databaseChangeLogLockTableName", database.getDatabaseChangeLogLockTableName());
            setChangeLogParameter("database.databaseChangeLogTableName", database.getDatabaseChangeLogTableName());
            setChangeLogParameter("database.databaseMajorVersion", database.getDatabaseMajorVersion());
            setChangeLogParameter("database.databaseMinorVersion", database.getDatabaseMinorVersion());
            setChangeLogParameter("database.databaseProductName", database.getDatabaseProductName());
            setChangeLogParameter("database.databaseProductVersion", database.getDatabaseProductVersion());
            setChangeLogParameter("database.defaultCatalogName", database.getDefaultCatalogName());
            setChangeLogParameter("database.defaultSchemaName", database.getDefaultSchemaName());
            setChangeLogParameter("database.defaultSchemaNamePrefix", StringUtils.trimToNull(database.getDefaultSchemaName())==null?"":"."+database.getDefaultSchemaName());
            setChangeLogParameter("database.lineComment", database.getLineComment());
            setChangeLogParameter("database.liquibaseSchemaName", database.getLiquibaseSchemaName());
            setChangeLogParameter("database.liquibaseTablespaceName", database.getLiquibaseTablespaceName());
            setChangeLogParameter("database.typeName", database.getShortName());
            setChangeLogParameter("database.isSafeToRunUpdate", database.isSafeToRunUpdate());
            setChangeLogParameter("database.requiresPassword", database.requiresPassword());
            setChangeLogParameter("database.requiresUsername", database.requiresUsername());
            setChangeLogParameter("database.supportsForeignKeyDisable", database.supportsForeignKeyDisable());
            setChangeLogParameter("database.supportsInitiallyDeferrableColumns", database.supportsInitiallyDeferrableColumns());
            setChangeLogParameter("database.supportsRestrictForeignKeys", database.supportsRestrictForeignKeys());
            setChangeLogParameter("database.supportsSchemas", database.supportsSchemas());
            setChangeLogParameter("database.supportsSequences", database.supportsSequences());
            setChangeLogParameter("database.supportsTablespaces", database.supportsTablespaces());
    }

    private LockService getLockService() {
        return LockServiceFactory.getInstance().getLockService(database);
    }

    public void setChangeExecListener(ChangeExecListener listener) {
        this.changeExecListener = listener;
    }

    public void setChangeLogSyncListener(ChangeLogSyncListener changeLogSyncListener) {
        this.changeLogSyncListener = changeLogSyncListener;
    }

    public boolean isIgnoreClasspathPrefix() {
        return ignoreClasspathPrefix;
    }

    public void setIgnoreClasspathPrefix(boolean ignoreClasspathPrefix) {
        this.ignoreClasspathPrefix = ignoreClasspathPrefix;
    }

    @SafeVarargs
    public final void generateChangeLog(CatalogAndSchema catalogAndSchema, DiffToChangeLog changeLogWriter,
                                  PrintStream outputStream, Class<? extends DatabaseObject>... snapshotTypes)
        throws DatabaseException, IOException, ParserConfigurationException {
        generateChangeLog(catalogAndSchema, changeLogWriter, outputStream, null, snapshotTypes);
    }

    @SafeVarargs
    public final void generateChangeLog(CatalogAndSchema catalogAndSchema, DiffToChangeLog changeLogWriter,
                                  PrintStream outputStream, ChangeLogSerializer changeLogSerializer,
                                  Class<? extends DatabaseObject>... snapshotTypes)
        throws DatabaseException, IOException, ParserConfigurationException {

        Set<Class<? extends DatabaseObject>> finalCompareTypes = null;
        if ((snapshotTypes != null) && (snapshotTypes.length > 0)) {
            finalCompareTypes = new HashSet<>(Arrays.asList(snapshotTypes));
        }

        SnapshotControl snapshotControl = new SnapshotControl(this.getDatabase(), snapshotTypes);
        CompareControl compareControl = new CompareControl(new CompareControl.SchemaComparison[] {
            new CompareControl.SchemaComparison(catalogAndSchema, catalogAndSchema)
        }, finalCompareTypes);

        DatabaseSnapshot originalDatabaseSnapshot = null;
        try {
            originalDatabaseSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(
                compareControl.getSchemas(CompareControl.DatabaseRole.REFERENCE),
                getDatabase(),
                snapshotControl
            );

            DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(
                originalDatabaseSnapshot,
                SnapshotGeneratorFactory.getInstance().createSnapshot(
                    compareControl.getSchemas(CompareControl.DatabaseRole.REFERENCE),
                    null,
                    snapshotControl
                ),
                compareControl
            );

            changeLogWriter.setDiffResult(diffResult);

            if(changeLogSerializer != null) {
                changeLogWriter.print(outputStream, changeLogSerializer);
            } else {
                changeLogWriter.print(outputStream);
            }
        } catch (InvalidExampleException e) {
            throw new UnexpectedLiquibaseException(e);
        }
    }

    @Override
    public void close() throws Exception {
        if (database != null) {
            database.close();
        }
    }
}

