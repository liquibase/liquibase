package liquibase.changelog;

import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.RuntimeEnvironment;
import liquibase.changelog.filter.ChangeSetFilter;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.changelog.visitor.ChangeSetVisitor;
import liquibase.changelog.visitor.SkippedChangeSetVisitor;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogService;
import liquibase.logging.Logger;
import liquibase.logging.LoggerContext;
import liquibase.util.StringUtil;

import java.util.*;

public class ChangeLogIterator {
    private DatabaseChangeLog databaseChangeLog;
    private List<ChangeSetFilter> changeSetFilters;

    private Set<String> seenChangeSets = new HashSet<>();

    public ChangeLogIterator(DatabaseChangeLog databaseChangeLog, ChangeSetFilter... changeSetFilters) {
        this.databaseChangeLog = databaseChangeLog;
        this.changeSetFilters = Arrays.asList(changeSetFilters);
    }

    public ChangeLogIterator(List<RanChangeSet> changeSetList, DatabaseChangeLog changeLog, ChangeSetFilter... changeSetFilters) {
        final List<ChangeSet> changeSets = new ArrayList<>();
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
        Logger log = LogService.getLog(getClass());
        databaseChangeLog.setRuntimeEnvironment(env);
        try (LoggerContext ignored = LogService.pushContext("databaseChangeLog", databaseChangeLog)) {
            List<ChangeSet> changeSetList = new ArrayList<>(databaseChangeLog.getChangeSets());
            if (visitor.getDirection().equals(ChangeSetVisitor.Direction.REVERSE)) {
                Collections.reverse(changeSetList);
            }

            for (ChangeSet changeSet : changeSetList) {
                boolean shouldVisit = true;
                Set<ChangeSetFilterResult> reasonsAccepted = new HashSet<>();
                Set<ChangeSetFilterResult> reasonsDenied = new HashSet<>();
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

                try (LoggerContext ignored2 = LogService.pushContext("changeSet", changeSet)) {
                    if (shouldVisit && !alreadySaw(changeSet)) {
                        visitor.visit(changeSet, databaseChangeLog, env.getTargetDatabase(), reasonsAccepted);
                        markSeen(changeSet);
                    } else {
                        if (visitor instanceof SkippedChangeSetVisitor) {
                            ((SkippedChangeSetVisitor) visitor).skipped(changeSet, databaseChangeLog, env.getTargetDatabase(), reasonsDenied);
                        }
                    }
                }
            }
        } finally {
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
                + ":" + StringUtil.join(changeSet.getDbmsSet(), ",");
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
