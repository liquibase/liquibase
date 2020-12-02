package liquibase.ui;

import liquibase.AbstractExtensibleObject;
import liquibase.Scope;

import java.util.logging.Level;

/**
 * Sends all UI requests to the configured Logger. The level they are logged at defaults to {@link Level#INFO} for standard messages and {@link Level#SEVERE} for error messages.
 */
public class LoggerUIService extends AbstractExtensibleObject implements UIService {

    private Level standardLogLevel = Level.INFO;
    private Level errorLogLevel = Level.SEVERE;

    @Override
    public int getPriority() {
        return PRIORITY_DEFAULT;
    }

    @Override
    public void sendMessage(String message) {
        Scope.getCurrentScope().getLog(getClass()).log(standardLogLevel, message, null);
    }

    @Override
    public void sendErrorMessage(String message) {
        sendErrorMessage(message, null);
    }

    @Override
    public void sendErrorMessage(String message, Throwable exception) {
        Scope.getCurrentScope().getLog(getClass()).log(errorLogLevel, message, exception);

    }

    /**
     *
     * Prompt the user with the message and wait with a running time
     * with a running time.  Return the response as a String
     *
     * @param  promptString     String to display as a prompt
     * @param  defaultValue     String to return as a default
     * @param  timerValue       Value to use as a countdown timer
     *                          Must be a valid integer > 0
     * @param  type             return type class
     * @return                  Instance of return type
     *
     * NOT IMPLEMENTED
     *
     */
    @Override
    public <T> T prompt(String promptString, T defaultValue , int timerValue, Class<T> type) throws IllegalArgumentException {
        return null;
    }

    public Level getStandardLogLevel() {
        return standardLogLevel;
    }

    public void setStandardLogLevel(Level standardLogLevel) {
        this.standardLogLevel = standardLogLevel;
    }

    public Level getErrorLogLevel() {
        return errorLogLevel;
    }

    public void setErrorLogLevel(Level errorLogLevel) {
        this.errorLogLevel = errorLogLevel;
    }
}
