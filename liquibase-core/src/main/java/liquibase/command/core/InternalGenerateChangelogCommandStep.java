package liquibase.command.core;

import liquibase.Scope;
import liquibase.command.*;
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

import java.io.File;
import java.io.PrintStream;

public class InternalGenerateChangelogCommandStep extends InternalDiffChangelogCommandStep {

    public static final String[] COMMAND_NAME = {"internalGenerateChangelog"};

    private static final String INFO_MESSAGE =
            "When generating formatted SQL changelogs, it is important to decide if batched statements\n" +
            "should be split or not.  For storedlogic objects, the default behavior is 'splitStatements:false'\n." +
            "All other objects default to 'splitStatements:true'.  See https://docs.liquibase.org for additional information.";


    public static final CommandArgumentDefinition<String> AUTHOR_ARG;
    public static final CommandArgumentDefinition<String> CONTEXT_ARG;

    static {
        final CommandBuilder builder = new CommandBuilder(COMMAND_NAME);

        AUTHOR_ARG = builder.argument("author", String.class).build();
        CONTEXT_ARG = builder.argument("context", String.class).build();

    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        super.adjustCommandDefinition(commandDefinition);
        commandDefinition.setInternal(true);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        outputBestPracticeMessage();

        String changeLogFile = StringUtil.trimToNull(commandScope.getArgumentValue(CHANGELOG_FILE_ARG));
        if (changeLogFile != null && changeLogFile.toLowerCase().endsWith(".sql")) {
            Scope.getCurrentScope().getUI().sendMessage("\n" + INFO_MESSAGE + "\n");
            Scope.getCurrentScope().getLog(getClass()).info("\n" + INFO_MESSAGE + "\n");
        }

        final Database referenceDatabase = commandScope.getArgumentValue(REFERENCE_DATABASE_ARG);

        InternalSnapshotCommandStep.logUnsupportedDatabase(referenceDatabase, this.getClass());

        DiffResult diffResult = createDiffResult(commandScope);

        DiffToChangeLog changeLogWriter = new DiffToChangeLog(diffResult, commandScope.getArgumentValue(DIFF_OUTPUT_CONTROL_ARG));

        changeLogWriter.setChangeSetAuthor(commandScope.getArgumentValue(AUTHOR_ARG));
        changeLogWriter.setChangeSetContext(commandScope.getArgumentValue(CONTEXT_ARG));
        changeLogWriter.setChangeSetPath(changeLogFile);

        ObjectQuotingStrategy originalStrategy = referenceDatabase.getObjectQuotingStrategy();
        try {
            referenceDatabase.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);
            if (StringUtil.trimToNull(changeLogFile) != null) {
                changeLogWriter.print(changeLogFile);
            } else {
                PrintStream outputStream = new PrintStream(resultsBuilder.getOutputStream());

                try {
                    changeLogWriter.print(outputStream);
                } finally {
                    outputStream.flush();
                }

            }
            if (StringUtil.trimToNull(changeLogFile) != null) {
                Scope.getCurrentScope().getUI().sendMessage("Generated changelog written to " + new File(changeLogFile).getAbsolutePath());
            }
        } finally {
            referenceDatabase.setObjectQuotingStrategy(originalStrategy);
        }
    }

    @Override
    protected DatabaseSnapshot createTargetSnapshot(CommandScope commandScope) throws DatabaseException, InvalidExampleException {
        SnapshotControl snapshotControl = new SnapshotControl(commandScope.getArgumentValue(REFERENCE_DATABASE_ARG), commandScope.getArgumentValue(SNAPSHOT_TYPES_ARG));
        return SnapshotGeneratorFactory.getInstance().createSnapshot(commandScope.getArgumentValue(COMPARE_CONTROL_ARG).getSchemas(CompareControl.DatabaseRole.REFERENCE), null, snapshotControl);
    }
}
