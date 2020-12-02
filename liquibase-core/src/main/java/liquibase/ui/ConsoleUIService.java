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
    public <T> T prompt(String promptString, T defaultValue, int timerValue, Class<T> type) throws LiquibaseException {
        if (timerValue <= 0) {
            throw new IllegalArgumentException("Value for countdown timer must be greater than 0");
        }
        ConsoleDelegate consoleDelegate = getConsoleDelegate();
        int count = timerValue;
        String input = null;
        try {
            while (! consoleDelegate.ready()) {
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
            } catch (Exception e) {
                throw new LiquibaseException(e);
            }
        }
        catch (IOException ioe) {
            throw new LiquibaseException(ioe);
        }
        catch (InterruptedException ie) {
          if (count >= 0) {
              Scope.getCurrentScope().getLog(getClass()).warning("Error while waiting for input: " + ie.getMessage());
          }
        }

        //
        // Return the default
        //
        if (input == null || input.isEmpty()) {
            return defaultValue;
        }
        return ObjectUtil.convert(input, type);
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
