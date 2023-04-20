package liquibase.exception;

import liquibase.hub.model.HubChangeLog;

/**
 * Exception class indicating that a particular changelog has already been registered with Hub.
 */
public class ChangeLogAlreadyRegisteredException extends Exception {


    private static final long serialVersionUID = -8860177597375468123L;

    /**
     * If present, the changelog metadata from Hub. If null, it can be assumed that the changelog has been registered
     * with some organization which the current API key cannot access.
     */
    private final HubChangeLog hubChangeLog;

    public ChangeLogAlreadyRegisteredException() {
        this(null);
    }

    public ChangeLogAlreadyRegisteredException(HubChangeLog hubChangeLog) {
        this.hubChangeLog = hubChangeLog;
    }

    public HubChangeLog getHubChangeLog() {
        return hubChangeLog;
    }
}
