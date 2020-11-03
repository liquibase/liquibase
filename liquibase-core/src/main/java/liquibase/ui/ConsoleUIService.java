package liquibase.ui;

import liquibase.AbstractExtensibleObject;

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
