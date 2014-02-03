package liquibase.changelog.filter;

public class ChangeSetFilterResult {

    private boolean accepted;
    private String message;
    private final Class<? extends ChangeSetFilter> filterType;

    public ChangeSetFilterResult(boolean accepted, String message, Class<? extends ChangeSetFilter> filterType) {
        this.accepted = accepted;
        this.message = message;
        this.filterType = filterType;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public String getMessage() {
        return message;
    }

    public Class<? extends ChangeSetFilter> getFilter() {
        return filterType;
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
