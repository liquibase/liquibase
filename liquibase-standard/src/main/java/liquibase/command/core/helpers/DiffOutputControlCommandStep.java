package liquibase.command.core.helpers;

import liquibase.Scope;
import liquibase.command.*;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.ObjectChangeFilter;
import liquibase.diff.output.changelog.ChangeGeneratorFactory;
import liquibase.diff.output.changelog.core.MissingDataExternalFileChangeGenerator;
import liquibase.logging.mdc.MdcKey;

import java.util.Collections;
import java.util.List;

/**
 * Internal command step to be used on pipeline to instantiate a DiffOutputControl object that is mainly used
 * by diffChangeLog/generateChangeLog .
 */
public class DiffOutputControlCommandStep extends AbstractHelperCommandStep implements CleanUpCommandStep{

    public static final String[] COMMAND_NAME = {"diffOutputControl"};
    public static final CommandArgumentDefinition<Boolean> INCLUDE_CATALOG_ARG;
    public static final CommandArgumentDefinition<Boolean> INCLUDE_SCHEMA_ARG;
    public static final CommandArgumentDefinition<Boolean> INCLUDE_TABLESPACE_ARG;

    public static final CommandArgumentDefinition<String> DATA_OUTPUT_DIR_ARG;
    public static final CommandArgumentDefinition<String> EXCLUDE_OBJECTS;
    public static final CommandArgumentDefinition<String> INCLUDE_OBJECTS;

    public static final CommandResultDefinition<DiffOutputControl> DIFF_OUTPUT_CONTROL;


    static {
        final CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        INCLUDE_CATALOG_ARG = builder.argument("includeCatalog", Boolean.class).defaultValue(false)
                .description("If true, the catalog will be included in generated changeSets. Defaults to false.").build();
        INCLUDE_SCHEMA_ARG = builder.argument("includeSchema", Boolean.class).defaultValue(false)
                .description("If true, the schema will be included in generated changeSets. Defaults to false.").build();
        INCLUDE_TABLESPACE_ARG = builder.argument("includeTablespace", Boolean.class).defaultValue(false)
                .description("Include the tablespace attribute in the changelog. Defaults to false.").build();
        DATA_OUTPUT_DIR_ARG = builder.argument("dataOutputDirectory", String.class)
                .description("Specifies a directory to send the loadData output of a diff-changelog/generate-changelog command as a CSV file.").build();

        EXCLUDE_OBJECTS = builder.argument("excludeObjects", String.class).defaultValue(null)
                .description("Objects to exclude from diff. Supports regular expressions. Defaults to null.").build();
        INCLUDE_OBJECTS = builder.argument("includeObjects", String.class).defaultValue(null)
                .description("Objects to include in diff. Supports regular expressions. Defaults to null.").build();

        DIFF_OUTPUT_CONTROL = builder.result("diffOutputControl", DiffOutputControl.class).build();
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Collections.singletonList(CompareControl.class);
    }

    @Override
    public List<Class<?>> providedDependencies() {
        return Collections.singletonList(DiffOutputControl.class);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        DiffOutputControl diffOutputControl = getDiffOutputControl(resultsBuilder);
        resultsBuilder.addResult(DIFF_OUTPUT_CONTROL.getName(), diffOutputControl);
        this.outputBestPracticeMessage();
    }

    /**
     * Creates a new DiffOutputControl object based on the parameters received by the command
     */
    private DiffOutputControl getDiffOutputControl(CommandResultsBuilder resultsBuilder) {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        CompareControl compareControl = (CompareControl) resultsBuilder.getResult(PreCompareCommandStep.COMPARE_CONTROL_RESULT.getName());
        ObjectChangeFilter objectChangeFilter = (ObjectChangeFilter) resultsBuilder.getResult(PreCompareCommandStep.OBJECT_CHANGE_FILTER_RESULT.getName());

        Boolean includeCatalog = commandScope.getArgumentValue(INCLUDE_CATALOG_ARG);
        Boolean includeSchema = commandScope.getArgumentValue(INCLUDE_SCHEMA_ARG);
        Boolean includeTablespace = commandScope.getArgumentValue(INCLUDE_TABLESPACE_ARG);
        String dataDir = commandScope.getArgumentValue(DATA_OUTPUT_DIR_ARG);
        addMdcProperties(includeCatalog, includeSchema, includeTablespace);
        DiffOutputControl diffOutputControl = new DiffOutputControl(
                includeCatalog, includeSchema,
                includeTablespace, compareControl.getSchemaComparisons());
        diffOutputControl.setExcludeObjects(commandScope.getArgumentValue(EXCLUDE_OBJECTS));
        diffOutputControl.setIncludeObjects(commandScope.getArgumentValue(INCLUDE_OBJECTS));
        for (CompareControl.SchemaComparison schema : compareControl.getSchemaComparisons()) {
            diffOutputControl.addIncludedSchema(schema.getReferenceSchema());
            diffOutputControl.addIncludedSchema(schema.getComparisonSchema());
        }

        if (objectChangeFilter != null) {
            diffOutputControl.setObjectChangeFilter(objectChangeFilter);
        }
        diffOutputControl.setDataDir(dataDir);

        return diffOutputControl;
    }

    private void addMdcProperties(Boolean includeCatalog, Boolean includeSchema, Boolean includeTablespace) {
        Scope.getCurrentScope().addMdcValue(MdcKey.INCLUDE_CATALOG, String.valueOf(includeCatalog));
        Scope.getCurrentScope().addMdcValue(MdcKey.INCLUDE_SCHEMA, String.valueOf(includeSchema));
        Scope.getCurrentScope().addMdcValue(MdcKey.INCLUDE_TABLESPACE, String.valueOf(includeTablespace));
    }

    protected void outputBestPracticeMessage() {
        Scope.getCurrentScope().getUI().sendMessage(
           "BEST PRACTICE: The changelog generated by diffChangeLog/generateChangeLog should be " +
                   "inspected for correctness and completeness before being deployed. " +
                   "Some database objects and their dependencies cannot be represented automatically, " +
                   "and they may need to be manually updated before being deployed.");
    }

    @Override
    public void cleanUp(CommandResultsBuilder resultsBuilder) {
        ChangeGeneratorFactory.getInstance().unregisterAll(MissingDataExternalFileChangeGenerator.class);
    }
}
