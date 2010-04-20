package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;

import java.util.List;

public class ActuallyExecutedChangeSetFilter extends RanChangeSetFilter {

    public ActuallyExecutedChangeSetFilter(List<RanChangeSet> ranChangeSets) {
        super(ranChangeSets);
    }

    public boolean accepts(ChangeSet changeSet) {
        RanChangeSet ranChangeSet = getRanChangeSet(changeSet);
        return ranChangeSet != null && (ranChangeSet.getExecType() == null || ranChangeSet.getExecType().equals(ChangeSet.ExecType.EXECUTED) || ranChangeSet.getExecType().equals(ChangeSet.ExecType.RERAN));
    }
}
