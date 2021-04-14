package liquibase.command.core;

import liquibase.command.*;
import liquibase.integration.commandline.Main;

public class ListLocksCommandStep extends AbstractCliWrapperCommandStep {
    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> USERNAME_ARG;
    public static final CommandArgumentDefinition<String> PASSWORD_ARG;
    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;

    static {
        CommandStepBuilder builder = new CommandStepBuilder(ListLocksCommandStep.class);
        URL_ARG = builder.argument("url", String.class).required()
            .description("The JDBC database connection URL").build();
        USERNAME_ARG = builder.argument("username", String.class)
            .description("Username to use to connect to the database").build();
        PASSWORD_ARG = builder.argument("password", String.class)
            .description("Password to use to connect to the database").build();
        CHANGELOG_FILE_ARG = builder.argument("changeLogFile", String.class)
            .description("File to write changelog to").build();
    }

    @Override
    public String[] getName() {
        return new String[] {"listLocks"};
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
        commandDefinition.setShortDescription("List the hostname, IP address, and timestamp of the Liquibase lock record");
        commandDefinition.setLongDescription("List the hostname, IP address, and timestamp of the Liquibase lock record");
    }
}
