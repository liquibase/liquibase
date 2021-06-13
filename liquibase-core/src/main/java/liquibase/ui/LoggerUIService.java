package liquibase.ui;

import liquibase.AbstractExtensibleObject;
import liquibase.Scope;
import liquibase.exception.LiquibaseException;

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

    @Override
    public void setAllowPrompt(boolean allowPrompt) throws IllegalArgumentException {
        if (allowPrompt) {
            throw new IllegalArgumentException("allowPrompt=true not allowed in LoggerUIService");
        }
    }

    @Override
    public boolean getAllowPrompt() {
        return false;
    }

    /**
     *
     * This implementation simply returns the default value, since it cannot prompt the user.
     */
    @Override
    public <T> T prompt(String prompt, T defaultValue, InputHandler<T> inputHandler, Class<T> type) {
        return defaultValue;
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
