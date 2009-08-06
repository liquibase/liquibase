package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.exception.RollbackFailedException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AfterTagChangeSetFilter implements ChangeSetFilter {

    private Set<String> changeLogsAfterTag = new HashSet<String>();

    public AfterTagChangeSetFilter(String tag, List<RanChangeSet> ranChangeSets) throws RollbackFailedException {
        boolean seenTag = false;
        for (RanChangeSet ranChangeSet : ranChangeSets) {
            if (seenTag && !tag.equalsIgnoreCase(ranChangeSet.getTag())) {
                changeLogsAfterTag.add(changeLogToString(ranChangeSet.getId(), ranChangeSet.getAuthor(), ranChangeSet.getChangeLog()));
            }

            if (!seenTag && tag.equalsIgnoreCase(ranChangeSet.getTag())) {
                seenTag = true;
            }
        }

        if (!seenTag) {
            throw new RollbackFailedException("Could not find tag '"+tag+"' in the database");
        }
    }

    private String changeLogToString(String id, String author, String changeLog) {
        return id+":"+author+":"+changeLog;
    }

    public boolean accepts(ChangeSet changeSet) {
        return changeLogsAfterTag.contains(changeLogToString(changeSet.getId(), changeSet.getAuthor(), changeSet.getFilePath()));
    }
}
