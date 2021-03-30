package liquibase.command.core;

import liquibase.command.*;
import liquibase.integration.commandline.Main;

public class ReleaseLocksCommandStep extends AbstractCliWrapperCommandStep {
    public static final CommandArgumentDefinition<String> URL_ARG;

    static {
        CommandStepBuilder builder = new CommandStepBuilder(ReleaseLocksCommandStep.class);
        URL_ARG = builder.argument("url", String.class).required().build();
    }

    @Override
    public String[] getName() {
        return new String[] {"releaseLocks"};
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
