package liquibase.changelog;

import liquibase.change.CheckSum;
import liquibase.changelog.filter.ChangeSetFilter;
import liquibase.changelog.filter.ChangeSetFilterResult;

import java.util.Date;
import java.util.Set;

/**
 * Contains the current status of a ChangeSet. Normally returned by {@link liquibase.changelog.visitor.StatusVisitor}.
 * Contains information on whether the changeSet has ran before and will run next time.
 */
public class ChangeSetStatus {

    private final ChangeSet changeSet;
    private final CheckSum currentCheckSum;
    private String description;
    private String comments;

    private boolean willRun;
    private ChangeSetFilterResult skipReason;
    private Set<ChangeSetFilterResult> runReasons;

    private CheckSum storedCheckSum;
    private Date dateLastExecuted;
    private boolean previouslyRan = false;

    public ChangeSetStatus(ChangeSet changeSet) {
        this.changeSet = changeSet;

        this.currentCheckSum = changeSet.generateCheckSum();
        this.description = changeSet.getDescription();
        this.comments = changeSet.getComments();
    }

    public ChangeSet getChangeSet() {
        return changeSet;
    }

    public CheckSum getCurrentCheckSum() {
        return currentCheckSum;
    }

    /**
     * ChangeSet description
     */
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * ChangeSet comments
     */
    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    /**
     * Will the change set run next time.
     */
    public boolean getWillRun() {
        return willRun;
    }

    public void setWillRun(boolean willRun) {
        this.willRun = willRun;
    }

    /**
     * Reason the change set will not run next time. Returns null if it will run next time.
     */
    public ChangeSetFilterResult getSkipReason() {
        return skipReason;
    }

    public void setSkipReason(ChangeSetFilterResult skipReason) {
        this.skipReason = skipReason;
    }

    /**
     * Reason(s) the change set will run next time. Returns null if it will not run next time.
     */
    public Set<ChangeSetFilterResult> getRunReasons() {
        return runReasons;
    }

    public void setRunReasons(Set<ChangeSetFilterResult> runReasons) {
        this.runReasons = runReasons;
    }

    /**
     * Convenience method to check wither a given ChangeSetFilter type is a reason for running the change set.
     */
    public boolean isRunReason(Class<? extends ChangeSetFilter> filterType) {
        if (!willRun) {
            return false;
        }

        if (runReasons == null) {
            return false;
        }

        for (ChangeSetFilterResult result : runReasons) {
            if (result.getFilter().equals(filterType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the checksum stored from the last execution of the change set. Returns null if it has not ran before
     */
    public CheckSum getStoredCheckSum() {
        return storedCheckSum;
    }

    public void setStoredCheckSum(CheckSum storedCheckSum) {
        this.storedCheckSum = storedCheckSum;
    }

    /**
     * Return the date the change set was last executed. Returns null if it has not ran before
     */
    public Date getDateLastExecuted() {
        return dateLastExecuted;
    }

    public void setDateLastExecuted(Date dateLastExecuted) {
        this.dateLastExecuted = dateLastExecuted;
    }

    /**
     * Returns true if the change set was ran previously.
     */
    public boolean getPreviouslyRan() {
        return previouslyRan;
    }

    public void setPreviouslyRan(boolean previouslyRan) {
        this.previouslyRan = previouslyRan;
    }
}
