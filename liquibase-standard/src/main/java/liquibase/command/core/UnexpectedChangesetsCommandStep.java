package liquibase.command.core;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.RuntimeEnvironment;
import liquibase.changelog.ChangeLogIterator;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;
import liquibase.changelog.filter.ContextChangeSetFilter;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.changelog.filter.IgnoreChangeSetFilter;
import liquibase.changelog.filter.LabelChangeSetFilter;
import liquibase.changelog.visitor.ExpectedChangesVisitor;
import liquibase.command.*;
import liquibase.configuration.ConfigurationValueObfuscator;
import liquibase.database.Database;
import liquibase.exception.CommandExecutionException;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.util.StreamUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.*;

public class UnexpectedChangesetsCommandStep extends AbstractCommandStep {
    public static final CommandArgumentDefinition<Boolean> VERBOSE_ARG;
    public static final String[] COMMAND_NAME = {"unexpectedChangesets"};

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        VERBOSE_ARG = builder.argument("verbose", Boolean.class)
                .defaultValue(false)
                .description("Verbose flag")
                .build();
    }

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
        commandDefinition.setShortDescription("Generate a list of changesets that have been executed but are not in the current changelog");
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        OutputStream outputStream = resultsBuilder.getOutputStream();
        OutputStreamWriter out = new OutputStreamWriter(outputStream);
        Database database = (Database) commandScope.getDependency(Database.class);
        DatabaseChangeLog changeLog = (DatabaseChangeLog) commandScope.getDependency(DatabaseChangeLog.class);
        ChangeLogParameters changeLogParameters = (ChangeLogParameters) commandScope.getDependency(ChangeLogParameters.class);
        Contexts contexts = changeLogParameters.getContexts();
        LabelExpression labelExpression = changeLogParameters.getLabels();
        boolean verbose = commandScope.getArgumentValue(VERBOSE_ARG);

        try {
            Collection<RanChangeSet> unexpectedChangeSets = listUnexpectedChangeSets(database, changeLog, contexts, labelExpression);
            if (unexpectedChangeSets.isEmpty()) {
                out.append(database.getConnection().getConnectionUserName());
                out.append("@");
                out.append(database.getConnection().getURL());
                out.append(" contains no unexpected changes!");
                out.append(StreamUtil.getLineSeparator());
            } else {
                out.append(String.valueOf(unexpectedChangeSets.size()));
                out.append(" unexpected changes were found in ");
                out.append(database.getConnection().getConnectionUserName());
                out.append("@");
                out.append(database.getConnection().getURL());
                out.append(StreamUtil.getLineSeparator());
                if (verbose) {
                    for (RanChangeSet ranChangeSet : unexpectedChangeSets) {
                        out.append("     ").append(ranChangeSet.toString()).append(StreamUtil.getLineSeparator());
                    }
                }
            }

            out.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }
    }

    public static Collection<RanChangeSet> listUnexpectedChangeSets(Database database, DatabaseChangeLog changeLog, Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        ExpectedChangesVisitor visitor = new ExpectedChangesVisitor(database.getRanChangeSetList());

        changeLog.validate(database, contexts, labelExpression);

        ChangeLogIterator logIterator = new ChangeLogIterator(changeLog,
                new ContextChangeSetFilter(contexts),
                new LabelChangeSetFilter(labelExpression),
                new DbmsChangeSetFilter(database),
                new IgnoreChangeSetFilter());
        logIterator.run(visitor, new RuntimeEnvironment(database, contexts, labelExpression));

        return visitor.getUnexpectedChangeSets();
    }
}
