package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ShouldRunChangeSetFilter implements ChangeSetFilter {

    private final Map<String, RanChangeSet> ranChangeSets;

    public ShouldRunChangeSetFilter(Database database, boolean ignoreClasspathPrefix) throws DatabaseException {
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
        for (RanChangeSet ranChangeSet : this.ranChangeSets.values()) {
            if (ranChangeSet.isSameAs(changeSet)) {
                if (changeSet.shouldAlwaysRun()) {
                    return new ChangeSetFilterResult(true, "Changeset always runs", this.getClass());
                }
                if (changeSet.shouldRunOnChange() && checksumChanged(changeSet, ranChangeSet)) {
                    return new ChangeSetFilterResult(true, "Changeset checksum changed", this.getClass());
                }
                return new ChangeSetFilterResult(false, "Changeset already ran", this.getClass());
            }
        }
        return new ChangeSetFilterResult(true, "Changeset has not ran yet", this.getClass());
    }


    protected boolean checksumChanged(ChangeSet changeSet, RanChangeSet ranChangeSet) {
        return !changeSet.generateCheckSum().equals(ranChangeSet.getLastCheckSum());
    }
}
