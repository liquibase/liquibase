package liquibase.ui;

public interface ConsoleInputValidator {
    <T> boolean validateInput(String input, T converted);
}