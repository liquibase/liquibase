package liquibase.changelog;

import liquibase.RuntimeEnvironment;
import liquibase.Scope;
import liquibase.change.core.TagDatabaseChange;
import liquibase.changelog.filter.ChangeSetFilter;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.changelog.filter.CountChangeSetFilter;
import liquibase.changelog.filter.UpToTagChangeSetFilter;
import liquibase.changelog.visitor.ChangeSetVisitor;
import liquibase.changelog.visitor.SkippedChangeSetVisitor;
import liquibase.exception.LiquibaseException;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 * This class calculates the status of all change sets involved in an update operation
 * It allows us to determine ALL reasons that a ChangeSet will not be deployed, unlike
 * the standard iterator which breaks upon finding a reason to deny the change set.
 *
 */
public class StatusChangeLogIterator extends ChangeLogIterator {
    private String tag;
    public StatusChangeLogIterator(DatabaseChangeLog databaseChangeLog, ChangeSetFilter... changeSetFilters) {
        super(databaseChangeLog, changeSetFilters);
    }

    public StatusChangeLogIterator(DatabaseChangeLog databaseChangeLog, String tag, ChangeSetFilter... changeSetFilters) {
        super(databaseChangeLog, changeSetFilters);
        this.tag = tag;
    }

    @Override
    public void run(ChangeSetVisitor visitor, RuntimeEnvironment env) throws LiquibaseException {
        databaseChangeLog.setRuntimeEnvironment(env);
        try {
            Scope.child(Scope.Attr.databaseChangeLog, databaseChangeLog, () -> {
                List<ChangeSet> changeSetList = new ArrayList<>(databaseChangeLog.getChangeSets());
                if (visitor.getDirection().equals(ChangeSetVisitor.Direction.REVERSE)) {
                    Collections.reverse(changeSetList);
                }
                for (ChangeSet changeSet : changeSetList) {
                    AtomicBoolean shouldVisit = new AtomicBoolean(true);
                    Set<ChangeSetFilterResult> reasonsAccepted = new LinkedHashSet<>();
                    Set<ChangeSetFilterResult> reasonsDenied = new LinkedHashSet<>();
                    if (changeSetFilters != null) {
                        shouldVisit.set(iterateFilters(changeSet, reasonsAccepted, reasonsDenied));
                    }

                    if (shouldVisit.get()) {
                        visitor.visit(changeSet, databaseChangeLog, env.getTargetDatabase(), reasonsAccepted);
                        markSeen(changeSet);
                    } else{
                        if (visitor instanceof SkippedChangeSetVisitor) {
                            ((SkippedChangeSetVisitor) visitor).skipped(changeSet, databaseChangeLog, env.getTargetDatabase(), reasonsDenied);
                        }
                    }
                }
            });
        } catch (Exception e) {
            throw new LiquibaseException(e);
        } finally {
            databaseChangeLog.setRuntimeEnvironment(null);
        }
    }

    //
    // Do not process the tag change and tag filter if this change set was already denied
    //
    private boolean alreadyDeniedForTagChangeAndTagFilter(Set<ChangeSetFilterResult> reasonsDenied, ChangeSet changeSet, ChangeSetFilter filter) {
        return ! (reasonsDenied.isEmpty() || hasTagDatabaseChange(changeSet)) && filter instanceof UpToTagChangeSetFilter;
    }

    //
    // Do not process the count filter if we have already denied the change set
    //
    private boolean alreadyDeniedForCountFilter(Set<ChangeSetFilterResult> reasonsDenied, ChangeSetFilter filter) {
        return ! reasonsDenied.isEmpty() && filter instanceof CountChangeSetFilter;
    }

    //
    // Iterate through the ChangeSetFilter list to assess the status of the ChangeSet
    //
    private boolean iterateFilters(ChangeSet changeSet, Set<ChangeSetFilterResult> reasonsAccepted, Set<ChangeSetFilterResult> reasonsDenied) {
        boolean shouldVisit = true;
        boolean tagDatabaseAlreadyFound = false;
        for (ChangeSetFilter filter : changeSetFilters) {
            //
            // Do not process if:
            //
            // The tag change has been found and this is the tag filter or
            // The change set has been denied and this is the tag change and filter or
            // The change set has been denied and this is the count filter
            //
            if (! (tagDatabaseAlreadyFound && filter instanceof UpToTagChangeSetFilter) &&
                ! alreadyDeniedForTagChangeAndTagFilter(reasonsDenied, changeSet, filter) &&
                ! alreadyDeniedForCountFilter(reasonsDenied, filter)) {
                ChangeSetFilterResult acceptsResult = filter.accepts(changeSet);
                if (acceptsResult.isAccepted()) {
                    reasonsAccepted.add(acceptsResult);
                } else {
                    //
                    // If the change set has already run, then there is no reason
                    // to iterate the other filters
                    //
                    shouldVisit = false;
                    reasonsDenied.add(acceptsResult);
                    if (acceptsResult.getMessage().toLowerCase().contains("changeset already ran")) {
                        break;
                    }
                    //
                    // We are collecting all reasons for skipping
                    // We are skipping this change set, so check to see
                    // if the change set has the tagDatabaseChange.  If it does
                    // then we do not want to process the UpToTagChangeSetFilter
                    // since it will act differently in the case where we are
                    // collecting all reasons
                    //
                    if (! tagDatabaseAlreadyFound) {
                        tagDatabaseAlreadyFound = hasTagDatabaseChange(changeSet);
                    }
                }
            }
        }
        return shouldVisit;
    }

    //
    // Check to see if this change set has a TagDatabase change
    //
    private boolean hasTagDatabaseChange(ChangeSet changeSet) {
        if (this.tag == null) {
            return false;
        }
        return changeSet.getChanges().stream().anyMatch( change -> {
            if (!(change instanceof TagDatabaseChange)) {
                return false;
            }
            TagDatabaseChange tagDatabaseChange = (TagDatabaseChange) change;
            return tagDatabaseChange.getTag().equals(tag);
        });
    }
}
