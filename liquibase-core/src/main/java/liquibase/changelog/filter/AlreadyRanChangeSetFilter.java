package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;

import java.util.List;

public class AlreadyRanChangeSetFilter extends RanChangeSetFilter {

    public AlreadyRanChangeSetFilter(List<RanChangeSet> ranChangeSets) {
        super(ranChangeSets);
    }

    @Override
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        if (getRanChangeSet(changeSet) != null) {
            return new ChangeSetFilterResult(true, "Changeset already ran", this.getClass(), getDisplayName());
        } else {
            return new ChangeSetFilterResult(false, "Changeset has not ran", this.getClass(), getDisplayName());
        }
    }

    @Override
    public String getDisplayName() {
        return "alreadyRan";
    }
}
