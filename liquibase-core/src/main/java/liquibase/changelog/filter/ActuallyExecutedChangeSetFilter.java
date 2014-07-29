package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;

import java.util.List;

public class ActuallyExecutedChangeSetFilter extends RanChangeSetFilter {

    public ActuallyExecutedChangeSetFilter(List<RanChangeSet> ranChangeSets, boolean ignoreClasspathPrefix) {
        super(ranChangeSets, ignoreClasspathPrefix);
    }

    @Override
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        RanChangeSet ranChangeSet = getRanChangeSet(changeSet);
        if (ranChangeSet != null && (ranChangeSet.getExecType() == null || ranChangeSet.getExecType().equals(ChangeSet.ExecType.EXECUTED) || ranChangeSet.getExecType().equals(ChangeSet.ExecType.RERAN))) {
            return new ChangeSetFilterResult(true, "Change set was executed previously", this.getClass());
        } else {
            return new ChangeSetFilterResult(false, "Change set was not previously executed", this.getClass());
        }
    }
}
