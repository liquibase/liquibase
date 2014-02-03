package liquibase.changelog;

import liquibase.change.CheckSum;
import liquibase.changelog.filter.ChangeSetFilterResult;

import java.util.Date;
import java.util.Set;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public boolean getWillRun() {
        return willRun;
    }

    public void setWillRun(boolean willRun) {
        this.willRun = willRun;
    }

    public ChangeSetFilterResult getSkipReason() {
        return skipReason;
    }

    public void setSkipReason(ChangeSetFilterResult skipReason) {
        this.skipReason = skipReason;
    }

    public Set<ChangeSetFilterResult> getRunReasons() {
        return runReasons;
    }

    public void setRunReasons(Set<ChangeSetFilterResult> runReasons) {
        this.runReasons = runReasons;
    }

    public CheckSum getStoredCheckSum() {
        return storedCheckSum;
    }

    public void setStoredCheckSum(CheckSum storedCheckSum) {
        this.storedCheckSum = storedCheckSum;
    }

    public Date getDateLastExecuted() {
        return dateLastExecuted;
    }

    public void setDateLastExecuted(Date dateLastExecuted) {
        this.dateLastExecuted = dateLastExecuted;
    }

    public boolean getPreviouslyRan() {
        return previouslyRan;
    }

    public void setPreviouslyRan(boolean previouslyRan) {
        this.previouslyRan = previouslyRan;
    }
}
