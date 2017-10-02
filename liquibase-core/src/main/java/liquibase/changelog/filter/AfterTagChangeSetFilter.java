package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.exception.RollbackFailedException;
import liquibase.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AfterTagChangeSetFilter implements ChangeSetFilter {

    private final String tag;
    private Set<String> changeLogsAfterTag = new HashSet<>();

    public AfterTagChangeSetFilter(String tag, List<RanChangeSet> ranChangeSets) throws RollbackFailedException {
        this.tag = tag;
        boolean seenTag = false;
        for (RanChangeSet ranChangeSet : ranChangeSets) {
            if (seenTag && !tag.equalsIgnoreCase(ranChangeSet.getTag())) {
                changeLogsAfterTag.add(changeLogToString(ranChangeSet.getId(), ranChangeSet.getAuthor(), ranChangeSet.getChangeLog()));
            }

            if (!seenTag && tag.equalsIgnoreCase(ranChangeSet.getTag())) {
                seenTag = true;
                if ("tagDatabase".equals(StringUtils.trimToEmpty(ranChangeSet.getDescription()))) { //changeSet is just tagging the database. Also remove it.
                    changeLogsAfterTag.add(changeLogToString(ranChangeSet.getId(), ranChangeSet.getAuthor(), ranChangeSet.getChangeLog()));
                }
            }
        }

        if (!seenTag) {
            throw new RollbackFailedException("Could not find tag '"+tag+"' in the database");
        }
    }

    private String changeLogToString(String id, String author, String changeLog) {
        return id+":"+author+":"+changeLog;
    }

    @Override
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        if (changeLogsAfterTag.contains(changeLogToString(changeSet.getId(), changeSet.getAuthor(), changeSet.getFilePath()))) {
            return new ChangeSetFilterResult(true, "Change set is before tag '"+tag+"'", this.getClass());
        } else {
            return new ChangeSetFilterResult(false, "Change set after tag '"+tag+"'", this.getClass());
        }
    }
}
