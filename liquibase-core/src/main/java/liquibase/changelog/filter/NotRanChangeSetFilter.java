package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;

import java.util.List;

public class NotRanChangeSetFilter implements ChangeSetFilter {

    public List<RanChangeSet> ranChangeSets;

    public NotRanChangeSetFilter(List<RanChangeSet> ranChangeSets) {
        this.ranChangeSets = ranChangeSets;
    }

    @Override
    @SuppressWarnings({"RedundantIfStatement"})
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        for (RanChangeSet ranChangeSet : ranChangeSets) {
            if (ranChangeSet.getId().equalsIgnoreCase(changeSet.getId())
                    && ranChangeSet.getAuthor().equalsIgnoreCase(changeSet.getAuthor())
                    && ranChangeSet.getChangeLog().replaceFirst("^classpath:", "").equalsIgnoreCase(changeSet.getFilePath().replaceFirst("^classpath:", ""))) {
                return new ChangeSetFilterResult(false, "Change set already ran", this.getClass());
            }
        }
        return new ChangeSetFilterResult(true, "Change set not yet ran", this.getClass());
    }
}
