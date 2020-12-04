package liquibase.ui;

import liquibase.AbstractExtensibleObject;
import liquibase.Scope;
import liquibase.exception.LiquibaseException;
import liquibase.util.ObjectUtil;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Thread.sleep;
import static liquibase.util.ObjectUtil.convert;

/**
 * {@link UIService} implementation that sends messages to stdout and stderr.
 */
public class ConsoleUIService extends AbstractExtensibleObject implements UIService {

    private PrintStream outputStream = System.out;
    private PrintStream errorStream = System.out;
    private boolean outputStackTraces = false;
    private Object defaultValue;

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
    /**
     *
     * Prompt the user with the message and wait for the timeout period.
     * If the timeout expires, return the default value.
     * The return is of type T
     *
     * @param   promptString     String to display as a prompt
     * @param   defaultValue     String to return as a default
     * @param   timerValue       Value to use as a countdown timer
     *                           Must be a valid integer > 0
     * @param   type             return type
     * @return  T                Instance of specified type
     *
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T prompt(String promptString, T defaultValue, int timerValue, Class<T> type)
            throws LiquibaseException {
        return prompt(promptString, defaultValue, timerValue, null, type);
    }

    /**
     *
     * Prompt the user with the message and wait for the timeout period.
     * If the timeout expires, return the default value.
     * The return is of type T
     *
     * @param   promptString     String to display as a prompt
     * @param   defaultValue     String to return as a default
     * @param   timerValue       Value to use as a countdown timer
     *                           Must be a valid integer > 0
     * @param   validator        Input validator (optional)
     * @param   type             return type
     * @return  T                Instance of specified type
     *
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T prompt(String promptString, T defaultValue, int timerValue, ConsoleInputValidator validator, Class<T> type)
            throws LiquibaseException {
        if (timerValue <= 0) {
            throw new IllegalArgumentException("Value for countdown timer must be greater than 0");
        }

        //
        // If we don't have a console then we just return the default value
        //
        ConsoleDelegate consoleDelegate = getConsoleDelegate();
        if (! consoleDelegate.hasConsole()) {
            return defaultValue;
        }
        int count = timerValue;
        String input = null;
        T converted = null;
        boolean validated = false;
        while (! validated) {
            try {
                while (!consoleDelegate.ready()) {
                    if (count == timerValue) {
                        String promptMessage = promptString + "- ";
                        System.out.print(promptMessage);
                    }
                    count--;
                    if (count < 0) {
                        throw new InterruptedException();
                    }
                    Thread.sleep(1000);
                }
                try {
                    input = consoleDelegate.readLine().trim();
                    converted = ObjectUtil.convert(input, type);
                    if (validator != null) {
                        validated = validator.validateInput(input, converted);
                    }
                    else {
                        validated = true;
                    }
                } catch (IllegalArgumentException iae) {
                    Scope.getCurrentScope().getUI().sendMessage(iae.getMessage());
                }
            } catch (IOException ioe) {
                throw new LiquibaseException(ioe);
            } catch (InterruptedException ie) {
                //
                // If we were interrupted and the timer had not rundown then complain and continue
                //
                if (count >= 0) {
                    Scope.getCurrentScope().getLog(getClass()).warning("Error while waiting for input: " + ie.getMessage());
                }
                validated = true;
            }
        }

        //
        // Return the default
        //
        if (input == null || input.isEmpty()) {
            System.out.println();
            Scope.getCurrentScope().getUI().sendMessage("Using default value of '" + defaultValue + "'");
            return defaultValue;
        }
        return converted;
    }

    protected ConsoleDelegate getConsoleDelegate() throws LiquibaseException {
        return new ConsoleDelegate();
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
}
