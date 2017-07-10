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
    private Set<String> changeLogsAfterDate = new HashSet<>();

    public ExecutedAfterChangeSetFilter(Date date, List<RanChangeSet> ranChangeSets) {
        this.date = date;
        for (RanChangeSet ranChangeSet : ranChangeSets) {
            if ((ranChangeSet.getDateExecuted() != null) && (ranChangeSet.getDateExecuted().getTime() > date.getTime())) {
                changeLogsAfterDate.add(changeLogToString(ranChangeSet.getId(), ranChangeSet.getAuthor(), ranChangeSet.getChangeLog()));
            }
        }
    }

    private String changeLogToString(String id, String author, String changeLog) {
        return id+":"+author+":"+changeLog;
    }

    @Override
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        if (changeLogsAfterDate.contains(changeLogToString(changeSet.getId(), changeSet.getAuthor(), changeSet.getFilePath()))) {
            return new ChangeSetFilterResult(true, "Change set ran after "+ new ISODateFormat().format(new java.sql.Timestamp(date.getTime())), this.getClass());
        } else {
            return new ChangeSetFilterResult(false, "Change set ran before "+ new ISODateFormat().format(new java.sql.Timestamp(date.getTime())), this.getClass());
        }
    }
}