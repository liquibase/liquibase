package liquibase.command.core;

import liquibase.command.*;
import liquibase.integration.commandline.Main;

public class ClearCheckSumsCommandStep extends AbstractCliWrapperCommandStep {
    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> USERNAME_ARG;
    public static final CommandArgumentDefinition<String> PASSWORD_ARG;

    static {
        CommandStepBuilder builder = new CommandStepBuilder(ClearCheckSumsCommandStep.class);
        URL_ARG = builder.argument("url", String.class).required()
            .description("The JDBC database connection URL").build();
        USERNAME_ARG = builder.argument("username", String.class)
            .description("The database username").build();
        PASSWORD_ARG = builder.argument("username", String.class)
            .description("The database password").build();
    }

    @Override
    public String[] getName() {
        return new String[] {"clearCheckSums"};
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
        commandDefinition.setShortDescription("Clears all checksums");
        commandDefinition.setLongDescription("Clears all checksums and nullifies the MD5SUM column of the " +
            "DATABASECHANGELOG table so that they will be re-computed on the next database update");
    }
}
