package liquibase.command.core;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.Scope;
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
        return new String[][] { COMMAND_NAME };
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
        liquibase.update(tag, contexts, labelExpression);
        changeLogService.reset();
        int changesetsToRollback = changeLogService.getRanChangeSets().size() - originalSize;
        Scope.getCurrentScope().getLog(getClass()).info(String.format("Rolling back %d changeset(s).", changesetsToRollback));
        liquibase.rollback(changesetsToRollback, null, contexts, labelExpression);
        liquibase.update(tag, contexts, labelExpression);
    }
}
