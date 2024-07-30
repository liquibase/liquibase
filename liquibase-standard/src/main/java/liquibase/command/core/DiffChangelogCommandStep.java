package liquibase.command.core;

import liquibase.Scope;
import liquibase.command.*;
import liquibase.command.core.helpers.AbstractChangelogCommandStep;
import liquibase.command.core.helpers.DiffOutputControlCommandStep;
import liquibase.command.providers.ReferenceDatabase;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.diff.DiffResult;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.CommandValidationException;
import liquibase.logging.mdc.MdcKey;
import liquibase.logging.mdc.MdcObject;
import liquibase.logging.mdc.MdcValue;
import liquibase.util.StringUtil;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DiffChangelogCommandStep extends AbstractChangelogCommandStep {

    public static final String[] COMMAND_NAME = {"diffChangelog"};

    public static final CommandArgumentDefinition<String> AUTHOR_ARG;
    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> CONTEXTS_ARG;
    public static final CommandArgumentDefinition<String> LABEL_FILTER_ARG;
    public static final CommandArgumentDefinition<Boolean> USE_OR_REPLACE_OPTION;

    static {
        final CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        CHANGELOG_FILE_ARG = builder.argument(CommonArgumentNames.CHANGELOG_FILE, String.class)
                .description("Changelog file to write results").required().build();
        AUTHOR_ARG = builder.argument("author", String.class)
                .description("Specifies the author for changesets in the generated changelog").build();
        LABEL_FILTER_ARG = builder.argument("labelFilter", String.class)
                .addAlias("labels")
                .description("Changeset labels to generate")
                .build();
        CONTEXTS_ARG = builder.argument("contextFilter", String.class)
                .addAlias("contexts")
                .description("Changeset contexts to generate")
                .build();
        USE_OR_REPLACE_OPTION = builder.argument("useOrReplaceOption", Boolean.class)
                .description("If true, will add 'OR REPLACE' option to the create view change object")
                .defaultValue(false)
                .build();
        builder.addArgument(AbstractChangelogCommandStep.RUN_ON_CHANGE_TYPES_ARG).build();
        builder.addArgument(AbstractChangelogCommandStep.REPLACE_IF_EXISTS_TYPES_ARG).build();
        builder.addArgument(AbstractChangelogCommandStep.SKIP_OBJECT_SORTING).build();
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
        try {
            CommandScope commandScope = resultsBuilder.getCommandScope();
            Database referenceDatabase = (Database) commandScope.getDependency(ReferenceDatabase.class);
            DiffOutputControl diffOutputControl = (DiffOutputControl) resultsBuilder.getResult(DiffOutputControlCommandStep.DIFF_OUTPUT_CONTROL.getName());
            if (commandScope.getArgumentValue(DiffChangelogCommandStep.USE_OR_REPLACE_OPTION)) {
                diffOutputControl.setReplaceIfExistsSet(true);
            }
            referenceDatabase.setOutputDefaultSchema(diffOutputControl.getIncludeSchema());

            InternalSnapshotCommandStep.logUnsupportedDatabase(referenceDatabase, this.getClass());
            DiffResult diffResult = (DiffResult) resultsBuilder.getResult(DiffCommandStep.DIFF_RESULT.getName());
            PrintStream outputStream = new PrintStream(resultsBuilder.getOutputStream());

            ObjectQuotingStrategy originalStrategy = referenceDatabase.getObjectQuotingStrategy();
            try {
                String changeLogFile = commandScope.getArgumentValue(CHANGELOG_FILE_ARG);
                Scope.getCurrentScope().addMdcValue(MdcKey.DIFF_CHANGELOG_FILE, changeLogFile);
                referenceDatabase.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);

                DiffToChangeLog changeLogWriter = createDiffToChangeLogObject(diffResult, diffOutputControl, commandScope.getArgumentValue(AbstractChangelogCommandStep.SKIP_OBJECT_SORTING));
                changeLogWriter.setChangeSetContext(commandScope.getArgumentValue(CONTEXTS_ARG));
                changeLogWriter.setChangeSetLabels(commandScope.getArgumentValue(LABEL_FILTER_ARG));
                changeLogWriter.setChangeSetAuthor(commandScope.getArgumentValue(AUTHOR_ARG));
                changeLogWriter.setChangeSetRunOnChangeTypes(commandScope.getArgumentValue(RUN_ON_CHANGE_TYPES_ARG).split("\\s*,\\s*"));
                changeLogWriter.setChangeReplaceIfExistsTypes(commandScope.getArgumentValue(REPLACE_IF_EXISTS_TYPES_ARG).split("\\s*,\\s*"));
                if (StringUtil.trimToNull(changeLogFile) == null) {
                    changeLogWriter.print(outputStream);
                } else {
                    changeLogWriter.print(changeLogFile);
                }
            }
            finally {
                referenceDatabase.setObjectQuotingStrategy(originalStrategy);
                outputStream.flush();
            }
            try (MdcObject diffChangelogOutcome = Scope.getCurrentScope().addMdcValue(MdcKey.DIFF_CHANGELOG_OUTCOME, MdcValue.COMMAND_SUCCESSFUL)) {
                Scope.getCurrentScope().getLog(getClass()).info("Diff changelog command succeeded");
            }
        } catch (Exception e) {
            try (MdcObject diffChangelogOutcome = Scope.getCurrentScope().addMdcValue(MdcKey.DIFF_CHANGELOG_OUTCOME, MdcValue.COMMAND_FAILED)) {
                Scope.getCurrentScope().getLog(getClass()).warning("Diff changelog command failed: " + e.getMessage());
            }
            throw e;
        }

    }

    @Override
    public void validate(CommandScope commandScope) throws CommandValidationException {
        commandScope.addArgumentValue(DiffCommandStep.FORMAT_ARG, "none");
        validateReplaceIfExistsTypes(commandScope);
        validateRunOnChangeTypes(commandScope);
    }

    protected DiffToChangeLog createDiffToChangeLogObject(DiffResult diffResult, DiffOutputControl diffOutputControl, boolean skipObjectSorting) {
        return new DiffToChangeLog(diffResult, diffOutputControl, skipObjectSorting);
    }

}
