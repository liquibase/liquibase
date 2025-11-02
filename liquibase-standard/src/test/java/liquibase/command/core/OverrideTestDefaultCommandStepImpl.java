package liquibase.command.core;

import liquibase.command.CommandOverride;
import liquibase.command.CommandResultsBuilder;

/**
 * Default override that executes for all databases (empty supportedDatabases)
 */
@CommandOverride(override = OverrideTestDefaultBaseCommandStep.class)
public class OverrideTestDefaultCommandStepImpl extends OverrideTestDefaultBaseCommandStep {

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        executionLog.add("OverrideTestDefaultCommandStep");
    }
}
