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
import liquibase.command.core.helpers.DatabaseChangelogCommandStep;
import liquibase.command.core.helpers.HubHandler;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.hub.listener.HubChangeExecListener;
import liquibase.lockservice.LockService;
import liquibase.logging.core.BufferedLogService;
import liquibase.logging.core.CompositeLogService;
import liquibase.logging.mdc.MdcKey;
import liquibase.logging.mdc.MdcObject;
import liquibase.logging.mdc.MdcValue;
import liquibase.util.StringUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ChangelogSyncCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"changelogSync"};

    public static final CommandArgumentDefinition<ChangeExecListener> HUB_CHANGE_EXEC_LISTENER_ARG;

    private String tag = null;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);

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
        return Arrays.asList(LockService.class, DatabaseChangeLog.class, ChangeLogParameters.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        final CommandScope commandScope = resultsBuilder.getCommandScope();
        final Database database = (Database) commandScope.getDependency(Database.class);
        final String changeLogFile = commandScope.getArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG);
        final DatabaseChangeLog changeLog = (DatabaseChangeLog) commandScope.getDependency(DatabaseChangeLog.class);
        final ChangeLogParameters changeLogParameters = (ChangeLogParameters) commandScope.getDependency(ChangeLogParameters.class);

        BufferedLogService bufferLog = new BufferedLogService();
        HubHandler hubHandler = null;

        try {
            ChangeLogIterator runChangeLogIterator = buildChangeLogIterator(tag, changeLog, changeLogParameters.getContexts(), changeLogParameters.getLabels(), database);
            CompositeLogService compositeLogService = new CompositeLogService(true, bufferLog);

            hubHandler = new HubHandler(database, changeLog, changeLogFile, commandScope.getArgumentValue(HUB_CHANGE_EXEC_LISTENER_ARG));
            HubChangeExecListener changeLogSyncListener = hubHandler.startHubForChangelogSync(changeLogParameters, tag,
                    buildChangeLogIterator(tag, changeLog, changeLogParameters.getContexts(), changeLogParameters.getLabels(), database));

            AtomicInteger changesetCount = new AtomicInteger(0);
            Map<String, Object> scopeVars = new HashMap<>(2);
            scopeVars.put(Scope.Attr.logService.name(), compositeLogService);
            scopeVars.put("changesetCount", changesetCount);
            Scope.child(scopeVars, () ->
                    runChangeLogIterator.run(new ChangeLogSyncVisitor(database, changeLogSyncListener),
                    new RuntimeEnvironment(database, changeLogParameters.getContexts(), changeLogParameters.getLabels())));
            Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESET_SYNC_COUNT, changesetCount.toString());

            hubHandler.postUpdateHub(bufferLog);
            if (StringUtil.isNotEmpty(changeLog.getLogicalFilePath())) {
                Scope.getCurrentScope().addMdcValue(MdcKey.CHANGELOG_FILE, changeLog.getLogicalFilePath());
            } else {
                Scope.getCurrentScope().addMdcValue(MdcKey.CHANGELOG_FILE, changeLogFile);
            }
            try (MdcObject changelogSyncOutcome = Scope.getCurrentScope().addMdcValue(MdcKey.CHANGELOG_SYNC_OUTCOME, MdcValue.COMMAND_SUCCESSFUL)) {
                Scope.getCurrentScope().getLog(getClass()).info("Finished executing " + defineCommandNames()[0][0] + " command");
            }
        } catch (Exception e) {
            if (hubHandler != null) {
                hubHandler.postUpdateHubExceptionHandling(bufferLog, e.getMessage());
            }
            try (MdcObject changelogSyncOutcome = Scope.getCurrentScope().addMdcValue(MdcKey.CHANGELOG_SYNC_OUTCOME, MdcValue.COMMAND_FAILED)) {
                Scope.getCurrentScope().getLog(getClass()).warning("Failed executing " + defineCommandNames()[0][0] + " command");
            }
            throw e;
        }
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

    /**
     * Tag value can be set by subclasses that implements "SyncToTag"
     */
    protected void setTag(String tag) {
        this.tag = tag;
    }
}
