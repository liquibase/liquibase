package liquibase.ui;

import liquibase.AbstractExtensibleObject;
import liquibase.Scope;
import liquibase.configuration.ConfigurationProperty;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.logging.Logger;
import liquibase.util.StringUtil;

import java.io.Console;
import java.io.PrintStream;

/**
 * {@link UIService} implementation that sends messages to stdout and stderr.
 */
public class ConsoleUIService extends AbstractExtensibleObject implements UIService {

    private PrintStream outputStream = System.out;
    private PrintStream errorStream = System.out;
    private boolean outputStackTraces = false;

    /**
     * Returns {@link liquibase.plugin.Plugin#PRIORITY_NOT_APPLICABLE} because it must be manually configured as needed
     */
    @Override
    public int getPriority() {
        return PRIORITY_NOT_APPLICABLE;
    }

    @Override
    public void sendMessage(String message) {
        getOutputStream().println(message);
    }

    @Override
    public void sendErrorMessage(String message) {
        getErrorStream().println(message);
    }

    @Override
    public void sendErrorMessage(String message, Throwable exception) {
        sendErrorMessage(message);
        if (getOutputStackTraces()) {
            exception.printStackTrace(getErrorStream());
        }
    }

    @Override
    public <T> T prompt(String prompt, T defaultValue, InputHandler<T> inputHandler, Class<T> type) {
        Logger log = Scope.getCurrentScope().getLog(getClass());

        ConsoleWrapper console = getConsole();

        final ConfigurationProperty headless = LiquibaseConfiguration.getInstance().getProperty(GlobalConfiguration.class, GlobalConfiguration.HEADLESS);
        if (headless.getWasOverridden()) {
            if (headless.getValue(Boolean.class)) {
                console = null;
            } else {
                if (console == null) {
                    throw new UnexpectedLiquibaseException("liquibase.headless was set to true, but Liquibase was run in a headless environment");
                }
            }
        }

        if (console == null) {
            log.fine("No console attached so cannot prompt '" + prompt + "'. Using default value '" + defaultValue + "'");
            return defaultValue;
        }

        if (inputHandler == null) {
            inputHandler = new DefaultInputHandler<>();
        }

        String initialMessage = prompt;
        if (defaultValue != null) {
            initialMessage += " (default \"" + defaultValue + "\")";
        }
        this.sendMessage(initialMessage + ": ");

        while (true) {
            String input = StringUtil.trimToNull(console.readLine());
            try {
                if (input == null) {
                    return defaultValue;
                }
                return inputHandler.parseInput(input, type);
            } catch (IllegalArgumentException e) {
                this.sendMessage("Invalid value: \"" + input + "\"");
                this.sendMessage(prompt + ": ");
            }
        }
    }

    protected ConsoleWrapper getConsole() {
        final Console console = System.console();
        if (console == null) {
            return null;
        }
        return new ConsoleWrapper(console);
    }

    @SuppressWarnings("WeakerAccess")
    public PrintStream getOutputStream() {
        return outputStream;
    }

    @SuppressWarnings("unused")
    public void setOutputStream(PrintStream outputStream) {
        this.outputStream = outputStream;
    }

    @SuppressWarnings("WeakerAccess")
    public PrintStream getErrorStream() {
        return errorStream;
    }

    @SuppressWarnings("unused")
    public void setErrorStream(PrintStream errorStream) {
        this.errorStream = errorStream;
    }

    @SuppressWarnings("WeakerAccess")
    public boolean getOutputStackTraces() {
        return outputStackTraces;
    }

    /**
     * Set to true to output stacktraces. Defaults to not outputing them.
     */
    @SuppressWarnings("unused")
    public void setOutputStackTraces(boolean outputStackTraces) {
        this.outputStackTraces = outputStackTraces;
    }

    /**
     * Wrapper around {@link Console} to allow replacements as needed. Primarily used for testing.
     */
    public static class ConsoleWrapper {

        private Console console;

        public ConsoleWrapper(Console console) {
            this.console = console;
        }

        public String readLine() {
            return console.readLine();
        }
    }
}
