package liquibase.command.core;

import liquibase.*;
import liquibase.changelog.ChangeLogHistoryService;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.command.*;
import liquibase.database.Database;

import java.util.Arrays;
import java.util.List;

public class UpdateTestingRollbackCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"updateTestingRollback"};

    public static final CommandArgumentDefinition<String> TAG_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        TAG_ARG = builder.argument("tag", String.class)
                .hidden()
                .build();
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(Database.class, DatabaseChangeLog.class, ChangeExecListener.class);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME};
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Updates database, then rolls back changes before updating again. Useful for testing rollback support");
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        Database database = (Database) commandScope.getDependency(Database.class);
        Contexts contexts = (Contexts) commandScope.getDependency(Contexts.class);
        LabelExpression labelExpression = (LabelExpression) commandScope.getDependency(LabelExpression.class);
        ChangeExecListener changeExecListener = (ChangeExecListener) commandScope.getDependency(ChangeExecListener.class);
        String tag = commandScope.getArgumentValue(TAG_ARG);
        DatabaseChangeLog databaseChangeLog = (DatabaseChangeLog) commandScope.getDependency(DatabaseChangeLog.class);
        Liquibase liquibase = new Liquibase(databaseChangeLog, Scope.getCurrentScope().getResourceAccessor(), database);
        liquibase.setChangeExecListener(changeExecListener);

        ChangeLogHistoryService changeLogService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database);
        int originalSize = changeLogService.getRanChangeSets().size();
        initialUpdate(liquibase, tag, contexts, labelExpression);
        changeLogService.reset();
        int changesetsToRollback = changeLogService.getRanChangeSets().size() - originalSize;
        Scope.getCurrentScope().getLog(getClass()).info(String.format("Rolling back %d changeset(s).", changesetsToRollback));
        rollbackUpdate(liquibase, changesetsToRollback, contexts, labelExpression);
        finalUpdate(liquibase, tag, contexts, labelExpression);
    }

    /**
     * Runs the first update operation in the update-testing-rollback chain
     *
     * @param liquibase       the liquibase object to used for running operations
     * @param tag             the tag to update to if available
     * @param contexts        the contexts to filter on if available
     * @param labelExpression the labels to filter on if available
     * @throws Exception if there was a problem with the update
     */
    @Beta
    protected void initialUpdate(Liquibase liquibase, String tag, Contexts contexts, LabelExpression labelExpression) throws Exception {
        liquibase.update(tag, contexts, labelExpression);
    }

    /**
     * Runs the rollback operation in the update-testing-rollback chain which rolls back the initial update
     *
     * @param liquibase            the liquibase object to used for running operations
     * @param changesetsToRollback the number of changes to roll back
     * @param contexts             the contexts to filter on if available
     * @param labelExpression      the labels to filter on if available
     * @throws Exception if there was a problem with the rollback
     */
    @Beta
    protected void rollbackUpdate(Liquibase liquibase, int changesetsToRollback, Contexts contexts, LabelExpression labelExpression) throws Exception {
        liquibase.rollback(changesetsToRollback, null, contexts, labelExpression);
    }

    /**
     * Runs the final update operation in the update-testing-rollback chain
     *
     * @param liquibase       the liquibase object to used for running operations
     * @param tag             the tag to update to if available
     * @param contexts        the contexts to filter on if available
     * @param labelExpression the labels to filter on if available
     * @throws Exception if there was a problem with the update
     */
    @Beta
    protected void finalUpdate(Liquibase liquibase, String tag, Contexts contexts, LabelExpression labelExpression) throws Exception {
        liquibase.update(tag, contexts, labelExpression);
    }
}
