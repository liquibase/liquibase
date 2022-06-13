package liquibase.command;

public class CommandFailedException extends Exception {
    private final CommandResults results;
    private final int exitCode;
    private final boolean hidden;

    public CommandFailedException(CommandResults results, int exitCode, String message, boolean hidden) {
        super(message);
        this.results = results;
        this.exitCode = exitCode;
        this.hidden = hidden;
    }

    public CommandResults getResults() {
        return results;
    }

    public int getExitCode() {
        return exitCode;
    }

    /**
     * In certain circumstances, this exception is thrown solely to set the exit code of a command's execution. In these
     * cases, Liquibase does not print the stacktrace of the exception to the logs, and isHidden returns true.
     */
    public boolean isHidden() {
        return hidden;
    }
}