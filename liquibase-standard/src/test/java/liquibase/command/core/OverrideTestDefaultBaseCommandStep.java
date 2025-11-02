package liquibase.command.core;

import liquibase.command.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Separate base CommandStep for testing default override (no supportedDatabases)
 */
public class OverrideTestDefaultBaseCommandStep extends AbstractCommandStep {

    public static List<String> executionLog = new ArrayList<>();

    public static final String[] COMMAND_NAME = {"overrideTestDefault"};

    public static void reset() {
        executionLog.clear();
    }

    @Override
    public String[][] defineCommandNames() {
        return new String[][]{COMMAND_NAME};
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        executionLog.add("OverrideTestDefaultBaseCommandStep");
    }
}
