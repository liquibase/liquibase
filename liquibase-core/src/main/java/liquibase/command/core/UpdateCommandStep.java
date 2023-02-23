package liquibase.command.core;

import liquibase.*;
import liquibase.changelog.*;
import liquibase.changelog.filter.*;
import liquibase.changelog.visitor.*;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.hub.*;
import liquibase.hub.model.Connection;
import liquibase.hub.model.HubChangeLog;
import liquibase.hub.model.Operation;
import liquibase.integration.commandline.ChangeExecListenerUtils;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.core.BufferedLogService;
import liquibase.logging.core.CompositeLogService;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.resource.ResourceAccessor;
import liquibase.util.StringUtil;

import java.util.*;

import static liquibase.Liquibase.MSG_COULD_NOT_RELEASE_LOCK;

public class UpdateCommandStep extends AbstractCommandStep {

    public static final String[] LEGACY_COMMAND_NAME = {"migrate"};
    public static String[] COMMAND_NAME = {"update"};

    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> LABEL_FILTER_ARG;
    public static final CommandArgumentDefinition<String> CONTEXTS_ARG;
    public static final CommandArgumentDefinition<String> CHANGE_EXEC_LISTENER_CLASS_ARG;
    public static final CommandArgumentDefinition<String> CHANGE_EXEC_LISTENER_PROPERTIES_FILE_ARG;
    public static final CommandArgumentDefinition<UpdateSummaryEnum> SHOW_SUMMARY;
    private final Map<String, Boolean> upToDateFastCheck = new HashMap<>();
    private UUID hubConnectionId;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME, LEGACY_COMMAND_NAME);
        CHANGELOG_FILE_ARG = builder.argument(CommonArgumentNames.CHANGELOG_FILE, String.class).required()
                .description("The root changelog").build();
        LABEL_FILTER_ARG = builder.argument("labelFilter", String.class)
                .addAlias("labels")
                .description("Changeset labels to match").build();
        CONTEXTS_ARG = builder.argument("contexts", String.class)
                .description("Changeset contexts to match").build();
        CHANGE_EXEC_LISTENER_CLASS_ARG = builder.argument("changeExecListenerClass", String.class)
                .description("Fully-qualified class which specifies a ChangeExecListener").build();
        CHANGE_EXEC_LISTENER_PROPERTIES_FILE_ARG = builder.argument("changeExecListenerPropertiesFile", String.class)
                .description("Path to a properties file for the ChangeExecListenerClass").build();
        SHOW_SUMMARY = builder.argument("showSummary", UpdateSummaryEnum.class)
                .description("Type of update results summary to show.  Values can be 'off', 'summary', or 'verbose'.")
                .defaultValue(UpdateSummaryEnum.OFF)
                .hidden()
                .setValueHandler(value -> {
                    if (value == null) {
                        return null;
                    }
                    if (value instanceof String && !value.equals("")) {
                        final List<String> validValues = Arrays.asList("OFF", "SUMMARY", "VERBOSE");
                        if (!validValues.contains(((String) value).toUpperCase())) {
                            throw new IllegalArgumentException("Illegal value for `showUpdateSummary'.  Valid values are 'OFF', 'SUMMARY', or 'VERBOSE'");
                        }
                        return UpdateSummaryEnum.valueOf(((String) value).toUpperCase());
                    } else if (value instanceof UpdateSummaryEnum) {
                        return (UpdateSummaryEnum) value;
                    }
                    return null;
                })
                .build();
    }

    public UUID getHubConnectionId() {
        return hubConnectionId;
    }

    public void setHubConnectionId(UUID hubConnectionId) {
        this.hubConnectionId = hubConnectionId;
    }


    @Override
    public String[][] defineCommandNames() {
        return new String[][]{
                COMMAND_NAME,
                LEGACY_COMMAND_NAME
        };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Deploy any changes in the changelog file that have not been deployed");

        if (commandDefinition.is(LEGACY_COMMAND_NAME)) {
            commandDefinition.setHidden(true);
        }

    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(Database.class, LockService.class);
    }

    @Override
    public List<Class<?>> providedDependencies() {
        return Collections.singletonList(UpdateCommandStep.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        String changeLogFile = commandScope.getArgumentValue(CHANGELOG_FILE_ARG);
        Database database = (Database) commandScope.getDependency(Database.class);
        Contexts contexts = new Contexts(commandScope.getArgumentValue(CONTEXTS_ARG));
        LabelExpression labelExpression = new LabelExpression(commandScope.getArgumentValue(LABEL_FILTER_ARG));
        ChangeLogParameters changeLogParameters = new ChangeLogParameters(database);
        DatabaseChangeLog changeLog = getDatabaseChangeLog(changeLogFile, changeLogParameters, true);
        if (isUpToDateFastCheck(database, changeLog, contexts, labelExpression)) {
            Scope.getCurrentScope().getUI().sendMessage("Database is up to date, no changesets to execute");
            return;
        }
        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.init();

        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        Operation updateOperation = null;
        BufferedLogService bufferLog = new BufferedLogService();
        HubUpdater hubUpdater = null;
        try {
            checkLiquibaseTables(database, true, changeLog, contexts, labelExpression);
            ChangeLogHistoryService changelogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
            changelogService.generateDeploymentId();
            changeLog.validate(database, contexts, labelExpression);

            //
            // Let the user know that they can register for Hub
            //
            hubUpdater = new HubUpdater(new Date(), changeLog, database);
            hubUpdater.register(changeLogFile);

            //
            // Create or retrieve the Connection if this is not SQL generation
            // Make sure the Hub is available here by checking the return
            // We do not need a connection if we are using a LoggingExecutor
            //
            ChangeLogIterator changeLogIterator = getStandardChangelogIterator(database, contexts, labelExpression, false, changeLog);

            //
            // Iterate to find the change sets which will be skipped
            //
            StatusVisitor statusVisitor = new StatusVisitor(database);
            ChangeLogIterator shouldRunIterator = getStandardChangelogIterator(database, contexts, labelExpression, false, changeLog);
            shouldRunIterator.run(statusVisitor, new RuntimeEnvironment(database, contexts, labelExpression));

            Connection connection = getConnection(database, changeLog);
            if (connection != null) {
                updateOperation =
                        hubUpdater.preUpdateHub("UPDATE", "update", connection, changeLogFile, contexts, labelExpression, changeLogIterator);
            }

            //
            // Make sure we don't already have a listener
//            //
//            if (connection != null) {
//                changeExecListener = new HubChangeExecListener(updateOperation, changeExecListener);
//            }

            //
            // Create another iterator to run
            //
            ChangeLogIterator runChangeLogIterator = getStandardChangelogIterator(database, contexts, labelExpression, false, changeLog);
            CompositeLogService compositeLogService = new CompositeLogService(true, bufferLog);
            Scope.child(Scope.Attr.logService.name(), compositeLogService, () -> {
                runChangeLogIterator.run(createUpdateVisitor(database,
                                commandScope.getArgumentValue(CHANGE_EXEC_LISTENER_CLASS_ARG),
                                commandScope.getArgumentValue(CHANGE_EXEC_LISTENER_PROPERTIES_FILE_ARG)),
                        new RuntimeEnvironment(database, contexts, labelExpression));
            });
//TODO
//            showUpdateSummary(changeLog, statusVisitor);

            hubUpdater.postUpdateHub(updateOperation, bufferLog);
        } catch (Throwable e) {
            if (hubUpdater != null) {
                hubUpdater.postUpdateHubExceptionHandling(updateOperation, bufferLog, e.getMessage());
            }
            throw e;
        } finally {
            database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
            try {
                lockService.releaseLock();
            } catch (LockException e) {
                Scope.getCurrentScope().getLog(getClass()).severe(MSG_COULD_NOT_RELEASE_LOCK, e);
            }
            resetServices();
        }
    }

    protected boolean isUpToDateFastCheck(Database database, DatabaseChangeLog databaseChangeLog,  Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        String cacheKey = contexts + "/" + labelExpression;
        if (!this.upToDateFastCheck.containsKey(cacheKey)) {
            try {
                if (listUnrunChangeSets(database, databaseChangeLog, contexts, labelExpression, false).isEmpty()) {
                    Scope.getCurrentScope().getLog(getClass()).fine("Fast check found no un-run changesets");
                    upToDateFastCheck.put(cacheKey, true);
                } else {
                    upToDateFastCheck.put(cacheKey, false);
                }
            } catch (DatabaseException e) {
                Scope.getCurrentScope().getLog(getClass()).info("Error querying Liquibase tables, disabling fast check for this execution. Reason: " + e.getMessage());
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

    public List<ChangeSet> listUnrunChangeSets(Database database, DatabaseChangeLog databaseChangeLog, Contexts contexts, LabelExpression labels, boolean checkLiquibaseTables) throws LiquibaseException {
        ListVisitor visitor = new ListVisitor();
        if (checkLiquibaseTables) {
            checkLiquibaseTables(database,true, databaseChangeLog, contexts, labels);
        }

        databaseChangeLog.validate(database, contexts, labels);

        ChangeLogIterator logIterator = getStandardChangelogIterator(database, contexts, labels, false, databaseChangeLog);

        logIterator.run(visitor, new RuntimeEnvironment(database, contexts, labels));
        return visitor.getSeenChangeSets();
    }

    private DatabaseChangeLog getDatabaseChangeLog(String changeLogFile, ChangeLogParameters changeLogParameters, boolean shouldWarnOnMismatchedXsdVersion) throws LiquibaseException {
        ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        if (changeLogFile != null) {
            ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor);
            if (parser instanceof XMLChangeLogSAXParser) {
                ((XMLChangeLogSAXParser) parser).setShouldWarnOnMismatchedXsdVersion(shouldWarnOnMismatchedXsdVersion);
            }
            return parser.parse(changeLogFile, changeLogParameters, resourceAccessor);
        }

        return null;
    }

    public void checkLiquibaseTables(Database database, boolean updateExistingNullChecksums, DatabaseChangeLog databaseChangeLog,
                                     Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        ChangeLogHistoryService changeLogHistoryService =
                ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
        changeLogHistoryService.init();
        if (updateExistingNullChecksums) {
            changeLogHistoryService.upgradeChecksums(databaseChangeLog, contexts, labelExpression);
        }
        LockServiceFactory.getInstance().getLockService(database).init();
    }

    /**
     * Return a ChangeLogIterator constructed with standard filters
     *
     * @param contexts          Contexts to filter for
     * @param labelExpression   Labels to filter for
     * @param collectAllReasons Flag to control whether all skip reasons are accumulated
     *                          default value is false to only gather the first
     * @param changeLog         The changelog to process
     * @return ChangeLogIterator
     * @throws DatabaseException
     */
    protected ChangeLogIterator getStandardChangelogIterator(Database database, Contexts contexts, LabelExpression labelExpression,
                                                             boolean collectAllReasons,
                                                             DatabaseChangeLog changeLog) throws DatabaseException {
        return new ChangeLogIterator(changeLog,
                collectAllReasons,
                new ShouldRunChangeSetFilter(database),
                new ContextChangeSetFilter(contexts),
                new LabelChangeSetFilter(labelExpression),
                new DbmsChangeSetFilter(database),
                new IgnoreChangeSetFilter());
    }

    /**
     *
     * Create or retrieve the Connection object
     *
     * @param   changeLog              Database changelog
     * @return  Connection
     * @throws LiquibaseHubException  Thrown by HubService
     *
     */
    public Connection getConnection(Database database, DatabaseChangeLog changeLog) throws LiquibaseHubException {
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
            exampleConnection.setJdbcUrl(database.getConnection().getURL());
            connection = hubService.getConnection(exampleConnection, true);
        } else {
            connection = hubService.getConnection(new Connection().setId(getHubConnectionId()), true);
        }
        return connection;
    }

    protected UpdateVisitor createUpdateVisitor(Database database, String changeExecListenerClass, String changeExecListenerPropertiesFile) throws Exception {
        ChangeExecListener listener = ChangeExecListenerUtils.getChangeExecListener(
                database, Scope.getCurrentScope().getResourceAccessor(),
                changeExecListenerClass, changeExecListenerPropertiesFile);
        DefaultChangeExecListener defaultChangeExecListener = new DefaultChangeExecListener(listener);
        return new UpdateVisitor(database, defaultChangeExecListener);
    }

    protected void resetServices() {
        LockServiceFactory.getInstance().resetAll();
        ChangeLogHistoryServiceFactory.getInstance().resetAll();
        Scope.getCurrentScope().getSingleton(ExecutorService.class).reset();
    }
}
