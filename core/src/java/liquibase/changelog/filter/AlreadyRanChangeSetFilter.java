package liquibase.changelog.filter;

import liquibase.ChangeSet;
import liquibase.RanChangeSet;

import java.util.List;

public class AlreadyRanChangeSetFilter implements ChangeSetFilter {

    public List<RanChangeSet> ranChangeSets;

    public AlreadyRanChangeSetFilter(List<RanChangeSet> ranChangeSets) {
        this.ranChangeSets = ranChangeSets;
    }

    @SuppressWarnings({"RedundantIfStatement"})
    public boolean accepts(ChangeSet changeSet) {
        for (RanChangeSet ranChangeSet : ranChangeSets) {
            if (ranChangeSet.getId().equals(changeSet.getId())
                    && ranChangeSet.getAuthor().equals(changeSet.getAuthor())
                    && ranChangeSet.getChangeLog().equals(changeSet.getFilePath())) {
                return true;
            }
        }
        return false;
    }
}
