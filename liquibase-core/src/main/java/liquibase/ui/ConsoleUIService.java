package liquibase.ui;

import liquibase.AbstractExtensibleObject;
import liquibase.exception.LiquibaseException;

import java.io.Console;
import java.io.PrintStream;

import static java.lang.Thread.sleep;

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

    /**
     *
     * Prompt the user with the message and wait with a running time
     * with a running time.  Return the response as a String
     *
     * @param message          String to display as a prompt
     * @param timerValue       Value to use as a countdown timer
     *                         Must be a valid integer > 0
     *
     */
    @Override
    public String prompt(String message, int timerValue, ConsoleDelegate consoleDelegate) throws LiquibaseException {
        if (timerValue <= 0) {
            throw new IllegalArgumentException("Value for countdown timer must be greater than 0");
        }
        if (consoleDelegate == null) {
            throw new IllegalArgumentException("You must supply a ConsoleDelegate instance");
        }
        String input = null;
        CountdownTimer countdownTimer = new CountdownTimer(message, timerValue);
        try {
            new Thread(countdownTimer).start();
        }
        catch (Exception e) {
            // Consume
        }
        input = consoleDelegate.readLine().trim();
        countdownTimer.stop();
        return input;
    }

    private static class CountdownTimer implements Runnable {
        private final int timerValue;
        private final String message;
        private boolean stop = false;

        public CountdownTimer(String message, int timerValue) {
            this.timerValue = timerValue;
            this.message = message;
        }
        public void run() {
            for (int i=timerValue; i > 0; i--) {
                if (stop) {
                    return;
                }
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    // consume
                }
                if (stop) {
                    return;
                }
                String promptMessage = "\r" + message + " *" + Integer.toString(i) + "*- ";
                System.out.print(promptMessage);
            }
            if (timerValue > 0) {
                System.out.println();
            }
        }
        public void stop() {
            this.stop = true;
        }
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
