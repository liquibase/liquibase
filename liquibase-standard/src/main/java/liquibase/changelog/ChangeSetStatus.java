package liquibase.changelog;

import liquibase.ChecksumVersion;
import liquibase.change.CheckSum;
import liquibase.changelog.filter.ChangeSetFilter;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.exception.LiquibaseException;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;

import java.util.Date;
import java.util.Set;

/**
 * Contains the current status of a ChangeSet. Normally returned by {@link liquibase.changelog.visitor.StatusVisitor}.
 * Contains information on whether the changeSet has run before and will run next time.
 */
public class ChangeSetStatus {

    private final ChangeSet changeSet;
    private final CheckSum currentCheckSum;
    private String description;
    private String comments;

    private boolean willRun;
    private Set<ChangeSetFilterResult> filterResults;

    private CheckSum storedCheckSum;
    private Date dateLastExecuted;
    private boolean previouslyRan;

    private RanChangeSet ranChangeSet;

    public ChangeSetStatus(ChangeSet changeSet) {
        this.changeSet = changeSet;
        ChecksumVersion version = changeSet.getStoredCheckSum() != null ?
                ChecksumVersion.enumFromChecksumVersion(changeSet.getStoredCheckSum().getVersion()) :
                ChecksumVersion.latest();
        this.currentCheckSum = changeSet.generateCheckSum(version);
        this.description = changeSet.getDescription();
        this.comments = changeSet.getComments();
    }

    public ChangeSetStatus(ChangeSet changeSet, boolean skipChangeSetStatusGeneration) throws LiquibaseException {
        if(skipChangeSetStatusGeneration) {
            this.changeSet = changeSet;
            this.currentCheckSum = null;
            this.description = null;
            this.comments = null;
        }
        else {
            throw new LiquibaseException(String.format("ChangeSetStatus for ChangeSet %s cannot generated", changeSet.toString()));
        }
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
     * Will the changeset run next time.
     */
    public boolean getWillRun() {
        return willRun;
    }

    public void setWillRun(boolean willRun) {
        this.willRun = willRun;
    }

    /**
     * Reasons the changeset will or will not run next time. Returns empty set if no reasons were given
     */
    public Set<ChangeSetFilterResult> getFilterResults() {
        return filterResults;
    }

    public void setFilterResults(Set<ChangeSetFilterResult> filterResults) {
        this.filterResults = filterResults;
    }

    /**
     * Convenience method to check wither a given ChangeSetFilter type is a reason for running the changeset or not.
     */
    public boolean isFilteredBy(Class<? extends ChangeSetFilter> filterType) {
        if (!willRun) {
            return false;
        }

        if (filterResults == null) {
            return false;
        }

        for (ChangeSetFilterResult result : filterResults) {
            if (result.getFilter().equals(filterType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the checksum stored from the last execution of the changeset. Returns null if it has not run before
     */
    public CheckSum getStoredCheckSum() {
        return storedCheckSum;
    }

    public void setStoredCheckSum(CheckSum storedCheckSum) {
        this.storedCheckSum = storedCheckSum;
    }

    /**
     * Return the date the changeset was last executed. Returns null if it has not run before
     */
    public Date getDateLastExecuted() {
        return dateLastExecuted;
    }

    public void setDateLastExecuted(Date dateLastExecuted) {
        this.dateLastExecuted = dateLastExecuted;
    }

    /**
     * Returns true if the changeset was run previously.
     */
    public boolean getPreviouslyRan() {
        return previouslyRan;
    }

    public void setPreviouslyRan(boolean previouslyRan) {
        this.previouslyRan = previouslyRan;
    }

    public RanChangeSet getRanChangeSet() {
        return ranChangeSet;
    }

    public void setRanChangeSet(RanChangeSet ranChangeSet) {
        this.ranChangeSet = ranChangeSet;
    }
}
