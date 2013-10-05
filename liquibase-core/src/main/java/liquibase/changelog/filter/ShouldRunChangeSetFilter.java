package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;

import java.util.List;

public class ShouldRunChangeSetFilter implements ChangeSetFilter {

    private final List<RanChangeSet> ranChangeSets;
    private final Database database;
    private final boolean ignoringClasspathPrefix;

    public ShouldRunChangeSetFilter(Database database,
                                    boolean ignoringClasspathPrefix) throws DatabaseException {
        this.database = database;
        this.ignoringClasspathPrefix = ignoringClasspathPrefix;
        this.ranChangeSets = database.getRanChangeSetList();
    }

    public ShouldRunChangeSetFilter(Database database) throws DatabaseException {
        this(database, false);
    }

    @SuppressWarnings({"RedundantIfStatement"})
    public boolean accepts(ChangeSet changeSet) {
        for (RanChangeSet ranChangeSet : ranChangeSets) {
            if (currentAndRanChangesetsMatch(changeSet, ranChangeSet)) {
                if (changeSet.shouldAlwaysRun()) {
                    return true;
                }
                if (changeSet.shouldRunOnChange() &&
                    currentChecksumHasChanged(changeSet, ranChangeSet)) {
                    return true;
                }
                return false;
            }
        }
        return true;
    }

    private boolean currentAndRanChangesetsMatch(ChangeSet changeSet, RanChangeSet ranChangeSet) {
        return ranChangeSet.getId().equals(changeSet.getId())
            && ranChangeSet.getAuthor().equals(changeSet.getAuthor())
            && isPathEquals(changeSet, ranChangeSet);
    }

    private boolean currentChecksumHasChanged(ChangeSet changeSet, RanChangeSet ranChangeSet) {
        return !changeSet.generateCheckSum().equals(ranChangeSet.getLastCheckSum());
    }

    private boolean isPathEquals(ChangeSet changeSet, RanChangeSet ranChangeSet) {
        return getChangeLog(ranChangeSet).equalsIgnoreCase(getFilePath(changeSet));
    }

    private String getChangeLog(RanChangeSet ranChangeSet) {
        return stripClasspathPrefix(ranChangeSet.getChangeLog());
    }

    private String getFilePath(ChangeSet changeSet) {
        return stripClasspathPrefix(changeSet.getFilePath());
    }

    private String stripClasspathPrefix(String filePath) {
        if (ignoringClasspathPrefix) {
            return filePath.replace("classpath:", "");
        }
        return filePath;
    }
}
