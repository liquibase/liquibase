package liquibase.changelog.filter;

import java.util.Objects;

/**
 * Contains the result of a {@link liquibase.changelog.filter.ChangeSetFilter#accepts(liquibase.changelog.ChangeSet)}  call.
 *
 * {@link #getMessage()}, but {@link #getFilter()} can be used to programatically determine the reason for accepting or not.
 */
public class ChangeSetFilterResult {

    private boolean accepted;
    private String message;
    private final Class<? extends ChangeSetFilter> filter;

    public ChangeSetFilterResult(boolean accepted, String message, Class<? extends ChangeSetFilter> filter) {
        this.accepted = accepted;
        this.message = message;
        this.filter = filter;
    }

    /**
     * Was the change set accepted by the filter
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
     * Returns the class of the filter that accepted or rejected this change set.
     */
    public Class<? extends ChangeSetFilter> getFilter() {
        return filter;
    }

    @Override
    public String toString() {
        if (accepted) {
            return "Will run because "+message;
        } else {
            return "Will NOT run because "+message;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChangeSetFilterResult that = (ChangeSetFilterResult) o;
        return accepted == that.accepted
                && Objects.equals(message, that.message)
                && Objects.equals(filter, that.filter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accepted, message, filter);
    }
}
