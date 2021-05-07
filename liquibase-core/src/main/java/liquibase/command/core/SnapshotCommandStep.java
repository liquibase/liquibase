package liquibase.command.core;

import liquibase.command.*;
import liquibase.integration.commandline.Main;

public class SnapshotCommandStep extends AbstractCliWrapperCommandStep {

    public static final String[] COMMAND_NAME = {"snapshot"};

    public static final CommandArgumentDefinition<String> USERNAME_ARG;
    public static final CommandArgumentDefinition<String> PASSWORD_ARG;
    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> SNAPSHOT_FORMAT_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        URL_ARG = builder.argument("url", String.class).required()
            .description("The JDBC database connection URL").build();
        USERNAME_ARG = builder.argument("username", String.class)
            .description("Username to use to connect to the database").build();
        PASSWORD_ARG = builder.argument("password", String.class)
            .description("Password to use to connect to the database").build();
        SNAPSHOT_FORMAT_ARG = builder.argument("snapshotFormat", String.class)
            .description("Output format to use (JSON or YAML").build();
    }

    @Override
    public String[] getName() {
        return COMMAND_NAME;
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Capture the current state of the database");
    }
}
