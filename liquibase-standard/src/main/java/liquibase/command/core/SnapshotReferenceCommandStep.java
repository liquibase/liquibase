package liquibase.command.core;

import liquibase.command.*;
import liquibase.configuration.ConfigurationValueObfuscator;
import liquibase.exception.CommandExecutionException;

public class SnapshotReferenceCommandStep extends AbstractCliWrapperCommandStep {

    public static final String[] COMMAND_NAME = {"snapshotReference"};

    public static final CommandArgumentDefinition<String> REFERENCE_USERNAME_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_PASSWORD_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_URL_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_DEFAULT_SCHEMA_NAME_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_DEFAULT_CATALOG_NAME_ARG;
    public static final CommandArgumentDefinition<String> SNAPSHOT_FORMAT_ARG;
    public static final CommandArgumentDefinition<String> DRIVER_ARG;
    public static final CommandArgumentDefinition<String> DRIVER_PROPERTIES_FILE_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        REFERENCE_URL_ARG = builder.argument("referenceUrl", String.class).required()
                .setValueObfuscator(ConfigurationValueObfuscator.URL_OBFUSCATOR)
                .description("The JDBC reference database connection URL").build();
        REFERENCE_DEFAULT_SCHEMA_NAME_ARG = builder.argument("referenceDefaultSchemaName", String.class)
                .description("The default schema name to use for the database connection").build();
        REFERENCE_DEFAULT_CATALOG_NAME_ARG = builder.argument("referenceDefaultCatalogName", String.class)
                .description("The default catalog name to use for the database connection").build();
        REFERENCE_USERNAME_ARG = builder.argument("referenceUsername", String.class)
                .description("Reference username to use to connect to the database").build();
        REFERENCE_PASSWORD_ARG = builder.argument("referencePassword", String.class)
                .description("Reference password to use to connect to the database")
                .setValueObfuscator(ConfigurationValueObfuscator.STANDARD)
                .build();
        DRIVER_ARG = builder.argument("driver", String.class)
                .description("The JDBC driver class").build();
        DRIVER_PROPERTIES_FILE_ARG = builder.argument("driverPropertiesFile", String.class)
                .description("The JDBC driver properties file").build();
        SNAPSHOT_FORMAT_ARG = builder.argument("snapshotFormat", String.class)
                .description("Output format to use (JSON or YAML").build();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Capture the current state of the reference database");
    }

    @Override
    protected String[] collectArguments(CommandScope commandScope) throws CommandExecutionException {
        return collectArguments(commandScope, null, null);
    }
}
