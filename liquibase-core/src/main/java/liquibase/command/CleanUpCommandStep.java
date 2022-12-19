package liquibase.command;

public interface CleanUpCommandStep {

    void cleanUp(CommandResultsBuilder resultsBuilder);
}
