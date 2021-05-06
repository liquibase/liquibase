package liquibase.command.core;

import liquibase.command.AbstractCommandStep;
import liquibase.command.CommandResultsBuilder;

/**
 * A command step for use in unit tests
 */
public class MockCommandStep extends AbstractCommandStep {

    public static MockCommandStep logic;

    /**
     * Resets any internal state, including removing defined arguments or defintions
     */
    public static void reset() {
        logic = null;
    }

    @Override
    public String[] getName() {
        return new String[]{"mock"};
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
