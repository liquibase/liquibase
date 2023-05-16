package liquibase.command;

import liquibase.exception.LiquibaseException;

/**
 * Interface implemented by CommandSteps when they need to execute clean up tasks (such as closing database
 * connections, flushing files, etc) after other steps in the pipeline are executed.
 */
public interface CleanUpCommandStep {

    /**
     * Method invoked to execute the cleanup action.
     * @param resultsBuilder builder used in this pipeline
     */
    void cleanUp(CommandResultsBuilder resultsBuilder);
}
