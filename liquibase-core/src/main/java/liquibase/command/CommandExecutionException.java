package liquibase.command;

/**
 * @deprecated Used by the old {@link LiquibaseCommand} style of command setup.
 */
public class CommandExecutionException extends Exception {
    private static final long serialVersionUID = -2115326810859901171L;

    public CommandExecutionException(Throwable cause) {
        super(cause);
    }
}
