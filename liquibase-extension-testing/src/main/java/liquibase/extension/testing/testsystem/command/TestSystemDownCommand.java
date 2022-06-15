package liquibase.extension.testing.testsystem.command;

import liquibase.command.AbstractCommandStep;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandBuilder;
import liquibase.command.CommandResultsBuilder;
import liquibase.extension.testing.testsystem.TestSystem;
import liquibase.extension.testing.testsystem.TestSystemFactory;

public class TestSystemDownCommand extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"sdk", "system", "down"};

    public static final CommandArgumentDefinition<String> NAME;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);

        NAME = builder.argument("name", String.class).required()
                .description("The name of the system to").build();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{
                COMMAND_NAME
        };
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {

        final TestSystem env = new TestSystemFactory().getTestSystem(resultsBuilder.getCommandScope().getConfiguredValue(NAME).getValue());

        env.stop();
    }
}
