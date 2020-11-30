package liquibase.ui;

import liquibase.exception.LiquibaseException;

import java.io.Console;

/**
 *
 * Wrapper around the Java Console class.
 * Sub-classes can override these methods for testing, etc.
 *
 */
public class ConsoleDelegate {
    private Console console;

    public ConsoleDelegate() {
        this.console = System.console();
    }

    public String readLine() throws LiquibaseException {
        if (this.console == null) {
            throw new LiquibaseException("No console available");
        }
        return console.readLine();
    }
}
