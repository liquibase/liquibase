package liquibase.command.core;

import liquibase.command.AbstractCliWrapperCommandStep;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandScope;
import liquibase.command.CommandStepBuilder;
import liquibase.integration.commandline.Main;

public class UpdateCountCommandStep extends AbstractCliWrapperCommandStep {
    public static final CommandArgumentDefinition<String> CHANGELOG_FILE_ARG;
    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> LABELS_ARG;
    public static final CommandArgumentDefinition<String> CONTEXTS_ARG;
    public static final CommandArgumentDefinition<Integer> COUNT_ARG;

    static {
        CommandStepBuilder builder = new CommandStepBuilder(UpdateCountCommandStep.class);
        CHANGELOG_FILE_ARG = builder.argument("changeLogFile", String.class).required().build();
        URL_ARG = builder.argument("url", String.class).required().build();
        LABELS_ARG = builder.argument("labels", String.class).build();
        CONTEXTS_ARG = builder.argument("contexts", String.class).build();
        COUNT_ARG = builder.argument("count", Integer.class).build();
    }

    @Override
    public String[] getName() {
        return new String[] {"updateCount"};
    }

    @Override
    public void run(CommandScope commandScope) throws Exception {
        String[] args = createParametersFromArgs(createArgs(commandScope), "count");
        int statusCode = Main.run(args);
        commandScope.addResult("statusCode", statusCode);
    }

}
