package liquibase.ui;

import liquibase.AbstractExtensibleObject;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.configuration.ConfiguredValue;
import liquibase.logging.Logger;
import liquibase.util.StringUtil;

import java.io.*;

/**
 * {@link UIService} implementation that sends messages to stdout and stderr.
 */
public class ConsoleUIService extends AbstractExtensibleObject implements UIService {

    private PrintStream outputStream = System.out;
    private PrintStream errorStream = System.out;
    private boolean outputStackTraces = false;
    private boolean allowPrompt = false;

    private ConsoleWrapper console;

    public static final String TERM_PROGRAM = "TERM_PROGRAM";
    public static final String MINTTY = "mintty";
    public static final String MSYSTEM = "MSYSTEM";
    public static final String MINGW64 = "mingw64";

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
        return getConsole().supportsInput() && allowPrompt;
    }

    @Override
    public <T> T prompt(String prompt, T valueIfNoEntry, InputHandler<T> inputHandler, Class<T> type) {
        //
        // Check the allowPrompt flag
        //
        Logger log = Scope.getCurrentScope().getLog(getClass());
        if (! allowPrompt) {
            log.fine("No prompt for input is allowed at this time");
            return valueIfNoEntry;
        }
        final ConsoleWrapper console = getConsole();

        if (!console.supportsInput()) {
            log.fine("No console attached. Skipping interactive prompt: '" + prompt + "'. Using default value '" + valueIfNoEntry + "'");
            return valueIfNoEntry;
        }

        if (inputHandler == null) {
            inputHandler = new DefaultInputHandler<>();
        }

        String initialMessage = prompt;
        if (valueIfNoEntry != null) {
            initialMessage += " [" + valueIfNoEntry + "]";
        }
        this.sendMessage(initialMessage + ": ");

        while (true) {
            String input = StringUtil.trimToNull(console.readLine());
            try {
                if (input == null) {
                    if (inputHandler.shouldAllowEmptyInput()) {
                        return valueIfNoEntry;
                    } else {
                        throw new IllegalArgumentException("Empty values are not permitted.");
                    }
                }
                return inputHandler.parseInput(input, type);
            }  catch (IllegalArgumentException e) {
                String message;
                if (e.getMessage() != null) {
                    message = e.getMessage();
                } else {
                    message = "Invalid value: \"" + input + "\"";
                }
                this.sendMessage(message);
                this.sendMessage(initialMessage + ": ");
            }
        }
    }

    /**
     * Creates the {@link ConsoleWrapper} to use.
     */
    protected ConsoleWrapper getConsole() {
        if (console == null) {
            final ConfiguredValue<Boolean> headlessValue = GlobalConfiguration.HEADLESS.getCurrentConfiguredValue();
            boolean headlessConfigValue = headlessValue.getValue();
            boolean wasHeadlessOverridden = !headlessValue.wasDefaultValueUsed();

            final Logger log = Scope.getCurrentScope().getLog(getClass());

            if (headlessConfigValue) {
                log.fine("Not prompting for user input because liquibase.headless=true. Set to 'false' in liquibase.properties to change this behavior.");
                console = new ConsoleWrapper(null, false);
            } else {
                //
                // If no system console and headless was not overridden as headless=false
                // then first, check the TERM_PROGRAM environment variable setting
                // to detect whether we need to read from stdin
                // otherwise check the MSYSTEM environment variable to look for the value "mingw"
                //
                final Console systemConsole = System.console();
                boolean useStdIn = wasHeadlessOverridden;
                String minTtyValue = System.getenv(TERM_PROGRAM);
                if (systemConsole == null && ! useStdIn) {
                    if (StringUtil.isNotEmpty(minTtyValue)) {
                        useStdIn = minTtyValue.equalsIgnoreCase(MINTTY);
                    }
                    if (! useStdIn) {
                        String msystem = System.getenv(MSYSTEM);
                        useStdIn = msystem != null && msystem.equalsIgnoreCase(MINGW64);
                    }
                }
                this.console = new ConsoleWrapper(systemConsole, useStdIn);

                if (systemConsole == null) {
                    log.fine("No system console detected for user input");
                    if (useStdIn) {
                        log.fine("Input will be from stdin");
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
        private final boolean useStdin;

        /**
         *
         * This constructor is used for extensions that provide UIService implementations
         *
         * @param   console    the console to use
         *
         */
        public ConsoleWrapper(Console console) {
            this(console, false);
        }

        public ConsoleWrapper(Console console, boolean useStdInParam) {
            this.console = console;
            this.useStdin = useStdInParam;
        }

        public String readLine() {
            if (console == null) {
                if (! useStdin) {
                    return "";
                }
                try {
                    return new BufferedReader(new InputStreamReader(System.in)).readLine();
                } catch (IOException ioe) {
                    //
                    // Throw an exception if we can't read
                    //
                    throw new RuntimeException("Unable to read from the system input stream", ioe);
                }
            }
            return console.readLine();
        }

        public boolean supportsInput() {
            return console != null || useStdin;
        }
    }
}
