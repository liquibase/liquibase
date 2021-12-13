package liquibase.extension.testing.environment.command;

import liquibase.command.AbstractCommandStep;
import liquibase.command.CommandArgumentDefinition;
import liquibase.command.CommandBuilder;
import liquibase.command.CommandResultsBuilder;
import liquibase.extension.testing.environment.TestEnvironment;
import liquibase.extension.testing.environment.TestEnvironmentFactory;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.lifecycle.Startable;

public class EnvironmentUpCommand extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"sdk", "env", "up"};

    public static final CommandArgumentDefinition<String> ENV_NAME;
    public static final CommandArgumentDefinition<String> IMAGE_NAME;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);

        ENV_NAME = builder.argument("env", String.class).required()
                .description("The environment to start").build();
        IMAGE_NAME = builder.argument("imageName", String.class)
                .description("Override image name to use").build();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{
                COMMAND_NAME
        };
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {

        final TestEnvironment env = new TestEnvironmentFactory().getEnvironment(resultsBuilder.getCommandScope().getConfiguredValue(ENV_NAME).getValue());

        env.start();

        System.out.println("Start environment '" + env.toString() + "'");
    }
}
