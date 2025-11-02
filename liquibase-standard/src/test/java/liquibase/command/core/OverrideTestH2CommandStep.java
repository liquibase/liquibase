package liquibase.command.core;

import liquibase.command.CommandOverride;
import liquibase.command.CommandResultsBuilder;
import liquibase.database.core.H2Database;

/**
 * Override that only executes for H2 databases
 */
@CommandOverride(override = OverrideTestBaseCommandStep.class, supportedDatabases = {H2Database.class})
public class OverrideTestH2CommandStep extends OverrideTestBaseCommandStep {

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        executionLog.add("OverrideTestH2CommandStep");
    }
}
