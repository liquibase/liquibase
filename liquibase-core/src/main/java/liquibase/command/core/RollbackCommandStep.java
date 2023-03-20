package liquibase.command.core;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.RuntimeEnvironment;
import liquibase.Scope;
import liquibase.change.core.RawSQLChange;
import liquibase.changelog.*;
import liquibase.changelog.filter.*;
import liquibase.changelog.visitor.ChangeSetVisitor;
import liquibase.changelog.visitor.RollbackVisitor;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.lockservice.LockService;
import liquibase.logging.mdc.MdcKey;
import liquibase.logging.mdc.MdcObject;
import liquibase.logging.mdc.MdcValue;
import liquibase.logging.mdc.customobjects.ChangesetsRolledback;
import liquibase.resource.Resource;
import liquibase.util.StreamUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class RollbackCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"rollback"};

    public static final CommandArgumentDefinition<String> ROLLBACK_SCRIPT_ARG;
    public static final CommandArgumentDefinition<String> TAG_ARG;
    public static final CommandArgumentDefinition<String> CHANGE_EXEC_LISTENER_CLASS_ARG;
    public static final CommandArgumentDefinition<String> CHANGE_EXEC_LISTENER_PROPERTIES_FILE_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);

        ROLLBACK_SCRIPT_ARG = builder.argument("rollbackScript", String.class)
                .description("Rollback script to execute").build();
        TAG_ARG = builder.argument("tag", String.class).required()
            .description("Tag to rollback to").build();
        CHANGE_EXEC_LISTENER_CLASS_ARG = builder.argument("changeExecListenerClass", String.class)
            .description("Fully-qualified class which specifies a ChangeExecListener").build();
        CHANGE_EXEC_LISTENER_PROPERTIES_FILE_ARG = builder.argument("changeExecListenerPropertiesFile", String.class)
            .description("Path to a properties file for the ChangeExecListenerClass").build();
    }

    public String operationCommand = "rollback";

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_OPERATION, COMMAND_NAME[0]);
        Scope.getCurrentScope().addMdcValue(MdcKey.LIQUIBASE_COMMAND_NAME, COMMAND_NAME[0]);
        CommandScope commandScope = resultsBuilder.getCommandScope();

        String tagToRollBackTo = commandScope.getArgumentValue(TAG_ARG);
        Scope.getCurrentScope().addMdcValue(MdcKey.ROLLBACK_TO_TAG, tagToRollBackTo);

        String rollbackScript = commandScope.getArgumentValue(ROLLBACK_SCRIPT_ARG);
        Scope.getCurrentScope().addMdcValue(MdcKey.ROLLBACK_SCRIPT, rollbackScript);

        ChangeLogParameters changeLogParameters = (ChangeLogParameters) commandScope.getDependency(ChangeLogParameters.class);

        DatabaseChangeLog databaseChangeLog = (DatabaseChangeLog) commandScope.getDependency(DatabaseChangeLog.class);
        Database database = (Database) commandScope.getDependency(Database.class);

        //final String operationCommand = "rollback";


        try {
            List<RanChangeSet> ranChangeSetList = database.getRanChangeSetList();
            // Create another iterator to run
            ChangeLogIterator logIterator = new ChangeLogIterator(ranChangeSetList, databaseChangeLog,
                    new AfterTagChangeSetFilter(tagToRollBackTo, ranChangeSetList),
                    new AlreadyRanChangeSetFilter(ranChangeSetList),
                    new ContextChangeSetFilter(changeLogParameters.getContexts()),
                    new LabelChangeSetFilter(changeLogParameters.getLabels()),
                    new IgnoreChangeSetFilter(),
                    new DbmsChangeSetFilter(database));

            doRollback(database, rollbackScript, logIterator, changeLogParameters.getContexts(), changeLogParameters.getLabels(), databaseChangeLog, changeLogParameters);
        }
        catch (Throwable t) {
            handleRollbackException(t, operationCommand);
            throw t;
        } finally {
            Scope.getCurrentScope().getMdcManager().remove(MdcKey.CHANGESETS_ROLLED_BACK);
        }
    }

    /**
     * Actually perform the rollback operation. Determining which changesets to roll back is the responsibility of the
     * logIterator.
     */
    public static void doRollback(Database database, String rollbackScript, ChangeLogIterator logIterator, Contexts contexts, LabelExpression labelExpression, DatabaseChangeLog databaseChangeLog, ChangeLogParameters changeLogParameters) throws Exception {
        if (rollbackScript == null) {
            List<ChangesetsRolledback.ChangeSet> processedChangesets = new ArrayList<>();

            logIterator.run(new RollbackVisitor(database, null, processedChangesets), new RuntimeEnvironment(database, contexts, labelExpression));

            Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESETS_ROLLED_BACK, new ChangesetsRolledback(processedChangesets), false);
        } else {
            List<ChangeSet> changeSets = determineRollbacks(database, logIterator, contexts, labelExpression);
            executeRollbackScript(database, rollbackScript, changeSets, databaseChangeLog, changeLogParameters);
            removeRunStatus(changeSets, database);
            Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESETS_ROLLED_BACK, ChangesetsRolledback.fromChangesetList(changeSets));
        }
        try (MdcObject deploymentOutcomeMdc = Scope.getCurrentScope().getMdcManager().put(MdcKey.DEPLOYMENT_OUTCOME, MdcValue.COMMAND_SUCCESSFUL)) {
            Scope.getCurrentScope().getLog(RollbackCommandStep.class).info("Rollback command completed successfully.");
        }
    }

    private static List<ChangeSet> determineRollbacks(Database database, ChangeLogIterator logIterator, Contexts contexts, LabelExpression labelExpression)
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

    private static void executeRollbackScript(Database database, String rollbackScript, List<ChangeSet> changeSets,
                                              DatabaseChangeLog changelog, ChangeLogParameters changeLogParameters) throws LiquibaseException {
        final Executor executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
        String rollbackScriptContents;
        try {
            Resource resource = Scope.getCurrentScope().getResourceAccessor().get(rollbackScript);
            if (resource == null) {
                throw new LiquibaseException("WARNING: The rollback script '" + rollbackScript + "' was not located.  Please check your parameters. No rollback was performed");
            }
            try (InputStream stream = resource.openInputStream()) {
                rollbackScriptContents = StreamUtil.readStreamAsString(stream);
            }
        } catch (IOException e) {
            throw new LiquibaseException("Error reading rollbackScript " + executor + ": " + e.getMessage());
        }

        // Expand changelog properties
        rollbackScriptContents = changeLogParameters.expandExpressions(rollbackScriptContents, changelog);

        RawSQLChange rollbackChange = buildRawSQLChange(rollbackScriptContents);
        try {
            executor.execute(rollbackChange);
            sendRollbackMessages(changeSets);
        } catch (DatabaseException e) {
            Scope.getCurrentScope().getLog(RollbackCommandStep.class).severe("Error executing rollback script: " + e.getMessage());
            throw new DatabaseException("Error executing rollback script", e);
        }
        database.commit();
    }

    protected static RawSQLChange buildRawSQLChange(String rollbackScriptContents) {
        RawSQLChange rollbackChange = new RawSQLChange(rollbackScriptContents);
        rollbackChange.setSplitStatements(true);
        rollbackChange.setStripComments(true);
        return rollbackChange;
    }

    private static void sendRollbackMessages(List<ChangeSet> changeSets) {
        for (ChangeSet changeSet : changeSets) {
            Scope.getCurrentScope().getUI().sendMessage(String.format("Rolled Back Changeset: %s", changeSet.toString(false)));
        }
    }

    private static void removeRunStatus(List<ChangeSet> changeSets, Database database) throws LiquibaseException {
        for (ChangeSet changeSet : changeSets) {
            database.removeRanStatus(changeSet);
            database.commit();
        }
    }

    private static void handleRollbackException(Throwable t, String operationName) throws IOException {
        try (MdcObject deploymentOutcomeMdc = Scope.getCurrentScope().addMdcValue(MdcKey.DEPLOYMENT_OUTCOME, MdcValue.COMMAND_FAILED)) {
            Scope.getCurrentScope().getLog(RollbackCommandStep.class).info(operationName + " command encountered an exception.");
        }
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(DatabaseChangeLog.class, LockService.class);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Rollback changes made to the database based on the specific tag");
    }


}
