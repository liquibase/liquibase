package liquibase.changelog;

import liquibase.ChecksumVersion;
import liquibase.change.CheckSum;
import liquibase.changelog.filter.ChangeSetFilter;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.exception.LiquibaseException;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

/**
 * Contains the current status of a ChangeSet. Normally returned by {@link liquibase.changelog.visitor.StatusVisitor}.
 * Contains information on whether the changeSet has run before and will run next time.
 */
public class ChangeSetStatus {

    @Getter
    private final ChangeSet changeSet;
    @Getter
    private final CheckSum currentCheckSum;
    /**
     * -- GETTER --
     *  ChangeSet description
     */
    @Setter
    @Getter
    private String description;
    /**
     * -- GETTER --
     *  ChangeSet comments
     */
    @Setter
    @Getter
    private String comments;

    @Setter
    private boolean willRun;
    /**
     * -- GETTER --
     *  Reasons the changeset will or will not run next time. Returns empty set if no reasons were given
     */
    @Setter
    @Getter
    private Set<ChangeSetFilterResult> filterResults;

    /**
     * -- GETTER --
     *  Return the checksum stored from the last execution of the changeset. Returns null if it has not run before
     */
    @Setter
    @Getter
    private CheckSum storedCheckSum;
    /**
     * -- GETTER --
     *  Return the date the changeset was last executed. Returns null if it has not run before
     */
    @Setter
    @Getter
    private Date dateLastExecuted;
    @Setter
    private boolean previouslyRan;

    @Setter
    @Getter
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

    /**
     * Will the changeset run next time.
     */
    public boolean getWillRun() {
        return willRun;
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
     * Returns true if the changeset was run previously.
     */
    public boolean getPreviouslyRan() {
        return previouslyRan;
    }

}
