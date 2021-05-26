package liquibase.command;

/**
 * @deprecated Used by the old {@link LiquibaseCommand} style of command setup.
 */
public class CommandValidationErrors {
    private final LiquibaseCommand command;

    public CommandValidationErrors(LiquibaseCommand command) {
        this.command = command;
    }
}
