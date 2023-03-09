package liquibase.command.core;

import liquibase.command.*;
import liquibase.command.core.helpers.DiffOutputControlCommandStep;
import liquibase.command.providers.ReferenceDatabase;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.diff.DiffResult;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.CommandValidationException;
import liquibase.util.StringUtil;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DiffChangelogCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"diffChangelog"};

    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;

    static {
        final CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        CHANGELOG_FILE_ARG = builder.argument(CommonArgumentNames.CHANGELOG_FILE, String.class)
                .description("Changelog file to write results").required().build();
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(DiffResult.class, DiffOutputControl.class);
    }

    @Override
    public List<Class<?>> providedDependencies() {
        return Collections.singletonList(DiffChangelogCommandStep.class);
    }


    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Compare two databases to produce changesets and write them to a changelog file");
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        Database referenceDatabase = (Database) commandScope.getDependency(ReferenceDatabase.class);
        DiffOutputControl diffOutputControl = (DiffOutputControl) resultsBuilder.getResult(DiffOutputControlCommandStep.DIFF_OUTPUT_CONTROL.getName());
        referenceDatabase.setOutputDefaultSchema(diffOutputControl.getIncludeSchema());

        InternalSnapshotCommandStep.logUnsupportedDatabase(referenceDatabase, this.getClass());
        DiffResult diffResult = (DiffResult) resultsBuilder.getResult(DiffCommandStep.DIFF_RESULT.getName());
        PrintStream outputStream = new PrintStream(resultsBuilder.getOutputStream());

        ObjectQuotingStrategy originalStrategy = referenceDatabase.getObjectQuotingStrategy();
        try {
            String changeLogFile = commandScope.getArgumentValue(CHANGELOG_FILE_ARG);
            referenceDatabase.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);
            if (StringUtil.trimToNull(changeLogFile) == null) {
                createDiffToChangeLogObject(diffResult, diffOutputControl).print(outputStream);
            } else {
                createDiffToChangeLogObject(diffResult, diffOutputControl).print(changeLogFile);
            }
        }
        finally {
            referenceDatabase.setObjectQuotingStrategy(originalStrategy);
            outputStream.flush();
        }
    }

    @Override
    public void validate(CommandScope commandScope) throws CommandValidationException {
        commandScope.addArgumentValue(DiffCommandStep.FORMAT_ARG, "none");
    }

    protected DiffToChangeLog createDiffToChangeLogObject(DiffResult diffResult, DiffOutputControl diffOutputControl) {
        return new DiffToChangeLog(diffResult, diffOutputControl);
    }

}
