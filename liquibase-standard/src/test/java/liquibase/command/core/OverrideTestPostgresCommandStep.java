package liquibase.command.core;

import liquibase.command.CommandOverride;
import liquibase.command.CommandResultsBuilder;
import liquibase.database.core.PostgresDatabase;

/**
 * Override that only executes for Postgres databases
 */
@CommandOverride(override = OverrideTestBaseCommandStep.class, supportedDatabases = {PostgresDatabase.class})
public class OverrideTestPostgresCommandStep extends OverrideTestBaseCommandStep {

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        executionLog.add("OverrideTestPostgresCommandStep");
    }
}
