package liquibase.command.core;

import liquibase.*;
import liquibase.changelog.*;
import liquibase.changelog.filter.*;
import liquibase.changelog.visitor.*;
import liquibase.command.*;
import liquibase.command.core.helpers.FastCheck;
import liquibase.command.core.helpers.HubHandler;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LockException;
import liquibase.executor.ExecutorService;
import liquibase.integration.commandline.ChangeExecListenerUtils;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.core.BufferedLogService;
import liquibase.logging.core.CompositeLogService;
import liquibase.logging.mdc.MdcKey;
import liquibase.logging.mdc.MdcObject;
import liquibase.logging.mdc.MdcValue;
import liquibase.util.ShowSummaryUtil;

import java.io.IOException;
import java.util.*;

import static liquibase.Liquibase.MSG_COULD_NOT_RELEASE_LOCK;

public class UpdateCommandStep extends AbstractCommandStep implements CleanUpCommandStep {

    public static final String[] LEGACY_COMMAND_NAME = {"migrate"};
    public static String[] COMMAND_NAME = {"update"};
    public static final String DEFAULT_CHANGE_EXEC_LISTENER_RESULT_KEY = "defaultChangeExecListener";

    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> LABEL_FILTER_ARG;
    public static final CommandArgumentDefinition<String> CONTEXTS_ARG;
    public static final CommandArgumentDefinition<String> CHANGE_EXEC_LISTENER_CLASS_ARG;
    public static final CommandArgumentDefinition<String> CHANGE_EXEC_LISTENER_PROPERTIES_FILE_ARG;
    public static final CommandArgumentDefinition<ChangeExecListener> CHANGE_EXEC_LISTENER_ARG;
    public static final CommandArgumentDefinition<UpdateSummaryEnum> SHOW_SUMMARY;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME, LEGACY_COMMAND_NAME);
        CHANGELOG_FILE_ARG = builder.argument(CommonArgumentNames.CHANGELOG_FILE, String.class)
                .required().description("The root changelog")
                .build();
        LABEL_FILTER_ARG = builder.argument("labelFilter", String.class)
                .addAlias("labels")
                .description("Changeset labels to match")
                .build();
        CONTEXTS_ARG = builder.argument("contexts", String.class)
                .description("Changeset contexts to match")
                .build();
        CHANGE_EXEC_LISTENER_CLASS_ARG = builder.argument("changeExecListenerClass", String.class)
                .description("Fully-qualified class which specifies a ChangeExecListener")
                .build();
        CHANGE_EXEC_LISTENER_PROPERTIES_FILE_ARG = builder.argument("changeExecListenerPropertiesFile", String.class)
                .description("Path to a properties file for the ChangeExecListenerClass")
                .build();
        CHANGE_EXEC_LISTENER_ARG = builder.argument("changeExecListener", ChangeExecListener.class)
                .hidden()
                .build();
        SHOW_SUMMARY = builder.argument("showSummary", UpdateSummaryEnum.class).description("Type of update results summary to show.  Values can be 'off', 'summary', or 'verbose'.")
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
                }).build();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME, LEGACY_COMMAND_NAME};
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
        return Arrays.asList(Database.class, LockService.class, DatabaseChangeLog.class, ChangeLogParameters.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_OPERATION, COMMAND_NAME[0]);
        Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_COMMAND_NAME, COMMAND_NAME[0]);
        CommandScope commandScope = resultsBuilder.getCommandScope();
        String changeLogFile = commandScope.getArgumentValue(CHANGELOG_FILE_ARG);
        Database database = (Database) commandScope.getDependency(Database.class);
        Contexts contexts = new Contexts(commandScope.getArgumentValue(CONTEXTS_ARG));
        LabelExpression labelExpression = new LabelExpression(commandScope.getArgumentValue(LABEL_FILTER_ARG));
        ChangeLogParameters changeLogParameters = (ChangeLogParameters) commandScope.getDependency(ChangeLogParameters.class);
        addCommandFiltersMdc(labelExpression, contexts);

        LockService lockService = (LockService) commandScope.getDependency(LockService.class);
        BufferedLogService bufferLog = new BufferedLogService();
        HubHandler hubHandler = null;
        DefaultChangeExecListener defaultChangeExecListener = new DefaultChangeExecListener();
        try {
            DatabaseChangeLog databaseChangeLog = (DatabaseChangeLog) commandScope.getDependency(DatabaseChangeLog.class);
            FastCheck fastCheck = new FastCheck();
            if (fastCheck.isUpToDate(database, databaseChangeLog, contexts, labelExpression)) {
                return;
            }
            ChangeLogHistoryService changelogService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
            Scope.getCurrentScope().addMdcValue(MdcKey.DEPLOYMENT_ID, changelogService.getDeploymentId());
            Scope.getCurrentScope().getLog(getClass()).info(String.format("Using deploymentId: %s", changelogService.getDeploymentId()));

            //Set up a "chain" of ChangeExecListeners. Starting with the custom change exec listener
            //then wrapping that in the DefaultChangeExecListener.
            ChangeExecListener listener = ChangeExecListenerUtils.getChangeExecListener(database,
                    Scope.getCurrentScope().getResourceAccessor(),
                    commandScope.getArgumentValue(CHANGE_EXEC_LISTENER_CLASS_ARG),
                    commandScope.getArgumentValue(CHANGE_EXEC_LISTENER_PROPERTIES_FILE_ARG));
            defaultChangeExecListener.addListener(listener);
            hubHandler = new HubHandler(database, databaseChangeLog, changeLogFile, defaultChangeExecListener);

            ChangeLogIterator changeLogIterator = getStandardChangelogIterator(database, contexts, labelExpression, databaseChangeLog);
            StatusVisitor statusVisitor = new StatusVisitor(database);
            ChangeLogIterator shouldRunIterator = getStatusChangelogIterator(database, contexts, labelExpression, databaseChangeLog);
            shouldRunIterator.run(statusVisitor, new RuntimeEnvironment(database, contexts, labelExpression));

            //Remember we built our hubHandler with our DefaultChangeExecListener so this HubChangeExecListener is delegating to them.
            ChangeExecListener hubChangeExecListener = hubHandler.startHubForUpdate(changeLogParameters, changeLogIterator);
            resultsBuilder.addResult(DEFAULT_CHANGE_EXEC_LISTENER_RESULT_KEY, defaultChangeExecListener);
            ChangeLogIterator runChangeLogIterator = getStandardChangelogIterator(database, contexts, labelExpression, databaseChangeLog);
            CompositeLogService compositeLogService = new CompositeLogService(true, bufferLog);
            HashMap<String, Object> scopeValues = new HashMap<>();
            scopeValues.put(Scope.Attr.logService.name(), compositeLogService);
            scopeValues.put("showSummary", commandScope.getArgumentValue(SHOW_SUMMARY));
            Scope.child(scopeValues, () -> {
                //If we are using hub, we want to use the HubChangeExecListener, which is wrapping all the others. Otherwise, use the default.
                ChangeExecListener listenerToUse = hubChangeExecListener != null ? hubChangeExecListener : defaultChangeExecListener;
                runChangeLogIterator.run(new UpdateVisitor(database, listenerToUse), new RuntimeEnvironment(database, contexts, labelExpression));
                ShowSummaryUtil.showUpdateSummary(databaseChangeLog, statusVisitor);
            });

            hubHandler.postUpdateHub(bufferLog);
            resultsBuilder.addResult("statusCode", 0);
            logDeploymentOutcomeMdc(defaultChangeExecListener, true);
        } catch (Exception e) {
            logDeploymentOutcomeMdc(defaultChangeExecListener, false);
            resultsBuilder.addResult("statusCode", 1);
            if (hubHandler != null) {
                hubHandler.postUpdateHubExceptionHandling(bufferLog, e.getMessage());
            }
            throw e;
        } finally {
            //TODO: We should be able to remove this once we get the rest of the update family
            // set up with the CommandFramework
            try {
                lockService.releaseLock();
            } catch (LockException e) {
                Scope.getCurrentScope().getLog(ChangelogSyncCommandStep.class).severe(MSG_COULD_NOT_RELEASE_LOCK, e);
            }
        }
    }

    @Override
    public void cleanUp(CommandResultsBuilder resultsBuilder) {
        LockServiceFactory.getInstance().resetAll();
        ChangeLogHistoryServiceFactory.getInstance().resetAll();
        Scope.getCurrentScope().getSingleton(ExecutorService.class).reset();
    }

    private void addCommandFiltersMdc(LabelExpression labelExpression, Contexts contexts) {
        String labelFilterMdc = labelExpression != null && labelExpression.getOriginalString() != null ? labelExpression.getOriginalString() : "";
        String contextFilterMdc = contexts != null ? contexts.toString() : "";
        Scope.getCurrentScope().addMdcValue(MdcKey.COMMAND_LABEL_FILTER, labelFilterMdc);
        Scope.getCurrentScope().addMdcValue(MdcKey.COMMAND_CONTEXT_FILTER, contextFilterMdc);
    }

    private void logDeploymentOutcomeMdc(DefaultChangeExecListener defaultListener, boolean success) throws IOException {
        int deployedChangeSetCount = defaultListener.getDeployedChangeSets().size();
        String successLog = "Update command completed successfully.";
        String failureLog = "Update command encountered an exception.";
        try (MdcObject deploymentOutcomeMdc = Scope.getCurrentScope().addMdcValue(MdcKey.DEPLOYMENT_OUTCOME, success ? MdcValue.COMMAND_SUCCESSFUL : MdcValue.COMMAND_FAILED);
             MdcObject deploymentOutcomeCountMdc = Scope.getCurrentScope().addMdcValue(MdcKey.DEPLOYMENT_OUTCOME_COUNT, String.valueOf(deployedChangeSetCount))) {
            Scope.getCurrentScope().getLog(getClass()).info(success ? successLog : failureLog);
        }
    }

    @Beta
    public static ChangeLogIterator getStandardChangelogIterator(Database database, Contexts contexts, LabelExpression labelExpression, DatabaseChangeLog changeLog) throws DatabaseException {
        return new ChangeLogIterator(changeLog,
                new ShouldRunChangeSetFilter(database),
                new ContextChangeSetFilter(contexts),
                new LabelChangeSetFilter(labelExpression),
                new DbmsChangeSetFilter(database),
                new IgnoreChangeSetFilter());
    }

    @Beta
    public static ChangeLogIterator getStatusChangelogIterator(Database database, Contexts contexts, LabelExpression labelExpression, DatabaseChangeLog changeLog) throws DatabaseException {
        return new StatusChangeLogIterator(changeLog,
                new ShouldRunChangeSetFilter(database),
                new ContextChangeSetFilter(contexts),
                new LabelChangeSetFilter(labelExpression),
                new DbmsChangeSetFilter(database),
                new IgnoreChangeSetFilter());
    }
}
