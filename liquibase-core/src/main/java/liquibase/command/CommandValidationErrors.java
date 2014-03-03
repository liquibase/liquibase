package liquibase.command;

public class CommandValidationErrors {
    private final LiquibaseCommand command;

    public CommandValidationErrors(LiquibaseCommand command) {
        this.command = command;
    }
}
