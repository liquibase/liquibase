package liquibase.changelog;

import liquibase.*;
import liquibase.changelog.filter.ChangeSetFilter;
import liquibase.changelog.filter.ChangeSetFilterResult;
import liquibase.changelog.visitor.ChangeSetVisitor;
import liquibase.changelog.visitor.SkippedChangeSetVisitor;
import liquibase.changelog.visitor.ValidatingVisitor;
import liquibase.exception.LiquibaseException;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.util.BooleanUtil;
import liquibase.util.StringUtil;
import lombok.Getter;

import java.util.*;

import static java.util.ResourceBundle.getBundle;

/**
 * The ChangeLogIterator class is responsible for iterating through a list of ChangeSets in a DatabaseChangeLog
 * and executing a visitor for each ChangeSet that passes the specified filters.
 * It provides methods for running the visitor and validating the Executor for each ChangeSet.
 */
public class ChangeLogIterator {

    protected final DatabaseChangeLog databaseChangeLog;
    protected final List<ChangeSetFilter> changeSetFilters;
    private static final ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");
    private static final String MSG_COULD_NOT_FIND_EXECUTOR = coreBundle.getString("no.executor.found");
    private final Set<String> seenChangeSets = new HashSet<>();
    /**
     * Changesets which encountered an exception during visit.
     */
    @Getter
    private final List<ChangeSet> exceptionChangeSets = new ArrayList<>();
    /**
     * Changesets that were never visited because a previous changeset encountered an exception.
     */
    @Getter
    private final List<ChangeSet> skippedDueToExceptionChangeSets = new ArrayList<>();

    private final Boolean allowDuplicatedChangesetsIdentifiers = GlobalConfiguration.ALLOW_DUPLICATED_CHANGESETS_IDENTIFIERS.getCurrentValue();

    public ChangeLogIterator(DatabaseChangeLog databaseChangeLog, ChangeSetFilter... changeSetFilters) {
        this(databaseChangeLog, Arrays.asList(changeSetFilters));
    }

    public ChangeLogIterator(DatabaseChangeLog databaseChangeLog, List<ChangeSetFilter> changeSetFilters) {
        this.databaseChangeLog = databaseChangeLog;
        this.changeSetFilters = changeSetFilters;
    }

    public ChangeLogIterator(List<RanChangeSet> changeSetList, DatabaseChangeLog changeLog, ChangeSetFilter... changeSetFilters) {
        this(changeSetList, changeLog, Arrays.asList(changeSetFilters));
    }

    public ChangeLogIterator(List<RanChangeSet> changeSetList, DatabaseChangeLog changeLog, List<ChangeSetFilter> changeSetFilters) {
        final List<ChangeSet> changeSets = new ArrayList<>();
        for (RanChangeSet ranChangeSet : changeSetList) {
            final List<ChangeSet> changeSetsForRanChangeSet = changeLog.getChangeSets(ranChangeSet);
            for (ChangeSet changeSet : changeSetsForRanChangeSet) {
                if (changeSet != null) {
                    changeSet.setFilePath(DatabaseChangeLog.normalizePath(ranChangeSet.getChangeLog()));
                    changeSet.setDeploymentId(ranChangeSet.getDeploymentId());
                    changeSets.add(changeSet);
                }
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
        this.changeSetFilters = changeSetFilters;
    }

    public void run(ChangeSetVisitor visitor, RuntimeEnvironment env) throws LiquibaseException {
        databaseChangeLog.setRuntimeEnvironment(env);
        try {
            Scope.child(Scope.Attr.databaseChangeLog, databaseChangeLog, () -> {

                List<ChangeSet> changeSetList = new ArrayList<>(databaseChangeLog.getChangeSets());
                if (visitor.getDirection().equals(ChangeSetVisitor.Direction.REVERSE)) {
                    Collections.reverse(changeSetList);
                }
                for (int i = 0; i < changeSetList.size(); i++) {
                    ChangeSet changeSet = changeSetList.get(i);
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

                    boolean finalShouldVisit = shouldVisit;

                    Map<String, Object> scopeValues = new HashMap<>();
                    scopeValues.put(Scope.Attr.changeSet.name(), changeSet);
                    scopeValues.put(Scope.Attr.database.name(), env.getTargetDatabase());

                    int finalI = i;
                    Scope.child(scopeValues, () -> {
                        if (finalShouldVisit && !alreadySaw(changeSet)) {
                            //
                            // Go validate any changesets with an Executor if
                            // we are using a ValidatingVisitor
                            //
                            if (visitor instanceof ValidatingVisitor) {
                                validateChangeSetExecutor(changeSet, env);
                            }

                            try {
                                visitor.visit(changeSet, databaseChangeLog, env.getTargetDatabase(), reasonsAccepted);
                            } catch (Exception e) {
                                exceptionChangeSets.add(changeSet);
                                skippedDueToExceptionChangeSets.addAll(changeSetList.subList(finalI + 1, changeSetList.size()));

                                throw e;
                            }
                            markSeen(changeSet);
                        } else {
                            if (visitor instanceof SkippedChangeSetVisitor) {
                                ((SkippedChangeSetVisitor) visitor).skipped(changeSet, databaseChangeLog, env.getTargetDatabase(), reasonsDenied);
                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            throw new LiquibaseException(e);
        } finally {
            databaseChangeLog.setRuntimeEnvironment(null);
        }
    }

    /**
     *
     * Make sure that any changeset which has a runWith=<executor> setting
     * has a valid Executor, and that the changes in the changeset are eligible for execution by this Executor
     *
     * @param  changeSet                      The change set to validate
     * @param  env                            A RuntimeEnvironment instance
     * @throws LiquibaseException
     *
     */
    protected void validateChangeSetExecutor(ChangeSet changeSet, RuntimeEnvironment env) throws LiquibaseException {
        if (changeSet.getRunWith() == null) {
            return;
        }
        String executorName = ChangeSet.lookupExecutor(changeSet.getRunWith());

        Executor executor;
        try {
            executor = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor(executorName, env.getTargetDatabase());
        }
        catch (UnexpectedLiquibaseException ule) {
            String message = String.format(MSG_COULD_NOT_FIND_EXECUTOR, executorName, changeSet);
            Scope.getCurrentScope().getLog(getClass()).severe(message);
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
            Scope.getCurrentScope().getLog(getClass()).severe(message);
            throw new LiquibaseException(message);
        }
    }

    protected void markSeen(ChangeSet changeSet) {
        if (changeSet.key == null) {
            changeSet.key = createKey(changeSet);
        }
        seenChangeSets.add(changeSet.key);
    }


    /**
     * By default, this returns false as finalShouldVisit logic has better performance.
     * But prior to 4.19.1 the below logic worked, were if a changeset was already saw we would
     * just ignore it instead of throwing an error. This logic was backported under flag
     * ALLOW_DUPLICATED_CHANGESETS_IDENTIFIERS
     */
    private boolean alreadySaw(ChangeSet changeSet) {
        if (BooleanUtil.isTrue(allowDuplicatedChangesetsIdentifiers)) {
            if (changeSet.key == null) {
                changeSet.key = createKey(changeSet);
            }
            return seenChangeSets.contains(changeSet.key);
        }
        return false;
    }

    /**
     * Creates a unique key to identify this changeset
     */
    protected String createKey(ChangeSet changeSet) {
        Labels labels = changeSet.getLabels();
        ContextExpression contexts = changeSet.getContextFilter();

        return changeSet.toString(false)
                + ":" + (labels == null ? null : labels.toString())
                + ":" + (contexts == null ? null : contexts.toString())
                + ":" + StringUtil.join(changeSet.getDbmsSet(), ",");
    }

    public List<ChangeSetFilter> getChangeSetFilters() {
        return Collections.unmodifiableList(changeSetFilters);
    }
}
