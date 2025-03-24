package liquibase.changelog.filter;

import liquibase.Scope;
import liquibase.TagVersionEnum;
import liquibase.change.core.TagDatabaseChange;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;
import liquibase.exception.RollbackFailedException;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
import liquibase.logging.mdc.MdcKey;
import liquibase.logging.mdc.MdcValue;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class AfterTagChangeSetFilter implements ChangeSetFilter {

    private final String tag;
    private final Set<String> changeLogsAfterTag = new HashSet<>();
    private final DatabaseChangeLog databaseChangeLog;


    public AfterTagChangeSetFilter(String tag, List<RanChangeSet> ranChangeSets, TagVersionEnum tagVersion) throws RollbackFailedException {
        this(tag, ranChangeSets, tagVersion, null);
    }

    public AfterTagChangeSetFilter(String tag, List<RanChangeSet> ranChangeSets, TagVersionEnum tagVersion, DatabaseChangeLog databaseChangeLog)
            throws RollbackFailedException {
        this.tag = tag;
        this.databaseChangeLog = databaseChangeLog;
        if (tagVersion == TagVersionEnum.OLDEST) {
            oldestVersion(ranChangeSets);
            return;
        }

        //
        // Check to see if the tag exists
        //
        boolean seenTag = ranChangeSets.stream().anyMatch(ranChangeSet -> tag.equalsIgnoreCase(ranChangeSet.getTag()));
        if (! seenTag) {
            Scope.getCurrentScope().addMdcValue(MdcKey.DEPLOYMENT_OUTCOME, MdcValue.COMMAND_FAILED);
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
                if ("tagDatabase".equals(StringUtils.trimToEmpty(ranChangeSet.getDescription())) && shouldRemoveTag(ranChangeSet)) {
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
            /*
            In versions of Liquibase prior to (and including) 4.25.1, any newer duplicate tags are ignored when rolling
            back to the oldest version of that tag. In future versions, newer duplicate tags are rolled back when rolling
            back to the oldest version of that tag. This if statement and hidden config property allows a user to opt
            for the older functionality if desired.
             */
            if (seenTag && (LiquibaseCommandLineConfiguration.INCLUDE_MATCHING_TAG_IN_ROLLBACK_OLDEST.getCurrentValue() || !tag.equalsIgnoreCase(ranChangeSet.getTag()))) {
                changeLogsAfterTag.add(ranChangeSet.toString());
            }

            if (!seenTag && tag.equalsIgnoreCase(ranChangeSet.getTag())) {
                seenTag = true;
            }
            //changeSet is just tagging the database. Also remove it.
            if (tag.equalsIgnoreCase(ranChangeSet.getTag()) &&
                ("tagDatabase".equals(StringUtils.trimToEmpty(ranChangeSet.getDescription()))) &&
                    shouldRemoveTag(ranChangeSet)) {
                    changeLogsAfterTag.add(ranChangeSet.toString());
                }

        }

        if (!seenTag) {
            Scope.getCurrentScope().addMdcValue(MdcKey.DEPLOYMENT_OUTCOME, MdcValue.COMMAND_FAILED);
            throw new RollbackFailedException("Could not find tag '" + tag + "' in the database");
        }
    }

    /**
     * Check if the tag should be removed on rollback
     */
    private boolean shouldRemoveTag(RanChangeSet ranChangeSet) {
        if (databaseChangeLog != null) {
            ChangeSet changeSet = databaseChangeLog.getChangeSet(ranChangeSet);
            if (changeSet != null) {
                return (changeSet.getChanges().stream().noneMatch(c -> (c instanceof TagDatabaseChange) && BooleanUtils.isTrue(((TagDatabaseChange) c).isKeepTagOnRollback())));
            }
        }
        return true;
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
