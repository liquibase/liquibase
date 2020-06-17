package liquibase.changelog.filter;

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
        for (RanChangeSet ranChangeSet : this.ranChangeSets.values()) {
            if (changeSetsMatch(changeSet, ranChangeSet)) {
                if (changeSet.shouldAlwaysRun()) {
                    return new ChangeSetFilterResult(true, "Change set always runs", this.getClass());
                }
                if (changeSet.shouldRunOnChange() && checksumChanged(changeSet, ranChangeSet)) {
                    return new ChangeSetFilterResult(true, "Change set checksum changed", this.getClass());
                }
                return new ChangeSetFilterResult(false, "Change set already ran", this.getClass());
            }
        }
        return new ChangeSetFilterResult(true, "Change set has not ran yet", this.getClass());
    }

    protected boolean changeSetsMatch(ChangeSet changeSet, RanChangeSet ranChangeSet) {
        return idsAreEqual(changeSet, ranChangeSet)
                && authorsAreEqual(changeSet, ranChangeSet)
                && pathsAreEqual(changeSet, ranChangeSet);
    }

    protected boolean idsAreEqual(ChangeSet changeSet, RanChangeSet ranChangeSet) {
        return ranChangeSet.getId().equals(changeSet.getId());
    }

    protected boolean authorsAreEqual(ChangeSet changeSet, RanChangeSet ranChangeSet) {
        return ranChangeSet.getAuthor().equals(changeSet.getAuthor());
    }

    private boolean pathsAreEqual(ChangeSet changeSet, RanChangeSet ranChangeSet) {
        String ranChangeSetPath = getPath(ranChangeSet);
        String changeSetPath = getPath(changeSet);
        if (ranChangeSetPath == null) {
            return changeSetPath == null;
        } else {
            return ranChangeSetPath.equalsIgnoreCase(changeSetPath);
        }
    }

    protected boolean checksumChanged(ChangeSet changeSet, RanChangeSet ranChangeSet) {
        return !changeSet.generateCheckSum().equals(ranChangeSet.getLastCheckSum());
    }


    private String getPath(RanChangeSet ranChangeSet) {
        return normalizePath(ranChangeSet.getChangeLog());
    }

    private String getPath(ChangeSet changeSet) {
        return normalizePath(changeSet.getFilePath());
    }

    protected String normalizePath(String filePath) {
        if (filePath == null) {
            return null;
        }
        if (ignoreClasspathPrefix) {
            return filePath.replaceFirst("^classpath:", "");
        }
        return filePath;
    }
}
