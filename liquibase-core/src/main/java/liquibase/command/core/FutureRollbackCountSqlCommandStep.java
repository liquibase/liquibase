package liquibase.command.core;

import liquibase.command.*;
import liquibase.configuration.ConfigurationValueObfuscator;
import liquibase.exception.CommandExecutionException;

public class FutureRollbackCountSqlCommandStep extends AbstractCliWrapperCommandStep {

    public static final String[] COMMAND_NAME = {"futureRollbackCountSql"};

    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> DEFAULT_SCHEMA_NAME_ARG;
    public static final CommandArgumentDefinition<String> DEFAULT_CATALOG_NAME_ARG;
    public static final CommandArgumentDefinition<String> USERNAME_ARG;
    public static final CommandArgumentDefinition<String> PASSWORD_ARG;
    public static final CommandArgumentDefinition<String> LABELS_ARG;
    public static final CommandArgumentDefinition<String> CONTEXTS_ARG;
    public static final CommandArgumentDefinition<Integer> COUNT_ARG;
    public static final CommandArgumentDefinition<String> DRIVER_ARG;
    public static final CommandArgumentDefinition<String> DRIVER_PROPERTIES_FILE_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        CHANGELOG_FILE_ARG = builder.argument("changelogFile", String.class).required()
                .description("The root changelog").build();
        URL_ARG = builder.argument("url", String.class).required()
                .description("The JDBC database connection URL").build();
        DEFAULT_SCHEMA_NAME_ARG = builder.argument("defaultSchemaName", String.class)
                .description("The default schema name to use for the database connection").build();
        DEFAULT_CATALOG_NAME_ARG = builder.argument("defaultCatalogName", String.class)
                .description("The default catalog name to use for the database connection").build();
        DRIVER_ARG = builder.argument("driver", String.class)
            .description("The JDBC driver class").build();
        DRIVER_PROPERTIES_FILE_ARG = builder.argument("driverPropertiesFile", String.class)
            .description("The JDBC driver properties file").build();
        USERNAME_ARG = builder.argument("username", String.class)
                .description("The database username").build();
        PASSWORD_ARG = builder.argument("password", String.class)
                .description("The database password")
                .setValueObfuscator(ConfigurationValueObfuscator.STANDARD)
                .build();
        LABELS_ARG = builder.argument("labels", String.class)
                .description("Changeset labels to match").build();
        CONTEXTS_ARG = builder.argument("contexts", String.class)
                .description("Changeset contexts to match").build();
        COUNT_ARG = builder.argument("count", Integer.class).required()
                .description("Number of change sets to generate rollback SQL for").build();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    protected String[] collectArguments(CommandScope commandScope) throws CommandExecutionException {
        return collectArguments(commandScope, null, "count");
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Generates SQL to sequentially revert <count> number of changes");
    }
}
