package liquibase.command.core;

import liquibase.command.*;
import liquibase.configuration.ConfigurationValueObfuscator;
import liquibase.exception.CommandExecutionException;

public class HistoryCommandStep extends AbstractCliWrapperCommandStep {

    public static final String[] COMMAND_NAME = {"history"};

    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> DEFAULT_SCHEMA_NAME_ARG;
    public static final CommandArgumentDefinition<String> DEFAULT_CATALOG_NAME_ARG;
    public static final CommandArgumentDefinition<String> USERNAME_ARG;
    public static final CommandArgumentDefinition<String> PASSWORD_ARG;
    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> DRIVER_ARG;
    public static final CommandArgumentDefinition<String> DRIVER_PROPERTIES_FILE_ARG;
    public static final CommandArgumentDefinition<HistoryFormat> FORMAT_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        URL_ARG = builder.argument(CommonArgumentNames.URL, String.class).required()
                .description("The JDBC database connection URL").build();
        DEFAULT_SCHEMA_NAME_ARG = builder.argument("defaultSchemaName", String.class)
                .description("The default schema name to use for the database connection").build();
        DEFAULT_CATALOG_NAME_ARG = builder.argument("defaultCatalogName", String.class)
                .description("The default catalog name to use for the database connection").build();
        DRIVER_ARG = builder.argument("driver", String.class)
                .description("The JDBC driver class").build();
        DRIVER_PROPERTIES_FILE_ARG = builder.argument("driverPropertiesFile", String.class)
                .description("The JDBC driver properties file").build();
        USERNAME_ARG = builder.argument(CommonArgumentNames.USERNAME, String.class)
                .description("Username to use to connect to the database").build();
        PASSWORD_ARG = builder.argument(CommonArgumentNames.PASSWORD, String.class)
                .description("Password to use to connect to the database")
                .setValueObfuscator(ConfigurationValueObfuscator.STANDARD)
                .build();
        CHANGELOG_FILE_ARG = builder.argument(CommonArgumentNames.CHANGELOG_FILE, String.class)
                .description("The root changelog").build();
        FORMAT_ARG = builder.argument("format", HistoryFormat.class)
                .description("History output format")
                .defaultValue(HistoryFormat.TABULAR)
                .build();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("List all deployed changesets and their deployment ID");
    }

    @Override
    protected String[] collectArguments(CommandScope commandScope) throws CommandExecutionException {
        return collectArguments(commandScope, null, null);
    }
}
