package liquibase.command.core;

import liquibase.Scope;
import liquibase.command.*;
import liquibase.command.core.helpers.AbstractChangelogCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep;
import liquibase.command.core.helpers.DiffOutputControlCommandStep;
import liquibase.command.core.helpers.ReferenceDbUrlConnectionCommandStep;
import liquibase.command.providers.ReferenceDatabase;
import liquibase.configuration.ConfigurationValueObfuscator;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.diff.DiffResult;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.CommandValidationException;
import liquibase.resource.PathHandlerFactory;
import liquibase.resource.Resource;
import liquibase.util.StringUtil;
import liquibase.util.ValueHandlerUtil;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;

public class GenerateChangelogCommandStep extends AbstractChangelogCommandStep {

    public static final String[] COMMAND_NAME = {"generateChangelog"};

    private static final String INFO_MESSAGE =
            "BEST PRACTICE: When generating formatted SQL changelogs, always check if the 'splitStatements' attribute" + System.lineSeparator() +
            "works for your environment. See https://docs.liquibase.com/commands/inspection/generate-changelog.html for more information. ";

    public static final CommandArgumentDefinition<String> AUTHOR_ARG;
    public static final CommandArgumentDefinition<String> CONTEXT_ARG;
    public static final CommandArgumentDefinition<String> CONTEXTS_ARG;
    public static final CommandArgumentDefinition<String> LABEL_FILTER_ARG;
    public static final CommandArgumentDefinition<String> DATA_OUTPUT_DIR_ARG;
    public static final CommandArgumentDefinition<Boolean> OVERWRITE_OUTPUT_FILE_ARG;
    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_URL_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_USERNAME_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_PASSWORD_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_DEFAULT_SCHEMA_NAME_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_DEFAULT_CATALOG_NAME_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_DRIVER_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_DRIVER_PROPERTIES_FILE_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_SCHEMAS_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_LIQUIBASE_SCHEMA_NAME_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_LIQUIBASE_CATALOG_NAME_ARG;

    public static final CommandArgumentDefinition<Boolean> USE_OR_REPLACE_OPTION;

    static {
        final CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        CHANGELOG_FILE_ARG = builder.argument(CommonArgumentNames.CHANGELOG_FILE, String.class)
                .description("Changelog file to write results").build();

        // hiding parameter names that are not available externally but are used by this step.
        AUTHOR_ARG = builder.argument("author", String.class)
                .description("Specifies the author for changesets in the generated changelog").build();
        CONTEXT_ARG = builder.argument("context", String.class).hidden().build();
        LABEL_FILTER_ARG = builder.argument("labelFilter", String.class)
                .addAlias("labels")
                .description("Changeset labels to generate")
                .build();
        CONTEXTS_ARG = builder.argument("contextFilter", String.class)
                .addAlias("contexts")
                .description("Changeset contexts to generate")
                .build();
        DATA_OUTPUT_DIR_ARG = builder.argument("dataOutputDirectory", String.class)
                .description("Directory to write table data to").build();
        OVERWRITE_OUTPUT_FILE_ARG = builder.argument("overwriteOutputFile", Boolean.class)
                .defaultValue(false).description("Flag to allow overwriting of output changelog file. Default: false").build();

        // this happens because the command line asks for "url", but in fact uses it as "referenceUrl"
        REFERENCE_URL_ARG = builder.argument("referenceUrl", String.class).hidden().build();
        REFERENCE_DEFAULT_SCHEMA_NAME_ARG = builder.argument("referenceDefaultSchemaName", String.class)
                .hidden().build();
        REFERENCE_DEFAULT_CATALOG_NAME_ARG = builder.argument("referenceDefaultCatalogName", String.class)
                .hidden().build();
        REFERENCE_DRIVER_ARG = builder.argument("referenceDriver", String.class).hidden().build();
        REFERENCE_DRIVER_PROPERTIES_FILE_ARG = builder.argument("referenceDriverPropertiesFile", String.class)
                .hidden().build();
        REFERENCE_USERNAME_ARG = builder.argument("referenceUsername", String.class).hidden().build();
        REFERENCE_PASSWORD_ARG = builder.argument("referencePassword", String.class).hidden()
                .setValueObfuscator(ConfigurationValueObfuscator.STANDARD).build();
        REFERENCE_SCHEMAS_ARG = builder.argument("referenceSchemas", String.class).hidden().build();
        REFERENCE_LIQUIBASE_SCHEMA_NAME_ARG = builder.argument("referenceLiquibaseSchemaName", String.class)
                .hidden().build();
        REFERENCE_LIQUIBASE_CATALOG_NAME_ARG = builder.argument("referenceLiquibaseCatalogName", String.class)
                .hidden().build();
        USE_OR_REPLACE_OPTION = builder.argument("useOrReplaceOption", Boolean.class)
                .description("If true, will add 'OR REPLACE' option to the create view change object")
                .defaultValue(false)
                .setValueHandler(ValueHandlerUtil::booleanValueHandler)
                .build();
        builder.addArgument(AbstractChangelogCommandStep.RUN_ON_CHANGE_TYPES_ARG).build();
        builder.addArgument(AbstractChangelogCommandStep.REPLACE_IF_EXISTS_TYPES_ARG).build();
        builder.addArgument(AbstractChangelogCommandStep.SKIP_OBJECT_SORTING).build();
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(DiffOutputControl.class, DiffResult.class);
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        String changeLogFile = StringUtil.trimToNull(commandScope.getArgumentValue(CHANGELOG_FILE_ARG));
        if (changeLogFile != null && changeLogFile.toLowerCase().endsWith(".sql")) {
            Scope.getCurrentScope().getUI().sendMessage("\n" + INFO_MESSAGE + "\n");
            Scope.getCurrentScope().getLog(getClass()).info("\n" + INFO_MESSAGE + "\n");
        }

        final Database referenceDatabase = (Database) commandScope.getDependency(ReferenceDatabase.class);
        DiffOutputControl diffOutputControl = (DiffOutputControl) resultsBuilder.getResult(DiffOutputControlCommandStep.DIFF_OUTPUT_CONTROL.getName());
        diffOutputControl.setDataDir(commandScope.getArgumentValue(DATA_OUTPUT_DIR_ARG));
        referenceDatabase.setOutputDefaultSchema(diffOutputControl.getIncludeSchema());

        if(commandScope.getArgumentValue(GenerateChangelogCommandStep.USE_OR_REPLACE_OPTION).booleanValue()) {
            diffOutputControl.setReplaceIfExistsSet(true);
        }

        InternalSnapshotCommandStep.logUnsupportedDatabase(referenceDatabase, this.getClass());

        DiffResult diffResult = (DiffResult) resultsBuilder.getResult(DiffCommandStep.DIFF_RESULT.getName());

        DiffToChangeLog changeLogWriter = new DiffToChangeLog(diffResult, diffOutputControl, commandScope.getArgumentValue(SKIP_OBJECT_SORTING));

        changeLogWriter.setChangeSetAuthor(commandScope.getArgumentValue(AUTHOR_ARG));
        if (commandScope.getArgumentValue(CONTEXT_ARG) != null) {
            changeLogWriter.setChangeSetContext(commandScope.getArgumentValue(CONTEXT_ARG));
        } else {
            changeLogWriter.setChangeSetContext(commandScope.getArgumentValue(CONTEXTS_ARG));
        }
        if (commandScope.getArgumentValue(LABEL_FILTER_ARG) != null) {
            changeLogWriter.setChangeSetLabels(commandScope.getArgumentValue(LABEL_FILTER_ARG));
        }
        changeLogWriter.setChangeSetPath(changeLogFile);
        changeLogWriter.setChangeSetRunOnChangeTypes(commandScope.getArgumentValue(RUN_ON_CHANGE_TYPES_ARG).split("\\s*,\\s*"));
        changeLogWriter.setChangeReplaceIfExistsTypes(commandScope.getArgumentValue(REPLACE_IF_EXISTS_TYPES_ARG).split("\\s*,\\s*"));

        ObjectQuotingStrategy originalStrategy = referenceDatabase.getObjectQuotingStrategy();
        try {
            referenceDatabase.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);
            if (StringUtil.trimToNull(changeLogFile) != null) {
                Boolean overwriteOutputFile = commandScope.getArgumentValue(OVERWRITE_OUTPUT_FILE_ARG);
                changeLogWriter.print(changeLogFile, overwriteOutputFile);
            } else {
                try(PrintStream outputStream = new PrintStream(resultsBuilder.getOutputStream())) {
                    changeLogWriter.print(outputStream);
                }
            }
            if (StringUtil.trimToNull(changeLogFile) != null) {
                Scope.getCurrentScope().getUI().sendMessage("Generated changelog written to " + changeLogFile);
            }
        } finally {
            referenceDatabase.setObjectQuotingStrategy(originalStrategy);
        }
    }

    @Override
    public void validate(CommandScope commandScope) throws CommandValidationException {
        // sets the values to the reference database, as this is what we expect.
        commandScope.addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DATABASE_ARG, commandScope.getArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG));
        commandScope.addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_URL_ARG, commandScope.getArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG));
        commandScope.addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_USERNAME_ARG, commandScope.getArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG));
        commandScope.addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_PASSWORD_ARG, commandScope.getArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG));
        commandScope.addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DRIVER_ARG, commandScope.getArgumentValue(DbUrlConnectionArgumentsCommandStep.DRIVER_ARG));
        commandScope.addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DEFAULT_SCHEMA_NAME_ARG, commandScope.getArgumentValue(DbUrlConnectionArgumentsCommandStep.DEFAULT_SCHEMA_NAME_ARG));
        commandScope.addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DEFAULT_CATALOG_NAME_ARG, commandScope.getArgumentValue(DbUrlConnectionArgumentsCommandStep.DEFAULT_CATALOG_NAME_ARG));
        commandScope.addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DRIVER_PROPERTIES_FILE_ARG, commandScope.getArgumentValue(DbUrlConnectionArgumentsCommandStep.DRIVER_PROPERTIES_FILE_ARG));
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.SKIP_DATABASE_STEP_ARG, true);
        commandScope.addArgumentValue(DiffCommandStep.FORMAT_ARG, "none");
        validateConditionsToOverwriteChangelogFile(commandScope);
        validateReplaceIfExistsTypes(commandScope);
        validateRunOnChangeTypes(commandScope);
    }

    /**
     * If the changelog file already exists, validate if overwriteOutputFile is true. Otherwise, throws an exception
     */
    private static void validateConditionsToOverwriteChangelogFile(CommandScope commandScope) throws CommandValidationException {
        String changeLogFile = StringUtil.trimToNull(commandScope.getArgumentValue(CHANGELOG_FILE_ARG));
        if (changeLogFile != null) {
            final PathHandlerFactory pathHandlerFactory = Scope.getCurrentScope().getSingleton(PathHandlerFactory.class);
            Resource file;
            try {
                file = pathHandlerFactory.getResource(changeLogFile);
            } catch (IOException e) {
                throw new CommandValidationException(e);
            }
            Boolean overwriteOutputFile = commandScope.getArgumentValue(OVERWRITE_OUTPUT_FILE_ARG);
            if (file.exists() && (!overwriteOutputFile)) {
                throw new CommandValidationException(String.format(coreBundle.getString("changelogfile.already.exists"), changeLogFile));
            }
        }
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Generate a changelog");
        commandDefinition.setLongDescription("Writes Change Log XML to copy the current state of the database to standard out or a file");
    }
}
