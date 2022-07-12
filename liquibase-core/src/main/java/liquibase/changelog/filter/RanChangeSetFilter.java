package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;

import java.util.List;

public abstract class RanChangeSetFilter implements ChangeSetFilter {
    public List<RanChangeSet> ranChangeSets;

    public RanChangeSetFilter(List<RanChangeSet> ranChangeSets) {
        this.ranChangeSets = ranChangeSets;
    }

    public RanChangeSet getRanChangeSet(ChangeSet changeSet) {
        for (RanChangeSet ranChangeSet : ranChangeSets) {
            if (ranChangeSet.getId().equalsIgnoreCase(changeSet.getId())
                    && ranChangeSet.getAuthor().equalsIgnoreCase(changeSet.getAuthor())
                    && DatabaseChangeLog.normalizePath(ranChangeSet.getChangeLog()).equalsIgnoreCase(DatabaseChangeLog.normalizePath(changeSet.getFilePath()))) {
                return ranChangeSet;
            }
        }
        return null;

    }
}
