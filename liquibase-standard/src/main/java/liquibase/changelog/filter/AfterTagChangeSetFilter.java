package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.exception.RollbackFailedException;
import liquibase.util.StringUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AfterTagChangeSetFilter implements ChangeSetFilter {

    private final String tag;
    private final Set<String> changeLogsAfterTag = new HashSet<>();

    public AfterTagChangeSetFilter(String tag, List<RanChangeSet> ranChangeSets) throws RollbackFailedException {
        this.tag = tag;
        boolean seenTag = ranChangeSets.stream().anyMatch(ranChangeSet ->  {
            return tag.equalsIgnoreCase(ranChangeSet.getTag());
        });
        if (! seenTag) {
            throw new RollbackFailedException("Could not find tag '"+tag+"' in the database");
        }
        List<RanChangeSet> reversedRanChangeSets = ranChangeSets.stream().collect(
            Collectors.collectingAndThen(
                Collectors.toList(),
                l -> {
                    Collections.reverse(l); return l; }
            ));
        for (RanChangeSet ranChangeSet : reversedRanChangeSets) {
            if (tag.equalsIgnoreCase(ranChangeSet.getTag())) {
                if ("tagDatabase".equals(StringUtil.trimToEmpty(ranChangeSet.getDescription()))) {
                    changeLogsAfterTag.add(ranChangeSet.toString());
                }
                break;
            }
            changeLogsAfterTag.add(ranChangeSet.toString());
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
