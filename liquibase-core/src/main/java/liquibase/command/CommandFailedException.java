package liquibase.command;

public class CommandFailedException extends Exception {
    private final CommandResults results;
    private final int exitCode;

    public CommandFailedException(CommandResults results, int exitCode, String message) {
        super(message);
        this.results = results;
        this.exitCode = exitCode;
    }

    public CommandResults getResults() {
        return results;
    }

    public int getExitCode() {
        return exitCode;
    }
}