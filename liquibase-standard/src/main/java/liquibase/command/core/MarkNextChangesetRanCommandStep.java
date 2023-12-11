package liquibase.command.core;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.RuntimeEnvironment;
import liquibase.Scope;
import liquibase.changelog.ChangeLogHistoryServiceFactory;
import liquibase.changelog.ChangeLogIterator;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.*;
import liquibase.changelog.visitor.ChangeLogSyncVisitor;
import liquibase.command.*;
import liquibase.command.core.helpers.DatabaseChangelogCommandStep;
import liquibase.database.Database;
import liquibase.executor.ExecutorService;

import java.util.Arrays;
import java.util.List;

public class MarkNextChangesetRanCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"markNextChangesetRan"};

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(Database.class, DatabaseChangeLog.class);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Marks the next change you apply as executed in your database");
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        try {
            CommandScope commandScope = resultsBuilder.getCommandScope();
            DatabaseChangeLog changeLog = (DatabaseChangeLog) commandScope.getDependency(DatabaseChangeLog.class);
            Database database = ((Database) commandScope.getDependency(Database.class));
            Contexts contexts = ((Contexts) commandScope.getDependency(Contexts.class));
            LabelExpression labelExpression = ((LabelExpression) commandScope.getDependency(LabelExpression.class));

            Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database).generateDeploymentId();

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
            Scope.getCurrentScope().getSingleton(ExecutorService.class).reset();
        }
    }
}
