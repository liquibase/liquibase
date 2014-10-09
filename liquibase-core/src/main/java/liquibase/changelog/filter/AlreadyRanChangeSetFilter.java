package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;

import java.util.List;

public class AlreadyRanChangeSetFilter extends RanChangeSetFilter {

    public AlreadyRanChangeSetFilter(List<RanChangeSet> ranChangeSets, boolean ignoreClasspathPrefix) {
        super(ranChangeSets, ignoreClasspathPrefix);
    }

    @Override
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        if (getRanChangeSet(changeSet) != null) {
            return new ChangeSetFilterResult(true, "Change set already ran", this.getClass());
        } else {
            return new ChangeSetFilterResult(false, "Change set has not ran", this.getClass());
        }
    }

}
