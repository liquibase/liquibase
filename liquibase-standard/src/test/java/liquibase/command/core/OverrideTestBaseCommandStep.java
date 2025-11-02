package liquibase.command.core;

import liquibase.command.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Base CommandStep for testing CommandOverride functionality
 */
public class OverrideTestBaseCommandStep extends AbstractCommandStep {

    public static List<String> executionLog = new ArrayList<>();

    public static final String[] COMMAND_NAME = {"overrideTest"};

    public static void reset() {
        executionLog.clear();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME};
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        executionLog.add("OverrideTestBaseCommandStep");
    }
}
