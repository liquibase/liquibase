package liquibase.changelog.filter;

/**
 * Contains the result of a {@link liquibase.changelog.filter.ChangeSetFilter#accepts(liquibase.changelog.ChangeSet)}  call.
 * <p>
 * {@link #getMessage()}, but {@link #getFilter()} can be used to programmatically determine the reason for accepting or not.
 */
public class ChangeSetFilterResult {

    private final boolean accepted;
    private final String message;
    private final Class<? extends ChangeSetFilter> filter;
    private final String mdcName;
    private final String displayName;

    public ChangeSetFilterResult(boolean accepted, String message, Class<? extends ChangeSetFilter> filter) {
        this(accepted, message, filter, filter == null ? null : filter.getSimpleName(), filter == null ? null : filter.getSimpleName());
    }

    public ChangeSetFilterResult(boolean accepted, String message, Class<? extends ChangeSetFilter> filter, String mdcName, String displayName) {
        this.accepted = accepted;
        this.message = message;
        this.filter = filter;
        this.mdcName = mdcName;
        this.displayName = displayName;
    }

    /**
     * Was the changeSet accepted by the filter
     */
    public boolean isAccepted() {
        return accepted;
    }

    /**
     * Free-form text message from the filter giving the reason for accepting or rejecting. May be null.
     */
    public String getMessage() {
        return message;
    }

    /**
     * Returns the class of the filter that accepted or rejected this changeSet.
     */
    public Class<? extends ChangeSetFilter> getFilter() {
        return filter;
    }

    public String getMdcName() {
        return mdcName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        if (accepted) {
            return "Will run because "+message;
        } else {
            return "Will NOT run because "+message;
        }
    }
}
