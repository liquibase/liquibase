package liquibase.command.core;

import liquibase.Scope;
import liquibase.command.*;
import liquibase.command.providers.ReferenceDatabase;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.ObjectChangeFilter;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.util.StringUtil;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DiffChangelogCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"diffChangelog"};

    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<Boolean> INCLUDE_CATALOG_ARG;
    public static final CommandArgumentDefinition<Boolean> INCLUDE_SCHEMA_ARG;
    public static final CommandArgumentDefinition<Boolean> INCLUDE_TABLESPACE_ARG;

    public static final CommandResultDefinition<DiffOutputControl> DIFF_OUTPUT_CONTROL;


    static {
        final CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        CHANGELOG_FILE_ARG = builder.argument(CommonArgumentNames.CHANGELOG_FILE, String.class)
                .description("Changelog file to write results").required().build();
        INCLUDE_CATALOG_ARG = builder.argument("includeCatalog", Boolean.class).defaultValue(false)
                .description("If true, the catalog will be included in generated changeSets. Defaults to false.").build();
        INCLUDE_SCHEMA_ARG = builder.argument("includeSchema", Boolean.class).defaultValue(false)
                .description("If true, the schema will be included in generated changeSets. Defaults to false.").build();
        INCLUDE_TABLESPACE_ARG = builder.argument("includeTablespace", Boolean.class).defaultValue(false)
                .description("Include the tablespace attribute in the changelog. Defaults to false.").build();

        DIFF_OUTPUT_CONTROL = builder.result("diffOutputControl", DiffOutputControl.class).build();
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Collections.singletonList(DiffCommandStep.class);
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

        DiffOutputControl diffOutputControl = getDiffOutputControl(commandScope);
        resultsBuilder.addResult(DIFF_OUTPUT_CONTROL.getName(), diffOutputControl);


        referenceDatabase.setOutputDefaultSchema(diffOutputControl.getIncludeSchema());

        String changeLogFile = commandScope.getArgumentValue(CHANGELOG_FILE_ARG);

        InternalSnapshotCommandStep.logUnsupportedDatabase(referenceDatabase, this.getClass());

        DiffResult diffResult = (DiffResult) resultsBuilder.getResult(DiffCommandStep.DIFF_RESULT.getName());

        PrintStream outputStream = new PrintStream(resultsBuilder.getOutputStream());

        outputBestPracticeMessage();

        ObjectQuotingStrategy originalStrategy = referenceDatabase.getObjectQuotingStrategy();
        try {
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



    /**
     * Creates a new DiffOutputControl object based on the parameters received by the command
     */
    private DiffOutputControl getDiffOutputControl(CommandScope commandScope) {
        CompareControl compareControl =  commandScope.getArgumentValue(DiffCommandStep.COMPARE_CONTROL_ARG);
        ObjectChangeFilter objectChangeFilter = commandScope.getArgumentValue(DiffCommandStep.OBJECT_CHANGE_FILTER_ARG);

        DiffOutputControl diffOutputControl = new DiffOutputControl(
                commandScope.getArgumentValue(INCLUDE_CATALOG_ARG), commandScope.getArgumentValue(INCLUDE_SCHEMA_ARG),
                commandScope.getArgumentValue(INCLUDE_TABLESPACE_ARG), compareControl.getSchemaComparisons());
        for (CompareControl.SchemaComparison schema : compareControl.getSchemaComparisons()) {
            diffOutputControl.addIncludedSchema(schema.getReferenceSchema());
            diffOutputControl.addIncludedSchema(schema.getComparisonSchema());
        }

        if (objectChangeFilter != null) {
            diffOutputControl.setObjectChangeFilter(objectChangeFilter);
        }

        return diffOutputControl;
    }

    protected DiffToChangeLog createDiffToChangeLogObject(DiffResult diffResult, DiffOutputControl diffOutputControl) {
        return new DiffToChangeLog(diffResult, diffOutputControl);
    }


    protected void outputBestPracticeMessage() {
        Scope.getCurrentScope().getUI().sendMessage(
           "BEST PRACTICE: The changelog generated by diffChangeLog/generateChangeLog should be " +
                   "inspected for correctness and completeness before being deployed. " +
                   "Some database objects and their dependencies cannot be represented automatically, " +
                   "and they may need to be manually updated before being deployed.");
    }

}
