package liquibase.changelog;

import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.RuntimeEnvironment;
import liquibase.Scope;
import liquibase.change.core.TagDatabaseChange;
import liquibase.changelog.filter.ChangeSetFilter;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.changelog.filter.CountChangeSetFilter;
import liquibase.changelog.filter.UpToTagChangeSetFilter;
import liquibase.changelog.visitor.ChangeSetVisitor;
import liquibase.changelog.visitor.SkippedChangeSetVisitor;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.util.StringUtil;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class StatusChangeLogIterator extends ChangeLogIterator {

    public StatusChangeLogIterator(DatabaseChangeLog databaseChangeLog, ChangeSetFilter... changeSetFilters) {
        super(databaseChangeLog, changeSetFilters);
    }

    @Override
    public void run(ChangeSetVisitor visitor, RuntimeEnvironment env) throws LiquibaseException {
        databaseChangeLog.setRuntimeEnvironment(env);
        try {
            Scope.child(Scope.Attr.databaseChangeLog, databaseChangeLog, new Scope.ScopedRunner() {
                @Override
                public void run() throws Exception {
                    List<ChangeSet> changeSetList = new ArrayList<>(databaseChangeLog.getChangeSets());
                    if (visitor.getDirection().equals(ChangeSetVisitor.Direction.REVERSE)) {
                        Collections.reverse(changeSetList);
                    }
                    for (ChangeSet changeSet : changeSetList) {
                        AtomicBoolean shouldVisit = new AtomicBoolean(true);
                        Set<ChangeSetFilterResult> reasonsAccepted = new HashSet<>();
                        Set<ChangeSetFilterResult> reasonsDenied = new HashSet<>();
                        if (changeSetFilters != null) {
                            shouldVisit.set(iterateFilters(changeSet, reasonsAccepted, reasonsDenied));
                        }

                        if (shouldVisit.get() && !alreadySaw(changeSet)) {
                            visitor.visit(changeSet, databaseChangeLog, env.getTargetDatabase(), reasonsAccepted);
                            markSeen(changeSet);
                        } else{
                            if (visitor instanceof SkippedChangeSetVisitor) {
                                ((SkippedChangeSetVisitor) visitor).skipped(changeSet, databaseChangeLog, env.getTargetDatabase(), reasonsDenied);
                            }
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

    private boolean iterateFilters(ChangeSet changeSet, Set<ChangeSetFilterResult> reasonsAccepted, Set<ChangeSetFilterResult> reasonsDenied) {
        boolean shouldVisit = true;
        boolean tagDatabaseExists = false;
        for (ChangeSetFilter filter : changeSetFilters) {
            if (! (tagDatabaseExists && filter instanceof UpToTagChangeSetFilter)) {
                if (! reasonsDenied.isEmpty() && filter instanceof CountChangeSetFilter) {
                    continue;
                }
                ChangeSetFilterResult acceptsResult = filter.accepts(changeSet);
                if (acceptsResult.isAccepted()) {
                    reasonsAccepted.add(acceptsResult);
                } else {
                    shouldVisit = false;
                    reasonsDenied.add(acceptsResult);
                    //
                    // We are collecting all reasons for skipping
                    // We are skipping this change set, so check to see
                    // if the change set has a tagDatabaseChange.  If it does
                    // then we do not want to process the UpToTagChangeSetFilter
                    // since it will act differently in the case where we are
                    // collection all reasons
                    //
                    if (! tagDatabaseExists) {
                        tagDatabaseExists = hasTagDatabaseChange(changeSet);
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
        return changeSet.getChanges().stream().anyMatch(TagDatabaseChange.class::isInstance);
    }
}
