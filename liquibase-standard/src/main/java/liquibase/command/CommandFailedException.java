package liquibase.command;

import liquibase.exception.ExitCodeException;

/**
 * CommandFailedException is thrown any time a command did not succeed. If it did not succeed due to normal and expected
 * reasons, mark it as expected=true. If the CommandFailedException is marked as expected=false, the code calling the
 * command may want to do additional logging or handling of the exception because it knows the command was surprised by the result.
 */
public class CommandFailedException extends Exception implements ExitCodeException {

    private static final long serialVersionUID = -394350095952659571L;
    private final CommandResults results;
    private final int exitCode;
    private final boolean expected;

    public CommandFailedException(CommandResults results, int exitCode, String message, boolean expected) {
        super(message);
        this.results = results;
        this.exitCode = exitCode;
        this.expected = expected;
    }

    public CommandResults getResults() {
        return results;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    /**
     * In certain circumstances, this exception is thrown solely to set the exit code of a command's execution, in which
     * case, the exception is expected. In these cases, Liquibase does not print the stacktrace of the exception to the
     * logs, and isExpected returns true.
     */
    public boolean isExpected() {
        return expected;
    }
}
