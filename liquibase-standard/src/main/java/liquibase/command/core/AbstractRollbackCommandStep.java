package liquibase.command.core;

import liquibase.RuntimeEnvironment;
import liquibase.Scope;
import liquibase.change.core.RawSQLChange;
import liquibase.changelog.*;
import liquibase.changelog.filter.*;
import liquibase.changelog.visitor.ChangeExecListener;
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

/**
 * AbstractRollbackCommandStep provides the common operations to all the rollback* commands.
 */
public abstract class AbstractRollbackCommandStep extends AbstractCommandStep {

    public static final CommandArgumentDefinition<String> ROLLBACK_SCRIPT_ARG;

    static {
        CommandBuilder builder = new CommandBuilder();
        ROLLBACK_SCRIPT_ARG = builder.argument("rollbackScript", String.class)
                .description("Rollback script to execute").build();
    }

    private enum RollbackMessageType {
        WILL_ROLLBACK, ROLLED_BACK, ROLLBACK_FAILED
    }

    protected void doRollback(CommandResultsBuilder resultsBuilder, List<RanChangeSet> ranChangeSetList,
                              ChangeSetFilter changeSetFilter) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        String rollbackScript = commandScope.getArgumentValue(ROLLBACK_SCRIPT_ARG);
        Scope.getCurrentScope().addMdcValue(MdcKey.ROLLBACK_SCRIPT, rollbackScript);

        ChangeLogParameters changeLogParameters = (ChangeLogParameters) commandScope.getDependency(ChangeLogParameters.class);

        DatabaseChangeLog databaseChangeLog = (DatabaseChangeLog) commandScope.getDependency(DatabaseChangeLog.class);
        Database database = (Database) commandScope.getDependency(Database.class);

        try {
            ChangeLogIterator logIterator = new ChangeLogIterator(ranChangeSetList, databaseChangeLog,
                    new AlreadyRanChangeSetFilter(ranChangeSetList),
                    new ContextChangeSetFilter(changeLogParameters.getContexts()),
                    new LabelChangeSetFilter(changeLogParameters.getLabels()),
                    new IgnoreChangeSetFilter(),
                    new DbmsChangeSetFilter(database),
                    changeSetFilter);

            doRollback(database, rollbackScript, logIterator, changeLogParameters, databaseChangeLog,
                    (ChangeExecListener) commandScope.getDependency(ChangeExecListener.class));
        }
        catch (Throwable t) {
            handleRollbackException(defineCommandNames()[0][0]);
            throw t;
        } finally {
            Scope.getCurrentScope().getMdcManager().remove(MdcKey.CHANGESETS_ROLLED_BACK);
        }
    }

    /**
     * Actually perform the rollback operation. Determining which changesets to roll back is the responsibility of the
     * logIterator.
     */
    public static void doRollback(Database database, String rollbackScript, ChangeLogIterator logIterator,ChangeLogParameters changeLogParameters,
                                  DatabaseChangeLog databaseChangeLog, ChangeExecListener changeExecListener) throws Exception {
        if (rollbackScript == null) {
            List<ChangesetsRolledback.ChangeSet> processedChangesets = new ArrayList<>();

            logIterator.run(new RollbackVisitor(database, changeExecListener, processedChangesets), new RuntimeEnvironment(database, changeLogParameters.getContexts(), changeLogParameters.getLabels()));

            Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESETS_ROLLED_BACK, new ChangesetsRolledback(processedChangesets), false);
        } else {
            List<ChangeSet> changeSets = determineRollbacks(database, logIterator, changeLogParameters);
            executeRollbackScript(database, rollbackScript, changeSets, databaseChangeLog, changeLogParameters, changeExecListener);
            removeRunStatus(changeSets, database);
            Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESETS_ROLLED_BACK, ChangesetsRolledback.fromChangesetList(changeSets));
        }
        try (MdcObject deploymentOutcomeMdc = Scope.getCurrentScope().getMdcManager().put(MdcKey.DEPLOYMENT_OUTCOME, MdcValue.COMMAND_SUCCESSFUL)) {
            Scope.getCurrentScope().getLog(AbstractRollbackCommandStep.class).info("Rollback command completed successfully.");
        }
    }

    private static List<ChangeSet> determineRollbacks(Database database, ChangeLogIterator logIterator, ChangeLogParameters changeLogParameters)
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
        }, new RuntimeEnvironment(database, changeLogParameters.getContexts(), changeLogParameters.getLabels()));
        return changeSetsToRollback;
    }

    private static void executeRollbackScript(Database database, String rollbackScript, List<ChangeSet> changeSets,
                                              DatabaseChangeLog changelog, ChangeLogParameters changeLogParameters, ChangeExecListener changeExecListener) throws LiquibaseException {
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
            sendRollbackMessages(changeSets, changelog, RollbackMessageType.WILL_ROLLBACK, database, changeExecListener, null);
            executor.execute(rollbackChange);
            sendRollbackMessages(changeSets, changelog, RollbackMessageType.ROLLED_BACK,  database, changeExecListener, null);
        } catch (DatabaseException e) {
            Scope.getCurrentScope().getLog(AbstractRollbackCommandStep.class).severe("Error executing rollback script: " + e.getMessage());
            if (changeExecListener != null) {
                sendRollbackMessages(changeSets, changelog, RollbackMessageType.ROLLBACK_FAILED,  database, changeExecListener, e);
            }
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

    private static void sendRollbackMessages(List<ChangeSet> changeSets,
                                             DatabaseChangeLog databaseChangeLog,
                                             RollbackMessageType messageType,
                                             Database database,
                                             ChangeExecListener changeExecListener,
                                             Exception exception) {
        changeSets.forEach(changeSet -> {
            if (messageType == RollbackMessageType.WILL_ROLLBACK) {
                changeExecListener.willRollback(changeSet, databaseChangeLog, database);
            }
            else if (messageType == RollbackMessageType.ROLLED_BACK) {
                final String message = "Rolled Back Changeset:" + changeSet.toString(false);
                Scope.getCurrentScope().getUI().sendMessage(message);
                Scope.getCurrentScope().getLog(AbstractRollbackCommandStep.class).info(message);
                changeExecListener.rolledBack(changeSet, databaseChangeLog, database);
            }
            else if (messageType == RollbackMessageType.ROLLBACK_FAILED) {
                final String message = "Failed rolling back Changeset:" + changeSet.toString(false);
                Scope.getCurrentScope().getUI().sendMessage(message);
                changeExecListener.rollbackFailed(changeSet, databaseChangeLog, database, exception);
            }
        });
    }

    private static void removeRunStatus(List<ChangeSet> changeSets, Database database) throws LiquibaseException {
        for (ChangeSet changeSet : changeSets) {
            database.removeRanStatus(changeSet);
            database.commit();
        }
    }

    private static void handleRollbackException(String operationName) {
        try (MdcObject deploymentOutcomeMdc = Scope.getCurrentScope().addMdcValue(MdcKey.DEPLOYMENT_OUTCOME, MdcValue.COMMAND_FAILED)) {
            Scope.getCurrentScope().getLog(AbstractRollbackCommandStep.class).info(operationName + " command encountered an exception.");
        }
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(DatabaseChangeLog.class, LockService.class, ChangeExecListener.class);
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Rollback changes made to the database based on the specific tag");
    }


}
