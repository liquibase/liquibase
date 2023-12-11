package liquibase.command.core;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.RuntimeEnvironment;
import liquibase.Scope;
import liquibase.changelog.ChangeLogIterator;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.filter.*;
import liquibase.changelog.visitor.ListVisitor;
import liquibase.command.*;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.logging.mdc.MdcKey;
import liquibase.logging.mdc.MdcObject;
import liquibase.logging.mdc.customobjects.SimpleStatus;
import liquibase.logging.mdc.customobjects.Status;
import liquibase.util.StreamUtil;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class StatusCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"status"};

    public static final CommandArgumentDefinition<Boolean> VERBOSE_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        VERBOSE_ARG = builder.argument("verbose", Boolean.class)
                .description("Verbose flag with optional values of 'True' or 'False'. The default is 'True'.")
                .defaultValue(true)
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
        commandDefinition.setShortDescription("Generate a list of pending changesets");
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
        LabelExpression labels = changeLogParameters.getLabels();
        boolean verbose = commandScope.getArgumentValue(VERBOSE_ARG);

        List<ChangeSet> unrunChangeSets = listUnrunChangeSets(contexts, labels, changeLog, database);
        String message;
        if (unrunChangeSets.isEmpty()) {
            message = "up-to-date";
            out.append(database.getConnection().getConnectionUserName());
            out.append("@");
            out.append(database.getConnection().getURL());
            out.append(" is up to date");
            out.append(StreamUtil.getLineSeparator());
        } else {
            message = "undeployed";
            int size = unrunChangeSets.size();
            out.append(String.valueOf(size));
            if (size == 1) {
                out.append(" changeset has not been applied to ");
            } else {
                out.append(" changesets have not been applied to ");
            }
            out.append(database.getConnection().getConnectionUserName());
            out.append("@");
            out.append(database.getConnection().getURL());
            out.append(StreamUtil.getLineSeparator());
            if (verbose) {
                for (ChangeSet changeSet : unrunChangeSets) {
                    out.append("     ").append(changeSet.toString(false))
                            .append(StreamUtil.getLineSeparator());
                }
            }
        }


        SimpleStatus statusMdc;
        if (verbose) {
            statusMdc = new Status(message, database.getConnection().getURL(), unrunChangeSets);
        } else {
            statusMdc = new SimpleStatus(message, database.getConnection().getURL(), unrunChangeSets);
        }

        try (MdcObject statusMdcObject = Scope.getCurrentScope().addMdcValue(MdcKey.STATUS, statusMdc)) {
            Scope.getCurrentScope().getLog(getClass()).fine("Status");
        }

        out.flush();
    }

    public List<ChangeSet> listUnrunChangeSets(Contexts contexts, LabelExpression labels, DatabaseChangeLog changeLog, Database database) throws Exception {
        ListVisitor visitor = new ListVisitor();

        Scope.child(Collections.singletonMap(Scope.Attr.database.name(), database), () -> {
            ChangeLogIterator logIterator = getStandardChangelogIterator(contexts, labels, changeLog, database);

            logIterator.run(visitor, new RuntimeEnvironment(database, contexts, labels));
        });
        return visitor.getSeenChangeSets();
    }

    protected ChangeLogIterator getStandardChangelogIterator(Contexts contexts, LabelExpression labelExpression,
                                                             DatabaseChangeLog changeLog, Database database) throws DatabaseException {
        return new ChangeLogIterator(changeLog,
                new ShouldRunChangeSetFilter(database),
                new ContextChangeSetFilter(contexts),
                new LabelChangeSetFilter(labelExpression),
                new DbmsChangeSetFilter(database),
                new IgnoreChangeSetFilter());
    }
}
