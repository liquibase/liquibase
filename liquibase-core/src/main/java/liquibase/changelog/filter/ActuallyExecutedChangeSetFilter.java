package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;

import java.util.List;

public class ActuallyExecutedChangeSetFilter extends RanChangeSetFilter {

    public ActuallyExecutedChangeSetFilter(List<RanChangeSet> ranChangeSets) {
        super(ranChangeSets);
    }

    @Override
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        RanChangeSet ranChangeSet = getRanChangeSet(changeSet);
        if ((ranChangeSet != null) && ((ranChangeSet.getExecType() == null) || ranChangeSet.getExecType().equals
            (ChangeSet.ExecType.EXECUTED) || ranChangeSet.getExecType().equals(ChangeSet.ExecType.RERAN))) {
            return new ChangeSetFilterResult(true, "Changeset was executed previously", this.getClass(), getMdcName(), getDisplayName());
        } else {
            return new ChangeSetFilterResult(false, "Changeset was not previously executed", this.getClass(), getMdcName(), getDisplayName());
        }
    }
}
