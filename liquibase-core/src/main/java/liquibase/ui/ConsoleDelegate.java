package liquibase.ui;

import liquibase.exception.LiquibaseException;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

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

    public boolean ready() throws LiquibaseException, IOException {
        if (this.console == null) {
            throw new LiquibaseException("No console available");
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        return reader.ready();
    }

    public String readLine() throws LiquibaseException {
        if (this.console == null) {
            throw new LiquibaseException("No console available");
        }
        return console.readLine();
    }
}
