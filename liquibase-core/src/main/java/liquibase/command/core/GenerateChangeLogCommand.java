package liquibase.command.core;

import liquibase.Scope;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandScope;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.util.StringUtil;

import java.io.PrintStream;

public class GenerateChangeLogCommand extends DiffToChangeLogCommand {
    private static final String INFO_MESSAGE =
            "When generating formatted SQL changelogs, it is important to decide if batched statements\n" +
                    "should be split (splitStatements:true is the default behavior) or not (splitStatements:false).\n" +
                    "See http://liquibase.org for additional documentation.";

    public static final CommandArgumentDefinition<String> AUTHOR_ARG;
    public static final CommandArgumentDefinition<String> CONTEXT_ARG;

    static {
        final CommandArgumentDefinition.Builder builder = new CommandArgumentDefinition.Builder(GenerateChangeLogCommand.class);

        AUTHOR_ARG = builder.define("author", String.class).build();
        CONTEXT_ARG = builder.define("context", String.class).build();

    }

    @Override
    public String[] getName() {
        return new String[]{"generateChangeLog"};
    }

    @Override
    public void run(CommandScope commandScope) throws Exception {
        outputBestPracticeMessage();

        String changeLogFile = StringUtil.trimToNull(CHANGELOG_FILENAME_ARG.getValue(commandScope));
        if (changeLogFile.toLowerCase().endsWith(".sql")) {
            Scope.getCurrentScope().getUI().sendMessage("\n" + INFO_MESSAGE + "\n");
            Scope.getCurrentScope().getLog(getClass()).info("\n" + INFO_MESSAGE + "\n");
        }

        final Database referenceDatabase = REFERENCE_DATABASE_ARG.getValue(commandScope);

        SnapshotCommand.logUnsupportedDatabase(referenceDatabase, this.getClass());

        DiffResult diffResult = createDiffResult(commandScope);

        DiffToChangeLog changeLogWriter = new DiffToChangeLog(diffResult, DIFF_OUTPUT_CONTROL_ARG.getValue(commandScope));

        changeLogWriter.setChangeSetAuthor(AUTHOR_ARG.getValue(commandScope));
        changeLogWriter.setChangeSetContext(CONTEXT_ARG.getValue(commandScope));
        changeLogWriter.setChangeSetPath(changeLogFile);

        ObjectQuotingStrategy originalStrategy = referenceDatabase.getObjectQuotingStrategy();
        try {
            referenceDatabase.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);
            if (StringUtil.trimToNull(changeLogFile) != null) {
                changeLogWriter.print(changeLogFile);
            } else {
                PrintStream outputStream = OUTPUT_STREAM_ARG.getValue(commandScope);
                if (outputStream == null) {
                    outputStream = System.out;
                }
                changeLogWriter.print(outputStream);
            }
        } finally {
            referenceDatabase.setObjectQuotingStrategy(originalStrategy);
        }
    }

    @Override
    protected DatabaseSnapshot createTargetSnapshot(CommandScope commandScope) throws DatabaseException, InvalidExampleException {
        SnapshotControl snapshotControl = new SnapshotControl(REFERENCE_DATABASE_ARG.getValue(commandScope), SNAPSHOT_TYPES_ARG.getValue(commandScope));
        return SnapshotGeneratorFactory.getInstance().createSnapshot(COMPARE_CONTROL_ARG.getValue(commandScope).getSchemas(CompareControl.DatabaseRole.REFERENCE), null, snapshotControl);
    }
}
