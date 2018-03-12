package liquibase.changelog;

import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.RuntimeEnvironment;
import liquibase.changelog.filter.*;
import liquibase.changelog.visitor.SkippedChangeSetVisitor;
import liquibase.changelog.visitor.ChangeSetVisitor;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.util.CollectionUtil;
import liquibase.util.StringUtils;

import java.util.*;

public class ChangeLogIterator {
    private DatabaseChangeLog databaseChangeLog;
    private List<ChangeSetFilter> changeSetFilters;

    private Set<String> seenChangeSets = new HashSet<String>();

    public ChangeLogIterator(DatabaseChangeLog databaseChangeLog, ChangeSetFilter... changeSetFilters) {
        this.databaseChangeLog = databaseChangeLog;
        this.changeSetFilters = Arrays.asList(changeSetFilters);
    }

    public ChangeLogIterator(List<RanChangeSet> changeSetList, DatabaseChangeLog changeLog, ChangeSetFilter... changeSetFilters) {
        final List<ChangeSet> changeSets = new ArrayList<ChangeSet>();
        for (RanChangeSet ranChangeSet : changeSetList) {
            ChangeSet changeSet = changeLog.getChangeSet(ranChangeSet);
            if (changeSet != null) {
                if (changeLog.ignoreClasspathPrefix()) {
                    changeSet.setFilePath(ranChangeSet.getChangeLog());
                }
                changeSets.add(changeSet);
            }
        }
        this.databaseChangeLog = (new DatabaseChangeLog() {
            @Override
            public List<ChangeSet> getChangeSets() {
                return changeSets;
            }
        });

        this.changeSetFilters = Arrays.asList(changeSetFilters);
    }

    public void run(ChangeSetVisitor visitor, RuntimeEnvironment env) throws LiquibaseException {
        Logger log = LogFactory.getLogger();
        databaseChangeLog.setRuntimeEnvironment(env);
        log.setChangeLog(databaseChangeLog);
        try {
            List<ChangeSet> changeSetList = new ArrayList<ChangeSet>(databaseChangeLog.getChangeSets());
            if (visitor.getDirection().equals(ChangeSetVisitor.Direction.REVERSE)) {
                Collections.reverse(changeSetList);
            }

            for (ChangeSet changeSet : changeSetList) {
                boolean shouldVisit = true;
                Set<ChangeSetFilterResult> reasonsAccepted = new HashSet<ChangeSetFilterResult>();
                Set<ChangeSetFilterResult> reasonsDenied = new HashSet<ChangeSetFilterResult>();
                if (changeSetFilters != null) {
                    for (ChangeSetFilter filter : changeSetFilters) {
                        ChangeSetFilterResult acceptsResult = filter.accepts(changeSet);
                        if (acceptsResult.isAccepted()) {
                            reasonsAccepted.add(acceptsResult);
                        } else {
                            shouldVisit = false;
                            reasonsDenied.add(acceptsResult);
                            break;
                        }
                    }
                }

                log.setChangeSet(changeSet);
                if (shouldVisit && !alreadySaw(changeSet)) {
                    visitor.visit(changeSet, databaseChangeLog, env.getTargetDatabase(changeSet.getDbConnection()), reasonsAccepted);
                    markSeen(changeSet);
                } else {
                    if (visitor instanceof SkippedChangeSetVisitor) {
                        ((SkippedChangeSetVisitor) visitor).skipped(changeSet, databaseChangeLog, env.getTargetDatabase(changeSet.getDbConnection()), reasonsDenied);
                    }
                }
                log.setChangeSet(null);
            }
        } finally {
            log.setChangeLog(null);
            databaseChangeLog.setRuntimeEnvironment(null);
        }
    }

    protected void markSeen(ChangeSet changeSet) {
        if (changeSet.key == null) {
            changeSet.key = createKey(changeSet);
        }

        seenChangeSets.add(changeSet.key);

    }

    protected String createKey(ChangeSet changeSet) {
        Labels labels = changeSet.getLabels();
        ContextExpression contexts = changeSet.getContexts();

        return changeSet.toString(true)
                + ":" + (labels == null ? null : labels.toString())
                + ":" + (contexts == null ? null : contexts.toString())
                + ":" + StringUtils.join(changeSet.getDbmsSet(), ",");
    }

    protected boolean alreadySaw(ChangeSet changeSet) {
        if (changeSet.key == null) {
            changeSet.key = createKey(changeSet);
        }
        return seenChangeSets.contains(changeSet.key);
    }

    public List<ChangeSetFilter> getChangeSetFilters() {
        return Collections.unmodifiableList(changeSetFilters);
    }
}
