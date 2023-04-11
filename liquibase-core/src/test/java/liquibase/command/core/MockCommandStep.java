package liquibase.command.core;

import liquibase.command.*;

/**
 * A command step for use in unit tests
 */
public class MockCommandStep extends AbstractCommandStep {

    public static MockCommandStep logic;

    public static final String[] COMMAND_NAME = {"mock"};

    public static final CommandArgumentDefinition<String> VALUE_1_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);

        VALUE_1_ARG = builder.argument("value1", String.class)
                .description("Value 1").build();
    }

        /**
         * Resets any internal state, including removing defined arguments or defintions
         */
    public static void reset() {
        logic = null;
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME};
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        if (logic == null) {
            resultsBuilder.getOutputStream().write("Mock command ran".getBytes());
        } else {
            logic.run(resultsBuilder);
        }
    }
}
