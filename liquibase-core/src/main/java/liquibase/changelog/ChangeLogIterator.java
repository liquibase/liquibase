package liquibase.changelog;

import liquibase.ContextExpression;
import liquibase.Labels;
import liquibase.RuntimeEnvironment;
import liquibase.changelog.filter.ChangeSetFilter;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.changelog.visitor.ChangeSetVisitor;
import liquibase.changelog.visitor.SkippedChangeSetVisitor;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogService;
import liquibase.logging.LogType;
import liquibase.logging.Logger;
import liquibase.logging.LoggerContext;
import liquibase.util.StringUtils;

import java.util.*;

import static java.util.ResourceBundle.getBundle;

public class ChangeLogIterator {
    private static final Logger LOG = LogService.getLog(ChangeLogIterator.class);
    private DatabaseChangeLog databaseChangeLog;
    private List<ChangeSetFilter> changeSetFilters;
    private static ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");
    protected static final String MSG_COULD_NOT_FIND_EXECUTOR = coreBundle.getString("no.executor.found");
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
            // Prevent NPE (CORE-3231)
            @Override
            public String toString() {
                return "";
            }
        });

        this.changeSetFilters = Arrays.asList(changeSetFilters);
    }

    public void run(ChangeSetVisitor visitor, RuntimeEnvironment env) throws LiquibaseException {
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
                        //
                        // Go validate any change sets with an Executor
                        //
                        validateChangeSetExecutor(changeSet, env);

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


    //
    // Make sure that any change set which has a runWith=<executor> setting
    // has a valid Executor, and that the changes in the change set
    // are eligible for execution by this Executor
    //
    private void validateChangeSetExecutor(ChangeSet changeSet, RuntimeEnvironment env) throws LiquibaseException {
        if (changeSet.getRunWith() == null) {
            return;
        }
        String executorName = changeSet.getRunWith();
        Executor executor;
        try {
            executor = ExecutorService.getInstance().getExecutor(executorName, env.getTargetDatabase());
        }
        catch (UnexpectedLiquibaseException ule) {
            String message = String.format(MSG_COULD_NOT_FIND_EXECUTOR, executorName, changeSet.toString());
            LOG.severe(LogType.LOG, message);
            throw new LiquibaseException(message);
        }
        //
        // ASSERT: the Executor is valid
        // allow the Executor to make changes to the object model
        // if needed
        //
        executor.modifyChangeSet(changeSet);

        ValidationErrors errors = executor.validate(changeSet);
        if (errors.hasErrors()) {
            String message = errors.toString();
            LOG.severe(LogType.LOG, message);
            throw new LiquibaseException(message);
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
