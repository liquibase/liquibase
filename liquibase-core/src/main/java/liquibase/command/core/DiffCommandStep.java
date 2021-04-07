package liquibase.command.core;

import liquibase.command.*;
import liquibase.integration.commandline.Main;

public class DiffCommandStep extends AbstractCliWrapperCommandStep {
    public static final CommandArgumentDefinition<String> REFERENCE_USERNAME_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_PASSWORD_ARG;
    public static final CommandArgumentDefinition<String> REFERENCE_URL_ARG;
    public static final CommandArgumentDefinition<String> USERNAME_ARG;
    public static final CommandArgumentDefinition<String> PASSWORD_ARG;
    public static final CommandArgumentDefinition<String> URL_ARG;

    static {
        CommandStepBuilder builder = new CommandStepBuilder(DiffCommandStep.class);
        REFERENCE_USERNAME_ARG = builder.argument("referenceUsername", String.class).build();
        REFERENCE_PASSWORD_ARG = builder.argument("referencePassword", String.class).build();
        REFERENCE_URL_ARG = builder.argument("referenceUrl", String.class).required().build();
        USERNAME_ARG = builder.argument("username", String.class).build();
        PASSWORD_ARG = builder.argument("password", String.class).build();
        URL_ARG = builder.argument("url", String.class).required().build();
    }

    @Override
    public String[] getName() {
        return new String[] {"diff"};
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        String[] args = createArgs(commandScope);
        int statusCode = Main.run(args);
        addStatusMessage(resultsBuilder, statusCode);
        resultsBuilder.addResult("statusCode", statusCode);
    }
}
