package liquibase.command.core;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.RuntimeEnvironment;
import liquibase.Scope;
import liquibase.changelog.*;
import liquibase.changelog.filter.*;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.changelog.visitor.ChangeLogSyncVisitor;
import liquibase.changelog.visitor.DefaultChangeExecListener;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.lockservice.LockService;

import java.util.Arrays;
import java.util.List;

public class ChangelogSyncCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"changelogSync"};

    private String tag = null;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
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
        final DatabaseChangeLog changeLog = (DatabaseChangeLog) commandScope.getDependency(DatabaseChangeLog.class);
        final ChangeLogParameters changeLogParameters = (ChangeLogParameters) commandScope.getDependency(ChangeLogParameters.class);

        ChangeLogIterator runChangeLogIterator = buildChangeLogIterator(tag, changeLog, changeLogParameters.getContexts(), changeLogParameters.getLabels(), database);
        runChangeLogIterator.run(new ChangeLogSyncVisitor(database, new DefaultChangeExecListener()),
                new RuntimeEnvironment(database, changeLogParameters.getContexts(), changeLogParameters.getLabels()));
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
