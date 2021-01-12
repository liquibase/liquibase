package liquibase.ui;

import liquibase.AbstractExtensibleObject;
import liquibase.Scope;
import liquibase.configuration.ConfigurationProperty;
import liquibase.configuration.GlobalConfiguration;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.exception.LiquibaseException;
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
    private boolean allowPrompt=true;

    private ConsoleWrapper console;

    public ConsoleUIService() {
    }

    protected ConsoleUIService(ConsoleWrapper console) {
        this.console = console;
    }

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
    public void setAllowPrompt(boolean allowPrompt) throws IllegalArgumentException {
        this.allowPrompt = allowPrompt;
    }

    @Override
    public boolean getAllowPrompt() {
        return allowPrompt;
    }

    @Override
    public <T> T prompt(String prompt, T defaultValue, InputHandler<T> inputHandler, Class<T> type) {
        //
        // Check the allowPrompt flag
        //
        Logger log = Scope.getCurrentScope().getLog(getClass());
        if (! allowPrompt) {
            log.fine("No prompt for input is allowed at this time");
            return defaultValue;
        }
        final ConsoleWrapper console = getConsole();

        if (!console.supportsInput()) {
            log.fine("No console attached. Skipping interactive prompt: '" + prompt + "'. Using default value '" + defaultValue + "'");
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

    /**
     * Creates the {@link ConsoleWrapper} to use.
     */
    protected ConsoleWrapper getConsole() {
        if (console == null) {
            final ConfigurationProperty headless = LiquibaseConfiguration.getInstance().getProperty(GlobalConfiguration.class, GlobalConfiguration.HEADLESS);
            boolean headlessConfigValue = headless.getValue(Boolean.class);
            boolean wasHeadlessOverridden = headless.getWasOverridden();

            final Logger log = Scope.getCurrentScope().getLog(getClass());

            if (headlessConfigValue) {
                log.fine("Not prompting for user input because liquibase.headless=true. Set to 'false' in liquibase.properties to change this behavior.");
                console = new ConsoleWrapper(null);
            } else {
                final Console systemConsole = System.console();

                this.console = new ConsoleWrapper(systemConsole);

                if (systemConsole == null) {
                    log.fine("No system console detected for user input");
                    if (wasHeadlessOverridden) {
                        throw new UnexpectedLiquibaseException("liquibase.headless was set to false, but Liquibase was run in an environment with no system console");
                    }
                } else {
                    log.fine("A system console was detected for user input");
                }
            }
            if (!wasHeadlessOverridden) {
                log.fine("To override or validate the auto-detected environment for user input, set the liquibase.headless property in liquibase.properties file.");
            }
        }
        return console;
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
     * Wrapper around {@link Console} to allow replacements as needed.
     * If the passed {@link Console} is null, {@link #supportsInput()} will return false, and {@link #readLine()} will return null.
     */
    public static class ConsoleWrapper {

        private final Console console;

        public ConsoleWrapper(Console console) {
            this.console = console;
        }

        public String readLine() {
            if (console == null) {
                return "";
            }
            return console.readLine();
        }

        public boolean supportsInput() {
            return console != null;
        }
    }
}
