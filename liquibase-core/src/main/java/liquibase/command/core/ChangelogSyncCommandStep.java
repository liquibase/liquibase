package liquibase.command.core;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.RuntimeEnvironment;
import liquibase.Scope;
import liquibase.changelog.*;
import liquibase.changelog.filter.*;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.changelog.visitor.ChangeLogSyncVisitor;
import liquibase.command.*;
import liquibase.command.core.helpers.HubHandler;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.executor.ExecutorService;
import liquibase.hub.listener.HubChangeExecListener;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.core.BufferedLogService;
import liquibase.logging.core.CompositeLogService;
import liquibase.logging.mdc.MdcKey;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.resource.ResourceAccessor;

import java.util.Collections;
import java.util.List;

import static liquibase.Liquibase.MSG_COULD_NOT_RELEASE_LOCK;

public class ChangelogSyncCommandStep extends AbstractCommandStep implements CleanUpCommandStep {

    public static final String[] COMMAND_NAME = {"changelogSync"};

    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> LABEL_FILTER_ARG;
    public static final CommandArgumentDefinition<String> CONTEXTS_ARG;

    public static final CommandArgumentDefinition<ChangeExecListener> HUB_CHANGE_EXEC_LISTENER_ARG;
    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        CHANGELOG_FILE_ARG = builder.argument(CommonArgumentNames.CHANGELOG_FILE, String.class).required()
            .description("The root changelog file").build();
        LABEL_FILTER_ARG = builder.argument("labelFilter", String.class)
                .addAlias("labels")
                .description("Label expression to use for filtering which changes to mark as executed").build();
        CONTEXTS_ARG = builder.argument("contexts", String.class)
            .description("Context string to use for filtering which changes to mark as executed").build();

        HUB_CHANGE_EXEC_LISTENER_ARG = builder.argument("changeExecListener", ChangeExecListener.class)
                .hidden().description("Class that will be used to listen to changes to be sent to Hub (if required)").build();

    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME};
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Marks all changes as executed in the database");
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Collections.singletonList(Database.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        final CommandScope commandScope = resultsBuilder.getCommandScope();
        final Database database = (Database) commandScope.getDependency(Database.class);
        final String changeLogFile = commandScope.getArgumentValue(CHANGELOG_FILE_ARG);
        final ChangeLogParameters changeLogParameters = new ChangeLogParameters(database);
        changeLogParameters.setContexts(new Contexts(commandScope.getArgumentValue(CONTEXTS_ARG)));
        changeLogParameters.setLabels(new LabelExpression(commandScope.getArgumentValue(LABEL_FILTER_ARG)));

        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.waitForLock();

        BufferedLogService bufferLog = new BufferedLogService();
        HubHandler hubHandler = null;

        try {
            DatabaseChangeLog changeLog = getDatabaseChangeLog(changeLogFile, changeLogParameters);
            checkLiquibaseTables(true, changeLog, changeLogParameters.getContexts(), changeLogParameters.getLabels(), database);
            ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database).generateDeploymentId();

            changeLog.validate(database, changeLogParameters.getContexts(), changeLogParameters.getLabels());

            ChangeLogIterator runChangeLogIterator = buildChangeLogIterator(null, changeLog, changeLogParameters.getContexts(), changeLogParameters.getLabels(), database);
            CompositeLogService compositeLogService = new CompositeLogService(true, bufferLog);

            hubHandler = new HubHandler(database, changeLog, changeLogFile, commandScope.getArgumentValue(HUB_CHANGE_EXEC_LISTENER_ARG));
            HubChangeExecListener changeLogSyncListener = hubHandler.startHubForChangelogSync(changeLogParameters, null,
                    buildChangeLogIterator(null, changeLog, changeLogParameters.getContexts(), changeLogParameters.getLabels(), database));

            Scope.child(Scope.Attr.logService.name(), compositeLogService, () ->
                    runChangeLogIterator.run(new ChangeLogSyncVisitor(database, changeLogSyncListener),
                    new RuntimeEnvironment(database, changeLogParameters.getContexts(), changeLogParameters.getLabels())));

            hubHandler.postUpdateHub(bufferLog);
        } catch (Exception e) {
            hubHandler.postUpdateHubExceptionHandling(bufferLog, e.getMessage());
            throw e;
        } finally {
            try {
                lockService.releaseLock();
            } catch (LockException e) {
                Scope.getCurrentScope().getLog(ChangelogSyncCommandStep.class).severe(MSG_COULD_NOT_RELEASE_LOCK, e);
            }
        }
    }

    private DatabaseChangeLog getDatabaseChangeLog(String changeLogFile, ChangeLogParameters changeLogParameters) throws LiquibaseException {
        ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        Scope.getCurrentScope().addMdcValue(MdcKey.CHANGELOG_FILE, changeLogFile);
        ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor);
        if (parser instanceof XMLChangeLogSAXParser) {
            ((XMLChangeLogSAXParser) parser).setShouldWarnOnMismatchedXsdVersion(false);
        }
        return parser.parse(changeLogFile, changeLogParameters, resourceAccessor);
    }

    private void checkLiquibaseTables(boolean updateExistingNullChecksums, DatabaseChangeLog databaseChangeLog,
                                     Contexts contexts, LabelExpression labelExpression, Database database) throws LiquibaseException {
        ChangeLogHistoryService changeLogHistoryService = ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(database);
        changeLogHistoryService.init();
        if (updateExistingNullChecksums) {
            changeLogHistoryService.upgradeChecksums(databaseChangeLog, contexts, labelExpression);
        }
        LockServiceFactory.getInstance().getLockService(database).init();
    }

    private ChangeLogIterator buildChangeLogIterator(String tag, DatabaseChangeLog changeLog, Contexts contexts,
                                                       LabelExpression labelExpression, Database database) throws DatabaseException {

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

    @Override
    public void cleanUp(CommandResultsBuilder resultsBuilder) {
        LockServiceFactory.getInstance().resetAll();
        ChangeLogHistoryServiceFactory.getInstance().resetAll();
        Scope.getCurrentScope().getSingleton(ExecutorService.class).reset();
    }
}
