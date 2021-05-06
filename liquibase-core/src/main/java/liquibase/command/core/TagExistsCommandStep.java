package liquibase.command.core;

import liquibase.command.*;
import liquibase.integration.commandline.Main;

import java.util.ArrayList;
import java.util.List;

public class TagExistsCommandStep extends AbstractCliWrapperCommandStep {

    public static final String[] COMMAND_NAME = {"tagExists"};

    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> USERNAME_ARG;
    public static final CommandArgumentDefinition<String> PASSWORD_ARG;
    public static final CommandArgumentDefinition<String> TAG_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        URL_ARG = builder.argument("url", String.class).required()
            .description("The JDBC database connection URL").build();
        USERNAME_ARG = builder.argument("username", String.class)
            .description("Username to use to connect to the database").build();
        PASSWORD_ARG = builder.argument("password", String.class)
            .description("Password to use to connect to the database").build();
        TAG_ARG = builder.argument("tag", String.class).required()
            .description("Tag to check").build();
    }

    @Override
    public String[] getName() {
        return COMMAND_NAME;
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        List<String> rhs = new ArrayList<>();
        rhs.add("tag");
        String[] argsFromScope = createArgs(commandScope, rhs);
        String[] args = createParametersFromArgs(argsFromScope, "tag");
        int statusCode = Main.run(args);
        addStatusMessage(resultsBuilder, statusCode);
        resultsBuilder.addResult("statusCode", statusCode);
    }

}
