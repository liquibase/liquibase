package liquibase;

import liquibase.change.CheckSum;
import liquibase.change.core.RawSQLChange;
import liquibase.changelog.*;
import liquibase.changelog.filter.*;
import liquibase.changelog.visitor.*;
import liquibase.command.CommandResults;
import liquibase.command.CommandScope;
import liquibase.command.core.*;
import liquibase.command.core.helpers.DatabaseChangelogCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionCommandStep;
import liquibase.command.core.helpers.PreCompareCommandStep;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.CommandExecutionException;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.hub.*;
import liquibase.hub.listener.HubChangeExecListener;
import liquibase.hub.model.Connection;
import liquibase.hub.model.HubChangeLog;
import liquibase.hub.model.Operation;
import liquibase.io.WriterOutputStream;
import liquibase.lockservice.DatabaseChangeLogLock;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.Logger;
import liquibase.logging.core.BufferedLogService;
import liquibase.logging.core.CompositeLogService;
import liquibase.logging.mdc.MdcKey;
import liquibase.logging.mdc.MdcObject;
import liquibase.logging.mdc.MdcValue;
import liquibase.logging.mdc.customobjects.ChangesetsRolledback;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.resource.PathHandlerFactory;
import liquibase.resource.Resource;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.structure.DatabaseObject;
import liquibase.util.*;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.text.DateFormat;
import java.util.*;
import java.util.function.Supplier;

import static java.util.ResourceBundle.getBundle;

/**
 * Primary facade class for interacting with Liquibase.
 * The built in command line, Ant, Maven and other ways of running Liquibase are wrappers around methods in this class.
 */
public class Liquibase implements AutoCloseable {

    private static final Logger LOG = Scope.getCurrentScope().getLog(Liquibase.class);
    private static final ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");
    public static final String MSG_COULD_NOT_RELEASE_LOCK = coreBundle.getString("could.not.release.lock");

    protected Database database;
    private DatabaseChangeLog databaseChangeLog;
    private String changeLogFile;
    private final ResourceAccessor resourceAccessor;
    private final ChangeLogParameters changeLogParameters;
    private ChangeExecListener changeExecListener;
    private ChangeLogSyncListener changeLogSyncListener;
    private final DefaultChangeExecListener defaultChangeExecListener = new DefaultChangeExecListener();
    private UUID hubConnectionId;
    private final Map<String, Boolean> upToDateFastCheck = new HashMap<>();

    private enum RollbackMessageType {
        WILL_ROLLBACK, ROLLED_BACK, ROLLBACK_FAILED
    }

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

    public UUID getHubConnectionId() {
        return hubConnectionId;
    }

    public void setHubConnectionId(UUID hubConnectionId) {
        this.hubConnectionId = hubConnectionId;
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
     * Convenience method for {@link #update(Contexts)} that runs in "no context mode".
     *
     * @see <a href="https://docs.liquibase.com/concepts/advanced/contexts.html" target="_top">contexts</a> in documentation
     */
    @Deprecated
    public void update() throws LiquibaseException {
        this.update(new Contexts());
    }

    /**
     * Convenience method for {@link #update(Contexts)} that constructs the Context object from the passed string.
     * To run in "no context mode", pass a null or empty "".
     *
     * @see <a href="https://docs.liquibase.com/concepts/advanced/contexts.html" target="_top">contexts</a> in documentation
     */
    @Deprecated
    public void update(String contexts) throws LiquibaseException {
        this.update(new Contexts(contexts));
    }

    /**
     * Executes Liquibase "update" logic which ensures that the configured {@link Database} is up to date according to
     * the configured changelog file. To run in "no context mode", pass a null or empty context object.
     *
     * @see <a href="https://docs.liquibase.com/concepts/advanced/contexts.html" target="_top">contexts</a> in documentation
     */
    @Deprecated
    public void update(Contexts contexts) throws LiquibaseException {
        update(contexts, new LabelExpression());
    }

    /**
     * Liquibase update
     *
     * @param contexts
     * @param labelExpression
     * @throws LiquibaseException
     *
     * @see <a href="https://docs.liquibase.com/concepts/advanced/contexts.html" target="_top">contexts</a> in documentation
     * @see <a href="https://docs.liquibase.com/concepts/advanced/labels.html" target="_top">labels</a> in documentation
     */
    @Deprecated
    public void update(Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        update(contexts, labelExpression, true);
    }

    /**
     * Liquibase update
     * @deprecated Use CommandStep
     * @param   contexts
     * @param   labelExpression
     * @param   checkLiquibaseTables
     * @throws  LiquibaseException
     *
     * @see <a href="https://docs.liquibase.com/concepts/advanced/contexts.html" target="_top">contexts</a> in documentation
     * @see <a href="https://docs.liquibase.com/concepts/advanced/labels.html" target="_top">labels</a> in documentation
     */
    @Deprecated
    public void update(Contexts contexts, LabelExpression labelExpression, boolean checkLiquibaseTables) throws LiquibaseException {
        runInScope(() -> {
            CommandScope updateCommand = new CommandScope("update");
            updateCommand.addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, getDatabase());
            updateCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changeLogFile);
            updateCommand.addArgumentValue(UpdateCommandStep.CONTEXTS_ARG, contexts != null ? contexts.toString() : null);
            updateCommand.addArgumentValue(UpdateCommandStep.LABEL_FILTER_ARG, labelExpression != null ? labelExpression.getOriginalString() : null);
            updateCommand.addArgumentValue(UpdateCommandStep.CHANGE_EXEC_LISTENER_ARG, changeExecListener);
            updateCommand.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters);
            updateCommand.execute();
        });
    }

    /**
     * Performs check of the historyService to determine if there is no unrun changesets without obtaining an exclusive write lock.
     * This allows multiple peer services to boot in parallel in the common case where there are no changelogs to run.
     * <p>
     * If we see that there is nothing in the changelog to run and this returns <b>true</b>, then regardless of the lock status we already know we are "done" and can finish up without waiting for the lock.
     * <p>
     * But, if there are changelogs that might have to be ran and this returns <b>false</b>, you MUST get a lock and do a real check to know what changesets actually need to run.
     * <p>
     * NOTE: to reduce the number of queries to the databasehistory table, this method will cache the "fast check" results within this instance under the assumption that the total changesets will not change within this instance.
     */
    protected boolean isUpToDateFastCheck(Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        String cacheKey = contexts +"/"+ labelExpression;
        if (!this.upToDateFastCheck.containsKey(cacheKey)) {
            try {
                if (listUnrunChangeSets(contexts, labelExpression, false).isEmpty()) {
                    LOG.fine("Fast check found no un-run changesets");
                    upToDateFastCheck.put(cacheKey, true);
                } else {
                    upToDateFastCheck.put(cacheKey, false);
                }
            } catch (DatabaseException e) {
                LOG.info("Error querying Liquibase tables, disabling fast check for this execution. Reason: " + e.getMessage());
                upToDateFastCheck.put(cacheKey, false);
            } finally {
                // Discard the cached fetched un-run changeset list, as if
                // another peer is running the changesets in parallel, we may
                // get a different answer after taking out the write lock

                ChangeLogHistoryService changeLogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
                changeLogService.reset();
            }
        }
        return upToDateFastCheck.get(cacheKey);
    }

    /**
     *
     * Create or retrieve the Connection object
     *
     * @param   changeLog              Database changelog
     * @return  Connection
     * @throws  LiquibaseHubException  Thrown by HubService
     *
     */
    public Connection getConnection(DatabaseChangeLog changeLog) throws LiquibaseHubException {
        //
        // If our current Executor is a LoggingExecutor then just return since we will not update Hub
        //
        Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
        if (executor instanceof LoggingExecutor) {
            return null;
        }
        String changeLogId = changeLog.getChangeLogId();
        HubUpdater hubUpdater = new HubUpdater(new Date(), changeLog, database);
        if (hubUpdater.hubIsNotAvailable(changeLogId)) {
            if (StringUtil.isNotEmpty(HubConfiguration.LIQUIBASE_HUB_API_KEY.getCurrentValue()) && changeLogId == null) {
                String message =
                    "An API key was configured, but no changelog ID exists.\n" +
                    "No operations will be reported. Register this changelog with Liquibase Hub to generate free deployment reports.\n" +
                    "Learn more at https://hub.liquibase.com.";
                Scope.getCurrentScope().getUI().sendMessage("WARNING: " + message);
                Scope.getCurrentScope().getLog(getClass()).warning(message);
            }
            return null;
        }

        //
        // Warn about the situation where there is a changeLog ID, but no API key
        //
        if (StringUtil.isEmpty(HubConfiguration.LIQUIBASE_HUB_API_KEY.getCurrentValue()) && changeLogId != null) {
            String message = "The changelog ID '" + changeLogId + "' was found, but no API Key exists.\n" +
                             "No operations will be reported. Simply add a liquibase.hub.apiKey setting to generate free deployment reports.\n" +
                             "Learn more at https://hub.liquibase.com.";
            Scope.getCurrentScope().getUI().sendMessage("WARNING: " + message);
            Scope.getCurrentScope().getLog(getClass()).warning(message);
            return null;
        }
        Connection connection;
        final HubService hubService = Scope.getCurrentScope().getSingleton(HubServiceFactory.class).getService();
        if (getHubConnectionId() == null) {
            HubChangeLog hubChangeLog = hubService.getHubChangeLog(UUID.fromString(changeLogId), "*");
            if (hubChangeLog == null) {
                Scope.getCurrentScope().getLog(getClass()).warning(
                    "Retrieving Hub Change Log failed for Changelog ID: " + changeLogId);
                return null;
            }
            if (hubChangeLog.isDeleted()) {
                //
                // Complain and stop the operation
                //
                String message =
                    "\n" +
                        "The operation did not complete and will not be reported to Hub because the\n" +  "" +
                        "registered changelog has been deleted by someone in your organization.\n" +
                        "Learn more at http://hub.liquibase.com.";
                throw new LiquibaseHubException(message);
            }

            Connection exampleConnection = new Connection();
            exampleConnection.setProject(hubChangeLog.getProject());
            exampleConnection.setJdbcUrl(Liquibase.this.database.getConnection().getURL());
            connection = hubService.getConnection(exampleConnection, true);

            setHubConnectionId(connection.getId());
        } else {
            connection = hubService.getConnection(new Connection().setId(getHubConnectionId()), true);
        }
        return connection;
    }


    public DatabaseChangeLog getDatabaseChangeLog() throws LiquibaseException {
        return getDatabaseChangeLog(false);
    }

    /**
     * @param shouldWarnOnMismatchedXsdVersion When set to true, a warning will be printed to the console if the XSD
     *                                         version used does not match the version of Liquibase. If "latest" is used
     *                                         as the XSD version, no warning is printed. If the changelog is not xml
     *                                         format, no warning is printed.
     */
    private DatabaseChangeLog getDatabaseChangeLog(boolean shouldWarnOnMismatchedXsdVersion) throws LiquibaseException {
        if (databaseChangeLog == null && changeLogFile != null) {
            Scope.getCurrentScope().addMdcValue(MdcKey.CHANGELOG_FILE, changeLogFile);
            ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor);
            if (parser instanceof XMLChangeLogSAXParser) {
                ((XMLChangeLogSAXParser) parser).setShouldWarnOnMismatchedXsdVersion(shouldWarnOnMismatchedXsdVersion);
            }
            databaseChangeLog = parser.parse(changeLogFile, changeLogParameters, resourceAccessor);
            Scope.getCurrentScope().getLog(Liquibase.class).info("Parsed changelog file '" + changeLogFile + "'");
        }

        return databaseChangeLog;
    }


    protected UpdateVisitor createUpdateVisitor() {
        return new UpdateVisitor(database, changeExecListener);
    }

    protected UpdateVisitor createUpdateVisitor(ChangeExecListener listener) {
        return new UpdateVisitor(database, listener);
    }

    protected RollbackVisitor createRollbackVisitor() {
        return new RollbackVisitor(database, changeExecListener);
    }

    protected RollbackVisitor createRollbackVisitor(List<ChangesetsRolledback.ChangeSet> processedChangesets) {
        return new RollbackVisitor(database, changeExecListener, processedChangesets);
    }

    /**
     *
     * Return a ChangeLogIterator constructed with standard filters
     *
     * @param   contexts                           Contexts to filter for
     * @param   labelExpression                    Labels to filter for
     * @param   changeLog                          The changelog to process
     *
     * @return  ChangeLogIterator
     * @throws DatabaseException
     *
     */
    protected ChangeLogIterator getStandardChangelogIterator(Contexts contexts, LabelExpression labelExpression,
                                                             DatabaseChangeLog changeLog) throws DatabaseException {
        return new ChangeLogIterator(changeLog,
                new ShouldRunChangeSetFilter(database),
                new ContextChangeSetFilter(contexts),
                new LabelChangeSetFilter(labelExpression),
                new DbmsChangeSetFilter(database),
                new IgnoreChangeSetFilter());
    }

    /**
     *
     * Return a StatusChangeLogIterator constructed with standard filters
     *
     * @param   contexts                           Contexts to filter for
     * @param   labelExpression                    Labels to filter for
     *                                             default value is false to only gather the first
     * @param   changeLog                          The changelog to process
     *
     * @return  StatusChangeLogIterator
     * @throws DatabaseException
     *
     */
    protected ChangeLogIterator getStandardStatusChangelogIterator(Contexts contexts, LabelExpression labelExpression,
                                                                   DatabaseChangeLog changeLog) throws DatabaseException {
        return new StatusChangeLogIterator(changeLog,
                new ShouldRunChangeSetFilter(database),
                new ContextChangeSetFilter(contexts),
                new LabelChangeSetFilter(labelExpression),
                new DbmsChangeSetFilter(database),
                new IgnoreChangeSetFilter());
    }

    protected ChangeLogIterator buildChangeLogIterator(String tag, DatabaseChangeLog changeLog, Contexts contexts,
                                                       LabelExpression labelExpression) throws DatabaseException {

        if (tag == null) {
            return new ChangeLogIterator(changeLog,
                    new NotRanChangeSetFilter(database.getRanChangeSetList()),
                new ContextChangeSetFilter(contexts),
                new LabelChangeSetFilter(labelExpression),
                new IgnoreChangeSetFilter(),
                new DbmsChangeSetFilter(database));
        } else {
            List<RanChangeSet> ranChangeSetList = database.getRanChangeSetList();
            return new ChangeLogIterator(changeLog,
                new NotRanChangeSetFilter(database.getRanChangeSetList()),
                new ContextChangeSetFilter(contexts),
                new LabelChangeSetFilter(labelExpression),
                new IgnoreChangeSetFilter(),
                new DbmsChangeSetFilter(database),
                new UpToTagChangeSetFilter(tag, ranChangeSetList));
        }
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

        runInScope(new Scope.ScopedRunner() {
            @Override
            public void run() throws Exception {

                /* We have no other choice than to save the current Executer here. */
                @SuppressWarnings("squid:S1941")
                Executor oldTemplate = getAndReplaceJdbcExecutor(output);

                outputHeader("Update Database Script");

                if (isUpToDateFastCheck(contexts, labelExpression)) {
                    Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("logging", database).comment("Database is up to date, no changesets to execute");
                } else {
                    LockService lockService = LockServiceFactory.getInstance().getLockService(database);
                    lockService.waitForLock();

                    update(contexts, labelExpression, checkLiquibaseTables);
                }

                flushOutputWriter(output);

                Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("jdbc", database, oldTemplate);
            }
        });
    }

    public void update(int changesToApply, String contexts) throws LiquibaseException {
        update(changesToApply, new Contexts(contexts), new LabelExpression());
    }

    /**
     *
     * Update to count
     *
     * @param  changesToApply
     * @param  contexts
     * @param  labelExpression
     * @throws LiquibaseException
     *
     */
    public void update(int changesToApply, Contexts contexts, LabelExpression labelExpression)
            throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);
        addCommandFiltersMdc(labelExpression, contexts);

        runInScope(new Scope.ScopedRunner() {
            @Override
            public void run() throws Exception {

                LockService lockService = LockServiceFactory.getInstance().getLockService(database);
                lockService.waitForLock();
                Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_TARGET_URL, database.getConnection().getURL());

                Operation updateOperation = null;
                BufferedLogService bufferLog = new BufferedLogService();
                DatabaseChangeLog changeLog = null;
                HubUpdater hubUpdater = null;
                try {
                    changeLog = getDatabaseChangeLog();

                    checkLiquibaseTables(true, changeLog, contexts, labelExpression);

                    changeLog.validate(database, contexts, labelExpression);

                    //
                    // Let the user know that they can register for Hub
                    //
                    hubUpdater = new HubUpdater(new Date(), changeLog, database);
                    hubUpdater.register(changeLogFile);

                    generateDeploymentId();

                    //
                    // Create an iterator which will be used with a ListVisitor
                    // to grab the list of changesets for the update
                    //
                    ChangeLogIterator listLogIterator = new ChangeLogIterator(changeLog,
                            new ShouldRunChangeSetFilter(database),
                            new ContextChangeSetFilter(contexts),
                            new LabelChangeSetFilter(labelExpression),
                            new DbmsChangeSetFilter(database),
                            new IgnoreChangeSetFilter(),
                            new CountChangeSetFilter(changesToApply));

                    //
                    // Create or retrieve the Connection
                    // Make sure the Hub is available here by checking the return
                    //
                    Connection connection = getConnection(changeLog);
                    if (connection != null) {
                        updateOperation =
                            hubUpdater.preUpdateHub("UPDATE", "update-count", connection, changeLogFile, contexts, labelExpression, listLogIterator);
                    }

                    //
                    // If we are doing Hub then set up a HubChangeExecListener
                    //
                    if (connection != null) {
                        changeExecListener = new HubChangeExecListener(updateOperation, changeExecListener);
                    }

                    //
                    // Iterate to find the change sets which will be skipped
                    //
                    StatusVisitor statusVisitor = new StatusVisitor(database);
                    ChangeLogIterator shouldRunIterator = new StatusChangeLogIterator(changeLog,
                            new ShouldRunChangeSetFilter(database),
                            new ContextChangeSetFilter(contexts),
                            new LabelChangeSetFilter(labelExpression),
                            new DbmsChangeSetFilter(database),
                            new IgnoreChangeSetFilter(),
                            new CountChangeSetFilter(changesToApply));
                    shouldRunIterator.run(statusVisitor, new RuntimeEnvironment(database, contexts, labelExpression));

                    //
                    // Create another iterator to run
                    //
                    ChangeLogIterator runChangeLogIterator = new ChangeLogIterator(changeLog,
                            new ShouldRunChangeSetFilter(database),
                            new ContextChangeSetFilter(contexts),
                            new LabelChangeSetFilter(labelExpression),
                            new DbmsChangeSetFilter(database),
                            new IgnoreChangeSetFilter(),
                            new CountChangeSetFilter(changesToApply));

                    CompositeLogService compositeLogService = new CompositeLogService(true, bufferLog);
                    Scope.child(Scope.Attr.logService.name(), compositeLogService, () -> runChangeLogIterator.run(createUpdateVisitor(), new RuntimeEnvironment(database, contexts, labelExpression)));

                    ShowSummaryUtil.showUpdateSummary(changeLog, statusVisitor);

                    hubUpdater.postUpdateHub(updateOperation, bufferLog);
                    logDeploymentOutcomeMdc(true);
                }
                catch (Throwable e) {
                    logDeploymentOutcomeMdc(false);
                    if (hubUpdater != null) {
                        hubUpdater.postUpdateHubExceptionHandling(updateOperation, bufferLog, e.getMessage());
                    }
                    throw e;
                } finally {
                    database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
                    try {
                        lockService.releaseLock();
                    } catch (LockException e) {
                        LOG.severe(MSG_COULD_NOT_RELEASE_LOCK, e);
                    }
                    resetServices();
                    setChangeExecListener(null);
                }
            }
        });
    }

    public void update(String tag, String contexts) throws LiquibaseException {
        update(tag, new Contexts(contexts), new LabelExpression());
    }

    public void update(String tag, Contexts contexts) throws LiquibaseException {
        update(tag, contexts, new LabelExpression());
    }

    /**
     *
     * Update to tag
     *
     * @param   tag                             Tag to update for
     * @param   contexts
     * @param   labelExpression
     * @throws  LiquibaseException
     *
     */
    public void update(String tag, Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        if (tag == null) {
            update(contexts, labelExpression);
            return;
        }
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);
        addCommandFiltersMdc(labelExpression, contexts);

        runInScope(new Scope.ScopedRunner() {
            @Override
            public void run() throws Exception {
                LockService lockService = LockServiceFactory.getInstance().getLockService(database);
                lockService.waitForLock();
                Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_TARGET_URL, database.getConnection().getURL());

                HubUpdater hubUpdater = null;
                Operation updateOperation = null;
                BufferedLogService bufferLog = new BufferedLogService();
                DatabaseChangeLog changeLog = null;
                try {

                    changeLog = getDatabaseChangeLog();

                    checkLiquibaseTables(true, changeLog, contexts, labelExpression);

                    changeLog.validate(database, contexts, labelExpression);

                    //
                    // Let the user know that they can register for Hub
                    //
                    hubUpdater = new HubUpdater(new Date(), changeLog, database);
                    hubUpdater.register(changeLogFile);

                    generateDeploymentId();

                    //
                    // Create an iterator which will be used with a ListVisitor
                    // to grab the list of changesets for the update
                    //
                    List<RanChangeSet> ranChangeSetList = database.getRanChangeSetList();
                    ChangeLogIterator listLogIterator = new ChangeLogIterator(changeLog,
                            new ShouldRunChangeSetFilter(database),
                            new ContextChangeSetFilter(contexts),
                            new LabelChangeSetFilter(labelExpression),
                            new DbmsChangeSetFilter(database),
                            new IgnoreChangeSetFilter(),
                            new UpToTagChangeSetFilter(tag, ranChangeSetList));

                    //
                    // Create or retrieve the Connection
                    // Make sure the Hub is available here by checking the return
                    //
                    Connection connection = getConnection(changeLog);
                    if (connection != null) {
                        updateOperation =
                           hubUpdater.preUpdateHub("UPDATE", "update-to-tag", connection, changeLogFile, contexts, labelExpression, listLogIterator);
                    }

                    //
                    // Check for an already existing Listener
                    //
                    if (connection != null) {
                        changeExecListener = new HubChangeExecListener(updateOperation, changeExecListener);
                    }

                    //
                    // Iterate to find the change sets which will be skipped
                    //
                    StatusVisitor statusVisitor = new StatusVisitor(database);
                    ChangeLogIterator shouldRunIterator = new StatusChangeLogIterator(changeLog, tag,
                            new ShouldRunChangeSetFilter(database),
                            new ContextChangeSetFilter(contexts),
                            new LabelChangeSetFilter(labelExpression),
                            new DbmsChangeSetFilter(database),
                            new IgnoreChangeSetFilter(),
                            new UpToTagChangeSetFilter(tag, ranChangeSetList));
                    shouldRunIterator.run(statusVisitor, new RuntimeEnvironment(database, contexts, labelExpression));

                    //
                    // Create another iterator to run
                    //
                    ChangeLogIterator runChangeLogIterator = new ChangeLogIterator(changeLog,
                            new ShouldRunChangeSetFilter(database),
                            new ContextChangeSetFilter(contexts),
                            new LabelChangeSetFilter(labelExpression),
                            new DbmsChangeSetFilter(database),
                            new IgnoreChangeSetFilter(),
                            new UpToTagChangeSetFilter(tag, ranChangeSetList));

                    CompositeLogService compositeLogService = new CompositeLogService(true, bufferLog);
                    Scope.child(Scope.Attr.logService.name(), compositeLogService, () -> runChangeLogIterator.run(createUpdateVisitor(), new RuntimeEnvironment(database, contexts, labelExpression)));

                    ShowSummaryUtil.showUpdateSummary(changeLog, statusVisitor);

                    hubUpdater.postUpdateHub(updateOperation, bufferLog);
                    logDeploymentOutcomeMdc(true);
                }
                catch (Throwable e) {
                    logDeploymentOutcomeMdc(false);
                    if (hubUpdater != null) {
                        hubUpdater.postUpdateHubExceptionHandling(updateOperation, bufferLog, e.getMessage());
                    }
                    throw e;
                } finally {
                    database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
                    try {
                        lockService.releaseLock();
                    } catch (LockException e) {
                        LOG.severe(MSG_COULD_NOT_RELEASE_LOCK, e);
                    }
                    resetServices();
                    setChangeExecListener(null);
                }
            }
        });
    }

    public void update(int changesToApply, String contexts, Writer output) throws LiquibaseException {
        this.update(changesToApply, new Contexts(contexts), new LabelExpression(), output);
    }

    public void update(int changesToApply, Contexts contexts, LabelExpression labelExpression, Writer output) throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        runInScope(new Scope.ScopedRunner() {
            @Override
            public void run() throws Exception {

                /* We have no other choice than to save the current Executer here. */
                @SuppressWarnings("squid:S1941")
                Executor oldTemplate = getAndReplaceJdbcExecutor(output);
                outputHeader("Update " + changesToApply + " Changesets Database Script");

                update(changesToApply, contexts, labelExpression);

                flushOutputWriter(output);

                resetServices();
                Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("jdbc", database, oldTemplate);
            }
        });

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

        runInScope(new Scope.ScopedRunner() {
            @Override
            public void run() throws Exception {

                /* We have no other choice than to save the current Executer here. */
                @SuppressWarnings("squid:S1941")
                Executor oldTemplate = getAndReplaceJdbcExecutor(output);

                outputHeader("Update to '" + tag + "' Database Script");

                update(tag, contexts, labelExpression);

                flushOutputWriter(output);

                resetServices();
                Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("jdbc", database, oldTemplate);
            }
        });
    }

    private void addCommandFiltersMdc(LabelExpression labelExpression, Contexts contexts) {
        String labelFilterMdc = labelExpression != null && labelExpression.getOriginalString() != null ? labelExpression.getOriginalString() : "";
        String contextFilterMdc = contexts != null ? contexts.toString() : "";
        Scope.getCurrentScope().addMdcValue(MdcKey.COMMAND_LABEL_FILTER, labelFilterMdc);
        Scope.getCurrentScope().addMdcValue(MdcKey.COMMAND_CONTEXT_FILTER, contextFilterMdc);
    }

    private void generateDeploymentId() {
        ChangeLogHistoryService changelogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
        changelogService.generateDeploymentId();
        Scope.getCurrentScope().addMdcValue(MdcKey.DEPLOYMENT_ID, changelogService.getDeploymentId());
    }

    private void logDeploymentOutcomeMdc(boolean success) throws IOException {
        int deployedChangeSetCount = getDefaultChangeExecListener().getDeployedChangeSets().size();
        String successLog = "Update command completed successfully.";
        String failureLog = "Update command encountered an exception.";
        try (MdcObject deploymentOutcomeMdc = Scope.getCurrentScope().addMdcValue(MdcKey.DEPLOYMENT_OUTCOME, success ? MdcValue.COMMAND_SUCCESSFUL : MdcValue.COMMAND_FAILED);
             MdcObject deploymentOutcomeCountMdc = Scope.getCurrentScope().addMdcValue(MdcKey.DEPLOYMENT_OUTCOME_COUNT, String.valueOf(deployedChangeSetCount))) {
            Scope.getCurrentScope().getLog(getClass()).info(success ? successLog : failureLog);
        }
    }

    /**
     * @deprecated use {@link LoggingExecutorTextUtil#outputHeader(String, Database, String))}
     */
    @Deprecated
    public void outputHeader(String message) throws DatabaseException {
        LoggingExecutorTextUtil.outputHeader(message, database, changeLogFile);
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

        runInScope(new Scope.ScopedRunner() {
            @Override
            public void run() throws Exception {

                /* We have no other choice than to save the current Executer here. */
                @SuppressWarnings("squid:S1941")
                Executor oldTemplate = getAndReplaceJdbcExecutor(output);

                outputHeader("Rollback " + changesToRollback + " Change(s) Script");

                rollback(changesToRollback, rollbackScript, contexts, labelExpression);

                flushOutputWriter(output);
                Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("jdbc", database, oldTemplate);
                resetServices();
            }
        });

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

    /**
     *
     * Rollback count
     *
     * @param changesToRollback
     * @param rollbackScript
     * @param contexts
     * @param labelExpression
     * @throws LiquibaseException
     */
    public void rollback(int changesToRollback, String rollbackScript, Contexts contexts,
                         LabelExpression labelExpression) throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);
        addCommandFiltersMdc(labelExpression, contexts);

        runInScope(new Scope.ScopedRunner() {
            @Override
            public void run() throws Exception {
                Scope.getCurrentScope().addMdcValue(MdcKey.ROLLBACK_COUNT, String.valueOf(changesToRollback));
                Scope.getCurrentScope().addMdcValue(MdcKey.ROLLBACK_SCRIPT, rollbackScript);
                Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_TARGET_URL, database.getConnection().getURL());

                LockService lockService = LockServiceFactory.getInstance().getLockService(database);
                lockService.waitForLock();

                Operation rollbackOperation = null;
                final String operationCommand = "rollback-count";
                BufferedLogService bufferLog = new BufferedLogService();
                DatabaseChangeLog changeLog = null;
                Date startTime = new Date();
                HubUpdater hubUpdater = null;
                try {
                    changeLog = getDatabaseChangeLog();
                    checkLiquibaseTables(false, changeLog, contexts, labelExpression);

                    changeLog.validate(database, contexts, labelExpression);

                    //
                    // Let the user know that they can register for Hub
                    //
                    hubUpdater = new HubUpdater(startTime, changeLog, database);
                    hubUpdater.register(changeLogFile);

                    //
                    // Create an iterator which will be used with a ListVisitor
                    // to grab the list of changesets for the update
                    //
                    ChangeLogIterator listLogIterator = new ChangeLogIterator(database.getRanChangeSetList(), changeLog,
                            new AlreadyRanChangeSetFilter(database.getRanChangeSetList()),
                            new ContextChangeSetFilter(contexts),
                            new LabelChangeSetFilter(labelExpression),
                            new DbmsChangeSetFilter(database),
                            new IgnoreChangeSetFilter(),
                            new CountChangeSetFilter(changesToRollback));

                    //
                    // Create or retrieve the Connection
                    // Make sure the Hub is available here by checking the return
                    //
                    Connection connection = getConnection(changeLog);
                    if (connection != null) {
                        rollbackOperation = hubUpdater.preUpdateHub("ROLLBACK", operationCommand, connection, changeLogFile, contexts, labelExpression, listLogIterator);
                    }

                    //
                    // If we are doing Hub then set up a HubChangeExecListener
                    //
                    if (connection != null) {
                        changeExecListener = new HubChangeExecListener(rollbackOperation, changeExecListener);
                    }

                    //
                    // Create another iterator to run
                    //
                    ChangeLogIterator logIterator = new ChangeLogIterator(database.getRanChangeSetList(), changeLog,
                            new AlreadyRanChangeSetFilter(database.getRanChangeSetList()),
                            new ContextChangeSetFilter(contexts),
                            new LabelChangeSetFilter(labelExpression),
                            new DbmsChangeSetFilter(database),
                            new IgnoreChangeSetFilter(),
                            new CountChangeSetFilter(changesToRollback));

                    doRollback(bufferLog, rollbackScript, logIterator, contexts, labelExpression, hubUpdater, rollbackOperation);
                }
                catch (Throwable t) {
                    handleRollbackException(t, hubUpdater, rollbackOperation, bufferLog, operationCommand);
                    throw t;
                } finally {
                    handleRollbackFinally(lockService);
                }
            }
        });
    }

    /**
     * Actually perform the rollback operation. Determining which changesets to roll back is the responsibility of the
     * logIterator.
     */
    private void doRollback(BufferedLogService bufferLog, String rollbackScript, ChangeLogIterator logIterator, Contexts contexts, LabelExpression labelExpression, HubUpdater hubUpdater, Operation rollbackOperation) throws Exception {
        CompositeLogService compositeLogService = new CompositeLogService(true, bufferLog);
        if (rollbackScript == null) {
            List<ChangesetsRolledback.ChangeSet> processedChangesets = new ArrayList<>();
            Scope.child(Scope.Attr.logService.name(), compositeLogService, () -> {
                logIterator.run(createRollbackVisitor(processedChangesets), new RuntimeEnvironment(database, contexts, labelExpression));
            });
            Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESETS_ROLLED_BACK, new ChangesetsRolledback(processedChangesets), false);
        } else {
            List<ChangeSet> changeSets = determineRollbacks(logIterator, contexts, labelExpression);
            Map<String, Object> values = new HashMap<>();
            values.put(Scope.Attr.logService.name(), compositeLogService);
            values.put(BufferedLogService.class.getName(), bufferLog);
            Scope.child(values, () -> {
                executeRollbackScript(rollbackScript, changeSets, contexts, labelExpression);
            });
            removeRunStatus(changeSets, contexts, labelExpression);
            Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESETS_ROLLED_BACK, ChangesetsRolledback.fromChangesetList(changeSets));
        }
        hubUpdater.postUpdateHub(rollbackOperation, bufferLog);
        try (MdcObject deploymentOutcomeMdc = Scope.getCurrentScope().getMdcManager().put(MdcKey.DEPLOYMENT_OUTCOME, MdcValue.COMMAND_SUCCESSFUL)) {
            Scope.getCurrentScope().getLog(getClass()).info("Rollback command completed successfully.");
        }
    }

    private List<ChangeSet> determineRollbacks(ChangeLogIterator logIterator, Contexts contexts, LabelExpression labelExpression)
            throws LiquibaseException {
        List<ChangeSet> changeSetsToRollback = new ArrayList<>();
        logIterator.run(new ChangeSetVisitor() {
            @Override
            public Direction getDirection() {
                return Direction.REVERSE;
            }

            @Override
            public void visit(ChangeSet changeSet, DatabaseChangeLog databaseChangeLog, Database database,
                              Set<ChangeSetFilterResult> filterResults) throws LiquibaseException {
                changeSetsToRollback.add(changeSet);
            }
        }, new RuntimeEnvironment(database, contexts, labelExpression));
        return changeSetsToRollback;
    }

    protected void removeRunStatus(List<ChangeSet> changeSets, Contexts contexts, LabelExpression labelExpression)
            throws LiquibaseException {
        for (ChangeSet changeSet : changeSets) {
            database.removeRanStatus(changeSet);
            database.commit();
        }
    }

    protected void executeRollbackScript(String rollbackScript, List<ChangeSet> changeSets, Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        final Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
        String rollbackScriptContents;
        try {
            Resource resource = resourceAccessor.get(rollbackScript);
            if (resource == null) {
                throw new LiquibaseException("WARNING: The rollback script '" + rollbackScript + "' was not located.  Please check your parameters. No rollback was performed");
            }
            try (InputStream stream = resource.openInputStream()) {
                rollbackScriptContents = StreamUtil.readStreamAsString(stream);
            }
        } catch (IOException e) {
            throw new LiquibaseException("Error reading rollbackScript " + executor + ": " + e.getMessage());
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
            ((HubChangeExecListener)changeExecListener).setRollbackScriptContents(rollbackScriptContents);
            sendRollbackMessages(changeSets, changelog, RollbackMessageType.WILL_ROLLBACK, contexts, labelExpression, null);
            executor.execute(rollbackChange);
            sendRollbackMessages(changeSets, changelog, RollbackMessageType.ROLLED_BACK, contexts, labelExpression, null);
        } catch (DatabaseException e) {
            Scope.getCurrentScope().getLog(getClass()).warning(e.getMessage());
            LOG.severe("Error executing rollback script: " + e.getMessage());
            if (changeExecListener != null) {
                sendRollbackMessages(changeSets, changelog, RollbackMessageType.ROLLBACK_FAILED, contexts, labelExpression, e);
            }
            throw new DatabaseException("Error executing rollback script", e);
        }
        database.commit();
    }

    private void sendRollbackMessages(List<ChangeSet> changeSets,
                                      DatabaseChangeLog changelog,
                                      RollbackMessageType messageType,
                                      Contexts contexts,
                                      LabelExpression labelExpression,
                                      Exception exception) throws LiquibaseException {
        for (ChangeSet changeSet : changeSets) {
            if (messageType == RollbackMessageType.WILL_ROLLBACK) {
                changeExecListener.willRollback(changeSet, databaseChangeLog, database);
            }
            else if (messageType == RollbackMessageType.ROLLED_BACK) {
                final String message = "Rolled Back Changeset:" + changeSet.toString(false);
                Scope.getCurrentScope().getUI().sendMessage(message);
                LOG.info(message);
                changeExecListener.rolledBack(changeSet, databaseChangeLog, database);
            }
            else if (messageType == RollbackMessageType.ROLLBACK_FAILED) {
                final String message = "Failed rolling back Changeset:" + changeSet.toString(false);
                Scope.getCurrentScope().getUI().sendMessage(message);
                changeExecListener.rollbackFailed(changeSet, databaseChangeLog, database, exception);
            }
        }
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

        /* We have no other choice than to save the current Executer here. */
        @SuppressWarnings("squid:S1941")
        Executor oldTemplate = getAndReplaceJdbcExecutor(output);

        outputHeader("Rollback to '" + tagToRollBackTo + "' Script");

        rollback(tagToRollBackTo, contexts, labelExpression);

        flushOutputWriter(output);
        Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("jdbc", database, oldTemplate);
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

    /**
     *
     * Rollback to tag
     *
     * @param tagToRollBackTo
     * @param rollbackScript
     * @param contexts
     * @param labelExpression
     * @throws LiquibaseException
     */
    public void rollback(String tagToRollBackTo, String rollbackScript, Contexts contexts,
                         LabelExpression labelExpression) throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);
        addCommandFiltersMdc(labelExpression, contexts);

        runInScope(new Scope.ScopedRunner() {
            @Override
            public void run() throws Exception {
                Scope.getCurrentScope().addMdcValue(MdcKey.ROLLBACK_TO_TAG, tagToRollBackTo);
                Scope.getCurrentScope().addMdcValue(MdcKey.ROLLBACK_SCRIPT, rollbackScript);
                Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_TARGET_URL, database.getConnection().getURL());

                LockService lockService = LockServiceFactory.getInstance().getLockService(database);
                lockService.waitForLock();

                Operation rollbackOperation = null;
                final String operationCommand = "rollback";
                BufferedLogService bufferLog = new BufferedLogService();
                DatabaseChangeLog changeLog = null;
                Date startTime = new Date();
                HubUpdater hubUpdater = null;

                try {

                    changeLog = getDatabaseChangeLog();
                    checkLiquibaseTables(false, changeLog, contexts, labelExpression);

                    changeLog.validate(database, contexts, labelExpression);

                    //
                    // Let the user know that they can register for Hub
                    //
                    hubUpdater = new HubUpdater(startTime, changeLog, database);
                    hubUpdater.register(changeLogFile);

                    //
                    // Create an iterator which will be used with a ListVisitor
                    // to grab the list of changesets for the update
                    //
                    List<RanChangeSet> ranChangeSetList = database.getRanChangeSetList();
                    ChangeLogIterator listLogIterator = new ChangeLogIterator(ranChangeSetList, changeLog,
                            new AfterTagChangeSetFilter(tagToRollBackTo, ranChangeSetList),
                            new AlreadyRanChangeSetFilter(ranChangeSetList),
                            new ContextChangeSetFilter(contexts),
                            new LabelChangeSetFilter(labelExpression),
                            new IgnoreChangeSetFilter(),
                            new DbmsChangeSetFilter(database));

                    //
                    // Create or retrieve the Connection
                    // Make sure the Hub is available here by checking the return
                    //
                    Connection connection = getConnection(changeLog);
                    if (connection != null) {
                        rollbackOperation = hubUpdater.preUpdateHub("ROLLBACK", operationCommand, connection, changeLogFile, contexts, labelExpression, listLogIterator);
                    }

                    //
                    // If we are doing Hub then set up a HubChangeExecListener
                    //
                    if (connection != null) {
                        changeExecListener = new HubChangeExecListener(rollbackOperation, changeExecListener);
                    }

                    //
                    // Create another iterator to run
                    //
                    ChangeLogIterator logIterator = new ChangeLogIterator(ranChangeSetList, changeLog,
                            new AfterTagChangeSetFilter(tagToRollBackTo, ranChangeSetList),
                            new AlreadyRanChangeSetFilter(ranChangeSetList),
                            new ContextChangeSetFilter(contexts),
                            new LabelChangeSetFilter(labelExpression),
                            new IgnoreChangeSetFilter(),
                            new DbmsChangeSetFilter(database));

                    doRollback(bufferLog, rollbackScript, logIterator, contexts, labelExpression, hubUpdater, rollbackOperation);
                }
                catch (Throwable t) {
                    handleRollbackException(t, hubUpdater, rollbackOperation, bufferLog, operationCommand);
                    throw t;
                } finally {
                    handleRollbackFinally(lockService);
                }
            }
        });
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

        @SuppressWarnings("squid:S1941")
        Executor oldTemplate = getAndReplaceJdbcExecutor(output);

        outputHeader("Rollback to " + dateToRollBackTo + " Script");

        rollback(dateToRollBackTo, contexts, labelExpression);

        flushOutputWriter(output);
        Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("jdbc", database, oldTemplate);
        resetServices();
    }

    private Executor getAndReplaceJdbcExecutor(Writer output) {
        /* We have no other choice than to save the current Executor here. */
        @SuppressWarnings("squid:S1941")
        Executor oldTemplate = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
        final LoggingExecutor loggingExecutor = new LoggingExecutor(oldTemplate, output, database);
        Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("logging", database, loggingExecutor);
        Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("jdbc", database, loggingExecutor);
        return oldTemplate;
    }

    public void rollback(Date dateToRollBackTo, String contexts) throws LiquibaseException {
        rollback(dateToRollBackTo, null, contexts);
    }

    public void rollback(Date dateToRollBackTo, Contexts contexts, LabelExpression labelExpression)
            throws LiquibaseException {
        rollback(dateToRollBackTo, null, contexts, labelExpression);
    }

    public void rollback(Date dateToRollBackTo, String rollbackScript, String contexts) throws LiquibaseException {
        rollback(dateToRollBackTo, new Contexts(contexts), new LabelExpression());
    }

    /**
     *
     * Rollback to date
     *
     * @param dateToRollBackTo
     * @param rollbackScript
     * @param contexts
     * @param labelExpression
     * @throws LiquibaseException
     */
    public void rollback(Date dateToRollBackTo, String rollbackScript, Contexts contexts,
                         LabelExpression labelExpression) throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);
        addCommandFiltersMdc(labelExpression, contexts);

        runInScope(new Scope.ScopedRunner() {
            @Override
            public void run() throws Exception {
                Scope.getCurrentScope().addMdcValue(MdcKey.ROLLBACK_TO_DATE, dateToRollBackTo.toString());
                Scope.getCurrentScope().addMdcValue(MdcKey.ROLLBACK_SCRIPT, rollbackScript);
                Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_TARGET_URL, database.getConnection().getURL());

                LockService lockService = LockServiceFactory.getInstance().getLockService(database);
                lockService.waitForLock();

                Operation rollbackOperation = null;
                final String operationCommand = "rollback-to-date";
                BufferedLogService bufferLog = new BufferedLogService();
                DatabaseChangeLog changeLog = null;
                Date startTime = new Date();
                HubUpdater hubUpdater = null;

                try {
                    changeLog = getDatabaseChangeLog();
                    checkLiquibaseTables(false, changeLog, contexts, labelExpression);
                    changeLog.validate(database, contexts, labelExpression);

                    //
                    // Let the user know that they can register for Hub
                    //
                    hubUpdater = new HubUpdater(startTime, changeLog, database);
                    hubUpdater.register(changeLogFile);

                    //
                    // Create an iterator which will be used with a ListVisitor
                    // to grab the list of changesets for the update
                    //
                    List<RanChangeSet> ranChangeSetList = database.getRanChangeSetList();
                    ChangeLogIterator listLogIterator = new ChangeLogIterator(ranChangeSetList, changeLog,
                            new ExecutedAfterChangeSetFilter(dateToRollBackTo, ranChangeSetList),
                            new AlreadyRanChangeSetFilter(ranChangeSetList),
                            new ContextChangeSetFilter(contexts),
                            new LabelChangeSetFilter(labelExpression),
                            new IgnoreChangeSetFilter(),
                            new DbmsChangeSetFilter(database));

                    //
                    // Create or retrieve the Connection
                    // Make sure the Hub is available here by checking the return
                    //
                    Connection connection = getConnection(changeLog);
                    if (connection != null) {
                        rollbackOperation = hubUpdater.preUpdateHub("ROLLBACK", operationCommand, connection, changeLogFile, contexts, labelExpression, listLogIterator);
                    }

                    //
                    // If we are doing Hub then set up a HubChangeExecListener
                    //
                    if (connection != null) {
                        changeExecListener = new HubChangeExecListener(rollbackOperation, changeExecListener);
                    }

                    //
                    // Create another iterator to run
                    //
                    ChangeLogIterator logIterator = new ChangeLogIterator(ranChangeSetList, changeLog,
                            new ExecutedAfterChangeSetFilter(dateToRollBackTo, ranChangeSetList),
                            new AlreadyRanChangeSetFilter(ranChangeSetList),
                            new ContextChangeSetFilter(contexts),
                            new LabelChangeSetFilter(labelExpression),
                            new IgnoreChangeSetFilter(),
                            new DbmsChangeSetFilter(database));

                    doRollback(bufferLog, rollbackScript, logIterator, contexts, labelExpression, hubUpdater, rollbackOperation);
                }
                catch (Throwable t) {
                    handleRollbackException(t, hubUpdater, rollbackOperation, bufferLog, operationCommand);
                    throw t;
                } finally {
                    handleRollbackFinally(lockService);
                }
            }
        });

    }

    /**
     * Handle an exception thrown by a rollback method.
     */
    private void handleRollbackException(Throwable t, HubUpdater hubUpdater, Operation rollbackOperation, BufferedLogService bufferLog, String operationName) throws IOException {
        if (hubUpdater != null) {
            hubUpdater.postUpdateHubExceptionHandling(rollbackOperation, bufferLog, t.getMessage());
        }
        try (MdcObject deploymentOutcomeMdc = Scope.getCurrentScope().addMdcValue(MdcKey.DEPLOYMENT_OUTCOME, MdcValue.COMMAND_FAILED)) {
            Scope.getCurrentScope().getLog(getClass()).info(operationName + " command encountered an exception.");
        }
    }

    /**
     * Cleanup code that should be called in the finally block of a rollback method.
     */
    private void handleRollbackFinally(LockService lockService){
        try {
            lockService.releaseLock();
        } catch (LockException e) {
            LOG.severe("Error releasing lock", e);
        }
        resetServices();
        setChangeExecListener(null);
        Scope.getCurrentScope().getMdcManager().remove(MdcKey.CHANGESETS_ROLLED_BACK);
    }

    public void changeLogSync(String contexts, Writer output) throws LiquibaseException {
        changeLogSync(new Contexts(contexts), new LabelExpression(), output);
    }

    public void changeLogSync(Contexts contexts, LabelExpression labelExpression, Writer output)
        throws LiquibaseException {

        doChangeLogSyncSql(null, contexts, labelExpression, output,
            () -> "SQL to add all changesets to database history table");
    }

    private void flushOutputWriter(Writer output) throws LiquibaseException {
        if (output == null) {
            return;
        }

        try {
            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }
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
        changeLogSync(null, contexts, labelExpression);
    }

    public void changeLogSync(String tag, String contexts) throws LiquibaseException {
        changeLogSync(tag, new Contexts(contexts), new LabelExpression());
    }

    /**
     *
     * Changelogsync or changelogsync to tag
     *
     * @deprecated Use CommandStep
     */
    public void changeLogSync(String tag, Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        String commandToRun = StringUtil.isEmpty(tag) ? ChangelogSyncCommandStep.COMMAND_NAME[0] : ChangelogSyncToTagCommandStep.COMMAND_NAME[0];
        runInScope(() -> {
            new CommandScope(commandToRun)
                    .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, Liquibase.this.getDatabase())
                    .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                    .addArgumentValue(ChangelogSyncCommandStep.HUB_CHANGE_EXEC_LISTENER_ARG, changeExecListener)
                    .addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null? contexts.toString() : null))
                    .addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null))
                    .addArgumentValue(ChangelogSyncToTagCommandStep.TAG_ARG, tag)
                    .execute();
        });

    }

    public void changeLogSync(String tag, String contexts, Writer output) throws LiquibaseException {
        changeLogSync(tag, new Contexts(contexts), new LabelExpression(), output);
    }

    public void changeLogSync(String tag, Contexts contexts, LabelExpression labelExpression, Writer output)
        throws LiquibaseException {
        doChangeLogSyncSql(tag, contexts, labelExpression, output,
            () -> "SQL to add changesets upto '" + tag + "' to database history table");
    }

    private void doChangeLogSyncSql(String tag, Contexts contexts, LabelExpression labelExpression, Writer output,
                                    Supplier<String> header) throws LiquibaseException {
        String commandToRun = StringUtil.isEmpty(tag) ? ChangelogSyncSqlCommandStep.COMMAND_NAME[0] : ChangelogSyncToTagSqlCommandStep.COMMAND_NAME[0];
        runInScope(() -> {
            new CommandScope(commandToRun)
                    .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, Liquibase.this.getDatabase())
                    .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                    .addArgumentValue(ChangelogSyncSqlCommandStep.HUB_CHANGE_EXEC_LISTENER_ARG, changeExecListener)
                    .addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null? contexts.toString() : null))
                    .addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null))
                    .addArgumentValue(ChangelogSyncToTagSqlCommandStep.TAG_ARG, tag)
                    .setOutput(new WriterOutputStream(output, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()))
                    .execute();
        });
    }

    public void markNextChangeSetRan(String contexts, Writer output) throws LiquibaseException {
        markNextChangeSetRan(new Contexts(contexts), new LabelExpression(), output);
    }

    public void markNextChangeSetRan(Contexts contexts, LabelExpression labelExpression, Writer output)
            throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        runInScope(() -> {

            @SuppressWarnings("squid:S1941")
            Executor oldTemplate = getAndReplaceJdbcExecutor(output);
            outputHeader("SQL to add all changesets to database history table");

            markNextChangeSetRan(contexts, labelExpression);

            flushOutputWriter(output);

            Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("jdbc", database, oldTemplate);
            resetServices();
        });
    }

    public void markNextChangeSetRan(String contexts) throws LiquibaseException {
        markNextChangeSetRan(new Contexts(contexts), new LabelExpression());
    }

    public void markNextChangeSetRan(Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        runInScope(new Scope.ScopedRunner() {
            @Override
            public void run() throws Exception {

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
                        LOG.severe(MSG_COULD_NOT_RELEASE_LOCK, e);
                    }
                    resetServices();
                }
            }
        });
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

        runInScope(new Scope.ScopedRunner() {
            @Override
            public void run() throws Exception {


                LoggingExecutor outputTemplate = new LoggingExecutor(Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor(database),
                        output, database);
                Executor oldTemplate = getAndReplaceJdbcExecutor(output);
                Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor(database, outputTemplate);

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
                        UpToTagChangeSetFilter upToTagChangeSetFilter = new UpToTagChangeSetFilter(tag, ranChangeSetList);
                        ChangeLogIterator forwardIterator = new ChangeLogIterator(changeLog,
                                new NotRanChangeSetFilter(ranChangeSetList),
                                new ContextChangeSetFilter(contexts),
                                new LabelChangeSetFilter(labelExpression),
                                new DbmsChangeSetFilter(database),
                                new IgnoreChangeSetFilter(),
                                upToTagChangeSetFilter);
                        final ListVisitor listVisitor = new ListVisitor();
                        forwardIterator.run(listVisitor, new RuntimeEnvironment(database, contexts, labelExpression));

                        //
                        // Check to see if the tag was found and stop if not
                        //
                        if (! upToTagChangeSetFilter.isSeenTag()) {
                            String message = "No tag matching '" + tag + "' found";
                            Scope.getCurrentScope().getUI().sendMessage("ERROR: " + message);
                            Scope.getCurrentScope().getLog(Liquibase.class).severe(message);
                            throw new LiquibaseException(new IllegalArgumentException(message));
                        }

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
                        LOG.severe(MSG_COULD_NOT_RELEASE_LOCK, e);
                    }
                    Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("jdbc", database, oldTemplate);
                    resetServices();
                }

                flushOutputWriter(output);
            }
        });
    }

    protected void resetServices() {
        LockServiceFactory.getInstance().resetAll();
        ChangeLogHistoryServiceFactory.getInstance().resetAll();
        Scope.getCurrentScope().getSingleton(ExecutorService.class).reset();
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
            schemas = new CatalogAndSchema[]{
                    new CatalogAndSchema(getDatabase().getDefaultCatalogName(), getDatabase().getDefaultSchemaName())
            };
        }

        CatalogAndSchema[] finalSchemas = schemas;
        try {
            CommandScope dropAll = new CommandScope("internalDropAll")
                    .addArgumentValue(InternalDropAllCommandStep.DATABASE_ARG, Liquibase.this.getDatabase())
                    .addArgumentValue(InternalDropAllCommandStep.SCHEMAS_ARG, finalSchemas);

            try {
                dropAll.execute();
            } catch (CommandExecutionException e) {
                throw new DatabaseException(e);
            }
        } catch (LiquibaseException e) {
            if (e instanceof DatabaseException) {
                throw (DatabaseException) e;
            } else {
                throw new DatabaseException(e);
            }
        }
    }

    /**
     * 'Tags' the database for future rollback
     *
     * @deprecated Use {@link CommandScope(String)} to tag instead of this method.
     */
    @Deprecated
    public void tag(String tagString) throws LiquibaseException {
        new CommandScope("tag")
                .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, database)
                .addArgumentValue(TagCommandStep.TAG_ARG, tagString)
                .execute();
    }

    /**
     *  Verifies if a given tag exist in the database
     *
     * @deprecated Use {link {@link CommandScope(String)} to verify tag exist instead of this method.
     */
    @Deprecated
    public boolean tagExists(String tagString) throws LiquibaseException {
        CommandResults commandResults = new CommandScope("tagExists")
                .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, database)
                .addArgumentValue(TagExistsCommandStep.TAG_ARG, tagString)
                .execute();
        return commandResults.getResult(TagExistsCommandStep.TAG_EXISTS_RESULT);
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
            return;
        }
        for (DatabaseChangeLogLock lock : locks) {
            out.println(" - " + lock.getLockedBy() + " at " +
                    DateFormat.getDateTimeInstance().format(lock.getLockGranted()));
        }
        out.println("NOTE:  The lock time displayed is based on the database's configured time");
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

        ListVisitor visitor = new ListVisitor();

        runInScope(new Scope.ScopedRunner() {
            @Override
            public void run() throws Exception {

                DatabaseChangeLog changeLog = getDatabaseChangeLog();

                if (checkLiquibaseTables) {
                    checkLiquibaseTables(true, changeLog, contexts, labels);
                }

                changeLog.validate(database, contexts, labels);

                ChangeLogIterator logIterator = getStandardChangelogIterator(contexts, labels, changeLog);

                logIterator.run(visitor, new RuntimeEnvironment(database, contexts, labels));
            }
        });
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
        StatusVisitor visitor = new StatusVisitor(database);

        runInScope(new Scope.ScopedRunner() {
            @Override
            public void run() throws Exception {

                DatabaseChangeLog changeLog = getDatabaseChangeLog();

                if (checkLiquibaseTables) {
                    checkLiquibaseTables(true, changeLog, contexts, labelExpression);
                }

                changeLog.validate(database, contexts, labelExpression);

                ChangeLogIterator logIterator = getStandardChangelogIterator(contexts, labelExpression, changeLog);

                logIterator.run(visitor, new RuntimeEnvironment(database, contexts, labelExpression));
            }
        });
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
                out.append(" changesets have not been applied to ");
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

        ExpectedChangesVisitor visitor = new ExpectedChangesVisitor(database.getRanChangeSetList());

        runInScope(() -> {

            DatabaseChangeLog changeLog = getDatabaseChangeLog();
            changeLog.validate(database, contexts, labelExpression);

            ChangeLogIterator logIterator = new ChangeLogIterator(changeLog,
                    new ContextChangeSetFilter(contexts),
                    new LabelChangeSetFilter(labelExpression),
                    new DbmsChangeSetFilter(database),
                    new IgnoreChangeSetFilter());
            logIterator.run(visitor, new RuntimeEnvironment(database, contexts, labelExpression));

        });
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
        LOG.info("Clearing database change log checksums");
        runInScope(() -> {
            LockService lockService = LockServiceFactory.getInstance().getLockService(database);
            lockService.waitForLock();

            try {
                checkLiquibaseTables(false, null, new Contexts(), new LabelExpression());
                ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).clearAllCheckSums();
            } finally {
                try {
                    lockService.releaseLock();
                } catch (LockException e) {
                    LOG.severe(MSG_COULD_NOT_RELEASE_LOCK, e);
                }
            }
            resetServices();
        });
    }

    /**
     * Calculate the checksum for a given identifier
     *
     * @deprecated Use {link {@link CommandScope(String)}.
     */
    @Deprecated
    public final CheckSum calculateCheckSum(final String changeSetIdentifier) throws LiquibaseException {
        CommandResults commandResults = new CommandScope("calculateChecksum")
                .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, database)
                .addArgumentValue(CalculateChecksumCommandStep.CHANGESET_IDENTIFIER_ARG, changeSetIdentifier)
                .addArgumentValue(CalculateChecksumCommandStep.CHANGELOG_FILE_ARG, this.changeLogFile)
                .execute();
        return commandResults.getResult(CalculateChecksumCommandStep.CHECKSUM_RESULT);
    }

    /**
     * Calculates the checksum for the values that form a given identifier
     *
     * @deprecated Use {link {@link CommandScope(String)}.
     */
    @Deprecated
    public CheckSum calculateCheckSum(final String filename, final String id, final String author)
            throws LiquibaseException {
        return this.calculateCheckSum(String.format("%s::%s::%s", filename, id, author));
    }

    public void generateDocumentation(String outputDirectory) throws LiquibaseException {
        // call without context
        generateDocumentation(outputDirectory, new Contexts(), new LabelExpression(), new CatalogAndSchema(null, null));
    }

    public void generateDocumentation(String outputDirectory, String contexts) throws LiquibaseException {
        generateDocumentation(outputDirectory, new Contexts(contexts), new LabelExpression(), new CatalogAndSchema(null, null));
    }

    public void generateDocumentation(String outputDirectory, String contexts, CatalogAndSchema... schemaList) throws LiquibaseException {
        generateDocumentation(outputDirectory, new Contexts(contexts), new LabelExpression(), schemaList);
    }

    public void generateDocumentation(String outputDirectory, Contexts contexts,
                                      LabelExpression labelExpression, CatalogAndSchema... schemaList) throws LiquibaseException {
        runInScope(new Scope.ScopedRunner() {
            @Override
            public void run() throws Exception {

                LOG.info("Generating Database Documentation");
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

                    final PathHandlerFactory pathHandlerFactory = Scope.getCurrentScope().getSingleton(PathHandlerFactory.class);
                    Resource resource = pathHandlerFactory.getResource(outputDirectory);
                    visitor.writeHTML(resource, resourceAccessor, schemaList);
                } catch (IOException e) {
                    throw new LiquibaseException(e);
                } finally {
                    try {
                        lockService.releaseLock();
                    } catch (LockException e) {
                        LOG.severe(MSG_COULD_NOT_RELEASE_LOCK, e);
                    }
                }
            }
        });
    }

    /**
     * @deprecated Use {link {@link CommandScope(String)} to generate diff instead of this method.
     */
    @Deprecated
    public DiffResult diff(Database referenceDatabase, Database targetDatabase, CompareControl compareControl)
            throws LiquibaseException {
        return DiffGeneratorFactory.getInstance().compare(referenceDatabase, targetDatabase, compareControl);
    }

    /**
     * Checks changelogs for bad MD5Sums and preconditions before attempting a migration
     */
    public void validate() throws LiquibaseException {
        DatabaseChangeLog changeLog = getDatabaseChangeLog(true);
        changeLog.validate(database);
    }

    public void setChangeLogParameter(String key, Object value) {
        this.changeLogParameters.set(key, value);
    }

    public void setChangeExecListener(ChangeExecListener listener) {
        this.changeExecListener = listener;
    }

    public void setChangeLogSyncListener(ChangeLogSyncListener changeLogSyncListener) {
        this.changeLogSyncListener = changeLogSyncListener;
    }

    public DefaultChangeExecListener getDefaultChangeExecListener() {
        return defaultChangeExecListener;
    }

    /**
     * @deprecated Use {link {@link CommandScope(String)} to generateChangelog instead of this method.
     */
    @Deprecated
    @SafeVarargs
    public final void generateChangeLog(CatalogAndSchema catalogAndSchema, DiffToChangeLog changeLogWriter,
                                        PrintStream outputStream, Class<? extends DatabaseObject>... snapshotTypes)
            throws DatabaseException, IOException, ParserConfigurationException, CommandExecutionException {
        generateChangeLog(catalogAndSchema, changeLogWriter, outputStream, null, snapshotTypes);
    }

    /**
     * @deprecated Use {link {@link CommandScope(String)} to generateChangelog instead of this method.
     */
    @Deprecated
    @SafeVarargs
    public final void generateChangeLog(CatalogAndSchema catalogAndSchema, DiffToChangeLog changeLogWriter,
                                        PrintStream outputStream, ChangeLogSerializer changeLogSerializer,
                                        Class<? extends DatabaseObject>... snapshotTypes)
            throws DatabaseException, IOException, ParserConfigurationException, CommandExecutionException {
        Set<Class<? extends DatabaseObject>> finalCompareTypes = null;
        if ((snapshotTypes != null) && (snapshotTypes.length > 0)) {
            finalCompareTypes = new HashSet<>(Arrays.asList(snapshotTypes));
        }
        CompareControl compareControl = new CompareControl(new CompareControl.SchemaComparison[]{
                new CompareControl.SchemaComparison(catalogAndSchema, catalogAndSchema)
        }, finalCompareTypes);

        new CommandScope(GenerateChangelogCommandStep.COMMAND_NAME[0])
                .addArgumentValue(GenerateChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(PreCompareCommandStep.COMPARE_CONTROL_ARG, compareControl)
                .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, getDatabase())
                .addArgumentValue(PreCompareCommandStep.SNAPSHOT_TYPES_ARG, snapshotTypes)
                .setOutput(outputStream)
                .execute();
    }

    private void runInScope(Scope.ScopedRunner scopedRunner) throws LiquibaseException {
        Map<String, Object> scopeObjects = new HashMap<>();
        scopeObjects.put(Scope.Attr.database.name(), getDatabase());
        scopeObjects.put(Scope.Attr.resourceAccessor.name(), getResourceAccessor());

        try {
            Scope.child(scopeObjects, scopedRunner);
        } catch (Exception e) {
            if (e instanceof LiquibaseException) {
                throw (LiquibaseException) e;
            } else {
                throw new LiquibaseException(e);
            }
        }
    }

    @Override
    public void close() throws LiquibaseException {
        if (database != null) {
            database.close();
        }
    }
}
