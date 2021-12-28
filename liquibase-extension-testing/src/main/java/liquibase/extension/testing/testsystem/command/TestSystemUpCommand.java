package liquibase.extension.testing.testsystem.command;

import liquibase.command.AbstractCommandStep;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandBuilder;
import liquibase.command.CommandResultsBuilder;
import liquibase.extension.testing.testsystem.TestSystem;
import liquibase.extension.testing.testsystem.TestSystemFactory;

public class TestSystemUpCommand extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"sdk", "up"};

    public static final CommandArgumentDefinition<String> SYSTEM_NAME;
    public static final CommandArgumentDefinition<String> VERSION;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);

        SYSTEM_NAME = builder.argument("system", String.class).required()
                .description("The system to start").build();
        VERSION = builder.argument("version", String.class)
                .description("Override version to use").build();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{
                COMMAND_NAME
        };
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {

        final TestSystem env = new TestSystemFactory().getTestSystem(resultsBuilder.getCommandScope().getConfiguredValue(SYSTEM_NAME).getValue());

        env.start(true);

        System.out.println("Start environment '" + env + "'");
    }
}
