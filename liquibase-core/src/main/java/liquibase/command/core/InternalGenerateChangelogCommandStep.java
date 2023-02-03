package liquibase.command.core;

import liquibase.Scope;
import liquibase.command.*;
import liquibase.command.providers.ReferenceDatabase;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.DatabaseException;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.util.StringUtil;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class InternalGenerateChangelogCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"internalGenerateChangelog"};

    private static final String INFO_MESSAGE =
            "BEST PRACTICE: When generating formatted SQL changelogs, always check if the 'splitStatements' attribute" + System.lineSeparator() +
            "works for your environment. See https://docs.liquibase.com/commands/generatechangelog.html for more information. ";

    public static final CommandArgumentDefinition<String> AUTHOR_ARG;
    public static final CommandArgumentDefinition<String> CONTEXT_ARG;
    public static final CommandArgumentDefinition<Boolean> OVERWRITE_OUTPUT_FILE_ARG;

    static {
        final CommandBuilder builder = new CommandBuilder(COMMAND_NAME);

        AUTHOR_ARG = builder.argument("author", String.class).build();
        CONTEXT_ARG = builder.argument("context", String.class).build();
        OVERWRITE_OUTPUT_FILE_ARG = builder.argument("overwriteOutputFile", Boolean.class)
                .description("Flag to allow overwriting of output changelog file").build();

    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Collections.singletonList(DiffChangelogCommandStep.class);
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

        //FIXME should call this outputBestPracticeMessage();

        String changeLogFile = StringUtil.trimToNull(commandScope.getArgumentValue(DiffChangelogCommandStep.CHANGELOG_FILE_ARG));
        if (changeLogFile != null && changeLogFile.toLowerCase().endsWith(".sql")) {
            Scope.getCurrentScope().getUI().sendMessage("\n" + INFO_MESSAGE + "\n");
            Scope.getCurrentScope().getLog(getClass()).info("\n" + INFO_MESSAGE + "\n");
        }

        final Database referenceDatabase = (Database) commandScope.getDependency(ReferenceDatabase.class);
        DiffOutputControl diffOutputControl = (DiffOutputControl) resultsBuilder.getResult(DiffChangelogCommandStep.DIFF_OUTPUT_CONTROL.getName());
        referenceDatabase.setOutputDefaultSchema(diffOutputControl.getIncludeSchema());

        InternalSnapshotCommandStep.logUnsupportedDatabase(referenceDatabase, this.getClass());

        DiffResult diffResult = (DiffResult) resultsBuilder.getResult(DiffCommandStep.DIFF_RESULT.getName());

        DiffToChangeLog changeLogWriter = new DiffToChangeLog(diffResult, diffOutputControl);

        changeLogWriter.setChangeSetAuthor(commandScope.getArgumentValue(AUTHOR_ARG));
        changeLogWriter.setChangeSetContext(commandScope.getArgumentValue(CONTEXT_ARG));
        changeLogWriter.setChangeSetPath(changeLogFile);

        ObjectQuotingStrategy originalStrategy = referenceDatabase.getObjectQuotingStrategy();
        try {
            referenceDatabase.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);
            if (StringUtil.trimToNull(changeLogFile) != null) {
                Boolean overwriteOutputFile = commandScope.getArgumentValue(OVERWRITE_OUTPUT_FILE_ARG);
                changeLogWriter.print(changeLogFile, overwriteOutputFile);
            } else {
                PrintStream outputStream = new PrintStream(resultsBuilder.getOutputStream());

                try {
                    changeLogWriter.print(outputStream);
                } finally {
                    outputStream.flush();
                }

            }
            if (StringUtil.trimToNull(changeLogFile) != null) {
                Scope.getCurrentScope().getUI().sendMessage("Generated changelog written to " + changeLogFile);
            }
        } finally {
            referenceDatabase.setObjectQuotingStrategy(originalStrategy);
        }
    }

    protected DatabaseSnapshot createTargetSnapshot(CommandScope commandScope) throws DatabaseException, InvalidExampleException {
        Database database = (Database) commandScope.getDependency(Database.class);
        SnapshotControl snapshotControl = new SnapshotControl(database, commandScope.getArgumentValue(DiffCommandStep.SNAPSHOT_TYPES_ARG));
        return SnapshotGeneratorFactory.getInstance().createSnapshot(commandScope.getArgumentValue(DiffCommandStep.COMPARE_CONTROL_ARG).getSchemas(CompareControl.DatabaseRole.REFERENCE), null, snapshotControl);
    }
}
