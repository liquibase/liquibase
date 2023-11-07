package liquibase.changelog.filter;

import liquibase.ChecksumVersion;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ShouldRunChangeSetFilter implements ChangeSetFilter {

    private final Map<String, RanChangeSet> ranChangeSets;
    private final boolean ignoreClasspathPrefix;

    public ShouldRunChangeSetFilter(Database database, boolean ignoreClasspathPrefix) throws DatabaseException {
        this.ignoreClasspathPrefix = ignoreClasspathPrefix;
        this.ranChangeSets = new HashMap<>();

        //ensure we have only the latest version of each ranChangeset in case multiple versions ended up in the databasechangelog table
        for (RanChangeSet ranChangeSet : database.getRanChangeSetList()) {
            RanChangeSet existingChangeSet = ranChangeSets.get(ranChangeSet.toString());
            boolean addToSet = false;
            if (existingChangeSet == null) {
                addToSet = true;
            } else {
                Date existingDate = existingChangeSet.getDateExecuted();
                Date thisDate = ranChangeSet.getDateExecuted();
                if ((existingDate != null) && (thisDate != null)) {
                    int comparedDates = thisDate.compareTo(existingDate);
                    if (comparedDates > 0) {
                        addToSet = true;
                    } else if (comparedDates == 0) {
                        Integer existingOrder = existingChangeSet.getOrderExecuted();
                        Integer thisOrder = ranChangeSet.getOrderExecuted();

                        if ((existingOrder != null) && (thisOrder != null) && (thisOrder.compareTo(existingOrder) < 0)) {
                            addToSet = true;
                        }
                    }
                }
            }
            if (addToSet) {
                this.ranChangeSets.put(ranChangeSet.toString(), ranChangeSet);
            }
        }
    }

    public ShouldRunChangeSetFilter(Database database) throws DatabaseException {
        this(database, true);
    }

    @Override
    @SuppressWarnings({"RedundantIfStatement"})
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        String key = changeSet.toNormalizedString();
        RanChangeSet ranChangeSet = this.ranChangeSets.get(key);
        if (ranChangeSet != null && ranChangeSet.isSameAs(changeSet)) {
            if (changeSet.shouldAlwaysRun()) {
                return new ChangeSetFilterResult(true, "Changeset always runs", this.getClass(), getMdcName(), getDisplayName());
            }
            if (changeSet.shouldRunOnChange() && checksumChanged(changeSet, ranChangeSet)) {
                return new ChangeSetFilterResult(true, "Changeset checksum changed", this.getClass(), getMdcName(), getDisplayName());
            }
            return new ChangeSetFilterResult(false, "Changeset already ran", this.getClass(), getMdcName(), getDisplayName());
        }
        return new ChangeSetFilterResult(true, "Changeset has not ran yet", this.getClass(), getMdcName(), getDisplayName());
    }


    protected boolean checksumChanged(ChangeSet changeSet, RanChangeSet ranChangeSet) {
        if (ranChangeSet.getLastCheckSum() == null) {
            // we don't have a last checksum to compare to, so we are not able to compare both. This may be the case of a
            // new changeset or after a clearChecksums execution, so we return false as we are unable to test it
            return false;
        }
        // generate the checksum for the current changeset loaded from changelog files using the same checksum version
        // that we have at database so we are able to compare them
        return !changeSet.generateCheckSum(ChecksumVersion.enumFromChecksumVersion(ranChangeSet.getLastCheckSum().getVersion()))
                .equals(ranChangeSet.getLastCheckSum());
    }

    @Override
    public String getMdcName() {
        return "alreadyRan";
    }

    @Override
    public String getDisplayName() {
        return "Already ran";
    }
}
