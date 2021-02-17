package liquibase.command;

public class CommandValidationErrors {
    private LiquibaseCommand command;
    private String error;

    public CommandValidationErrors(LiquibaseCommand command) {
        this.command = command;
    }

    public CommandValidationErrors(String error) {
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
