package liquibase.command.core;

import liquibase.command.*;
import liquibase.integration.commandline.Main;

import java.util.UUID;

public class DropAllCommandStep extends AbstractCliWrapperCommandStep {
    public static final CommandArgumentDefinition<String> USERNAME_ARG;
    public static final CommandArgumentDefinition<String> PASSWORD_ARG;
    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> HUB_CONNECTION_ID_ARG;

    static {
        CommandStepBuilder builder = new CommandStepBuilder(DropAllCommandStep.class);
        URL_ARG = builder.argument("url", String.class).required()
            .description("The JDBC database connection URL").build();
        USERNAME_ARG = builder.argument("username", String.class)
            .description("Username to use to connect to the database").build();
        PASSWORD_ARG = builder.argument("password", String.class)
            .description("Password to use to connect to the database").build();
        CHANGELOG_FILE_ARG = builder.argument("changeLogFile", String.class)
            .description("The root changelog").build();
        HUB_CONNECTION_ID_ARG = builder.argument("hubConnectionId", String.class)
            .description("The Hub connection ID").build();
    }

    @Override
    public String[] getName() {
        return new String[] {"dropAll"};
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        String[] args = createArgs(commandScope);
        int statusCode = Main.run(args);
        addStatusMessage(resultsBuilder, statusCode);
        resultsBuilder.addResult("statusCode", statusCode);
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Drop all database objects owned by the user");
        commandDefinition.setLongDescription("Drop all database objects owned by the user");
    }
}
