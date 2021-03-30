package liquibase.command.core;

import liquibase.command.*;
import liquibase.integration.commandline.Main;

public class TagExistsCommandStep extends AbstractCliWrapperCommandStep {
    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> TAG_ARG;

    static {
        CommandStepBuilder builder = new CommandStepBuilder(TagExistsCommandStep.class);
        URL_ARG = builder.argument("url", String.class).required().build();
        TAG_ARG = builder.argument("tag", String.class).build();
    }

    @Override
    public String[] getName() {
        return new String[] {"tagExists"};
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        String[] args = createParametersFromArgs(createArgs(commandScope), "tag");
        int statusCode = Main.run(args);
        addStatusMessage(resultsBuilder, statusCode);
        resultsBuilder.addResult("statusCode", statusCode);
    }

}
