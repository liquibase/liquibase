package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.exception.RollbackFailedException;
import liquibase.util.StringUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AfterTagChangeSetFilter implements ChangeSetFilter {

    private final String tag;
    private final Set<String> changeLogsAfterTag = new HashSet<>();

    public AfterTagChangeSetFilter(String tag, List<RanChangeSet> ranChangeSets) throws RollbackFailedException {
        this.tag = tag;
        boolean seenTag = false;
        for (RanChangeSet ranChangeSet : ranChangeSets) {
            if (seenTag && !tag.equalsIgnoreCase(ranChangeSet.getTag())) {
                changeLogsAfterTag.add(ranChangeSet.toString());
            }

            if (!seenTag && tag.equalsIgnoreCase(ranChangeSet.getTag())) {
                seenTag = true;
                if ("tagDatabase".equals(StringUtil.trimToEmpty(ranChangeSet.getDescription()))) { //changeSet is just tagging the database. Also remove it.
                    changeLogsAfterTag.add(ranChangeSet.toString());
                }
            }
        }

        if (!seenTag) {
            throw new RollbackFailedException("Could not find tag '"+tag+"' in the database");
        }
    }

    @Override
    public ChangeSetFilterResult accepts(ChangeSet changeSet) {
        if (changeLogsAfterTag.contains(changeSet.toString())) {
            return new ChangeSetFilterResult(true, "Changeset is before tag '"+tag+"'", this.getClass(), getMdcName(), getDisplayName());
        } else {
            return new ChangeSetFilterResult(false, "Changeset after tag '"+tag+"'", this.getClass(), getMdcName(), getDisplayName());
        }
    }
}
