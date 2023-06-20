package liquibase.changelog.filter;

import liquibase.changelog.ChangeSet;
import liquibase.changelog.RanChangeSet;
import liquibase.command.core.RollbackCommandStep;
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

    public AfterTagChangeSetFilter(String tag, List<RanChangeSet> ranChangeSets, RollbackCommandStep.TAG_VERSION tagVersion)
            throws RollbackFailedException {
        this.tag = tag;
        if (tagVersion == RollbackCommandStep.TAG_VERSION.OLDEST) {
            oldestVersion(ranChangeSets);
            return;
        }

        //
        // Check to see if the tag exists
        //
        boolean seenTag = ranChangeSets.stream().anyMatch(ranChangeSet ->  {
            return tag.equalsIgnoreCase(ranChangeSet.getTag());
        });
        if (! seenTag) {
            throw new RollbackFailedException("Could not find tag '"+tag+"' in the database");
        }

        //
        // Reverse the ranChangeSets
        //
        List<RanChangeSet> reversedRanChangeSets = ranChangeSets.stream().collect(
            Collectors.collectingAndThen(
                Collectors.toList(),
                l -> {
                    Collections.reverse(l); return l; }
            ));
        //
        // Search from newest to oldest
        //
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

    private void oldestVersion(List<RanChangeSet> ranChangeSets) throws RollbackFailedException {
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
            throw new RollbackFailedException("Could not find tag '" + tag + "' in the database");
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
