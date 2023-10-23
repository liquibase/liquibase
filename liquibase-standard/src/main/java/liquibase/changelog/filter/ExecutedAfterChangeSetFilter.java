package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.util.ISODateFormat;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExecutedAfterChangeSetFilter implements ChangeSetFilter {

    private final Date date;
    private final Set<String> changeLogsAfterDate = new HashSet<>();

    public ExecutedAfterChangeSetFilter(Date date, List<RanChangeSet> ranChangeSets) {
        this.date = date;
        for (RanChangeSet ranChangeSet : ranChangeSets) {
            if ((ranChangeSet.getDateExecuted() != null) && (ranChangeSet.getDateExecuted().getTime() > date.getTime())) {
                changeLogsAfterDate.add(ranChangeSet.toString());
            }
        }
    }

    @Override
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        if (changeLogsAfterDate.contains(changeSet.toString())) {
            return new ChangeSetFilterResult(true, "Changeset ran after "+ new ISODateFormat().format(new java.sql.Timestamp(date.getTime())), this.getClass(), getMdcName(), getDisplayName());
        } else {
            return new ChangeSetFilterResult(false, "Changeset ran before "+ new ISODateFormat().format(new java.sql.Timestamp(date.getTime())), this.getClass(), getMdcName(), getDisplayName());
        }
    }
}
