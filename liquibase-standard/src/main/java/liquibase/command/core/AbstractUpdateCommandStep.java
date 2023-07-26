package liquibase.command.core;

import liquibase.*;
import liquibase.changelog.*;
import liquibase.changelog.filter.*;
import liquibase.changelog.visitor.*;
import liquibase.command.AbstractCommandStep;
import liquibase.command.CleanUpCommandStep;
import liquibase.command.CommandResultsBuilder;
import liquibase.command.CommandScope;
import liquibase.command.core.helpers.DatabaseChangelogCommandStep;
import liquibase.database.Database;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.executor.ExecutorService;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.mdc.MdcKey;
import liquibase.logging.mdc.MdcObject;
import liquibase.logging.mdc.MdcValue;
import liquibase.logging.mdc.customobjects.ChangesetsUpdated;
import liquibase.util.ShowSummaryUtil;
import liquibase.util.StringUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static liquibase.Liquibase.MSG_COULD_NOT_RELEASE_LOCK;

public abstract class AbstractUpdateCommandStep extends AbstractCommandStep implements CleanUpCommandStep {
    public static final String DEFAULT_CHANGE_EXEC_LISTENER_RESULT_KEY = "defaultChangeExecListener";
    private boolean isFastCheckEnabled = true;

    private boolean isDBLocked = true;

    public abstract String getChangelogFileArg(CommandScope commandScope);
    public abstract String getContextsArg(CommandScope commandScope);
    public abstract String getLabelFilterArg(CommandScope commandScope);
    public abstract String[] getCommandName();
    public abstract UpdateSummaryEnum getShowSummary(CommandScope commandScope);

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(Database.class, LockService.class, DatabaseChangeLog.class, ChangeExecListener.class, ChangeLogParameters.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();
        Database database = (Database) commandScope.getDependency(Database.class);
        Contexts contexts = new Contexts(getContextsArg(commandScope));
        LabelExpression labelExpression = new LabelExpression(getLabelFilterArg(commandScope));
        DatabaseChangelogCommandStep.addCommandFiltersMdc(labelExpression, contexts);
        customMdcLogging(commandScope);

        ChangeExecListener changeExecListener = getChangeExecListener(resultsBuilder, commandScope);
        try {
            DatabaseChangeLog databaseChangeLog = (DatabaseChangeLog) commandScope.getDependency(DatabaseChangeLog.class);
            if (isFastCheckEnabled && isUpToDate(commandScope, database, databaseChangeLog, contexts, labelExpression, resultsBuilder.getOutputStream())) {
                return;
            }
            if(!isDBLocked) {
                LockServiceFactory.getInstance().getLockService(database).waitForLock();
                // waitForLock resets the changelog history service, so we need to rebuild that and generate a final deploymentId.
                ChangeLogHistoryService changeLogHistoryService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database);
                changeLogHistoryService.init();
                changeLogHistoryService.generateDeploymentId();
            }

            ChangeLogHistoryService changelogService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database);
            Scope.getCurrentScope().addMdcValue(MdcKey.DEPLOYMENT_ID, changelogService.getDeploymentId());
            Scope.getCurrentScope().getLog(getClass()).info(String.format("Using deploymentId: %s", changelogService.getDeploymentId()));

            StatusVisitor statusVisitor = getStatusVisitor(commandScope, database, contexts, labelExpression, databaseChangeLog);

            ChangeLogIterator runChangeLogIterator = getStandardChangelogIterator(commandScope, database, contexts, labelExpression, databaseChangeLog);
            AtomicInteger rowsAffected = new AtomicInteger(0);

            HashMap<String, Object> scopeValues = new HashMap<>();
            scopeValues.put("showSummary", getShowSummary(commandScope));
            scopeValues.put("rowsAffected", rowsAffected);
            Scope.child(scopeValues, () -> {
                runChangeLogIterator.run(new UpdateVisitor(database, changeExecListener, new ShouldRunChangeSetFilter(database)),
                        new RuntimeEnvironment(database, contexts, labelExpression));
                ShowSummaryUtil.showUpdateSummary(databaseChangeLog, getShowSummary(commandScope), statusVisitor, resultsBuilder.getOutputStream());
            });

            resultsBuilder.addResult("statusCode", 0);
            addChangelogFileToMdc(getChangelogFileArg(commandScope), databaseChangeLog);
            Scope.getCurrentScope().addMdcValue(MdcKey.ROWS_AFFECTED, String.valueOf(rowsAffected.get()));
            logDeploymentOutcomeMdc(changeExecListener, true);
            postUpdateLog(rowsAffected.get());
        } catch (Exception e) {
            DatabaseChangeLog databaseChangeLog = (DatabaseChangeLog) commandScope.getDependency(DatabaseChangeLog.class);
            addChangelogFileToMdc(getChangelogFileArg(commandScope), databaseChangeLog);
            logDeploymentOutcomeMdc(changeExecListener, false);
            resultsBuilder.addResult("statusCode", 1);
            throw e;
        } finally {
            //TODO: We should be able to remove this once we get the rest of the update family
            // set up with the CommandFramework
            try {
                LockServiceFactory.getInstance().getLockService(database).releaseLock();
            } catch (LockException e) {
                Scope.getCurrentScope().getLog(getClass()).severe(MSG_COULD_NOT_RELEASE_LOCK, e);
            }
        }
    }

    private StatusVisitor getStatusVisitor(CommandScope commandScope, Database database, Contexts contexts, LabelExpression labelExpression, DatabaseChangeLog databaseChangeLog) throws LiquibaseException {
        StatusVisitor statusVisitor = new StatusVisitor(database);
        ChangeLogIterator shouldRunIterator = getStatusChangelogIterator(commandScope, database, contexts, labelExpression, databaseChangeLog);
        shouldRunIterator.run(statusVisitor, new RuntimeEnvironment(database, contexts, labelExpression));
        return statusVisitor;
    }

    private ChangeExecListener getChangeExecListener(CommandResultsBuilder resultsBuilder, CommandScope commandScope) {
        //
        // Create and add the listener to the resultsBuilder so that it is available
        // for exception handling when there is an error
        //
        ChangeExecListener changeExecListener = (ChangeExecListener) commandScope.getDependency(ChangeExecListener.class);
        // Because of MDC we need to reset the cached changesets in the listener, because in the case of something like
        // update-testing-rollback, the same listener is used for both updates
        if (changeExecListener instanceof DefaultChangeExecListener) {
            ((DefaultChangeExecListener)changeExecListener).reset();
        }
        resultsBuilder.addResult(DEFAULT_CHANGE_EXEC_LISTENER_RESULT_KEY, changeExecListener);
        return changeExecListener;
    }

    private void addChangelogFileToMdc(String changeLogFile, DatabaseChangeLog databaseChangeLog) {
        if (StringUtil.isNotEmpty(databaseChangeLog.getLogicalFilePath())) {
            Scope.getCurrentScope().addMdcValue(MdcKey.CHANGELOG_FILE, databaseChangeLog.getLogicalFilePath());
        } else {
            Scope.getCurrentScope().addMdcValue(MdcKey.CHANGELOG_FILE, changeLogFile);
        }
    }

    protected void customMdcLogging(CommandScope commandScope) {
        // do nothing by default
    }

    @Override
    public void cleanUp(CommandResultsBuilder resultsBuilder) {
        LockServiceFactory.getInstance().resetAll();
        Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).resetAll();
        Scope.getCurrentScope().getSingleton(ExecutorService.class).reset();
    }

    private void logDeploymentOutcomeMdc(ChangeExecListener defaultListener, boolean success) {
        String successLog = "Update command completed successfully.";
        String failureLog = "Update command encountered an exception.";
        try (MdcObject deploymentOutcomeMdc = Scope.getCurrentScope().addMdcValue(MdcKey.DEPLOYMENT_OUTCOME, success ? MdcValue.COMMAND_SUCCESSFUL : MdcValue.COMMAND_FAILED)) {
            Scope.getCurrentScope().getLog(getClass()).info(success ? successLog : failureLog);
        }

        if (defaultListener instanceof DefaultChangeExecListener) {
            List<ChangeSet> deployedChangeSets = ((DefaultChangeExecListener)defaultListener).getDeployedChangeSets();
            int deployedChangeSetCount = deployedChangeSets.size();
            ChangesetsUpdated changesetsUpdated = new ChangesetsUpdated(deployedChangeSets);
            Scope.getCurrentScope().addMdcValue(MdcKey.DEPLOYMENT_OUTCOME_COUNT, String.valueOf(deployedChangeSetCount));
            Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESETS_UPDATED, changesetsUpdated);
        }
    }

    @Beta
    public ChangeLogIterator getStandardChangelogIterator(CommandScope commandScope, Database database, Contexts contexts, LabelExpression labelExpression, DatabaseChangeLog changeLog) throws DatabaseException {
        List<ChangeSetFilter> changesetFilters = this.getStandardChangelogIteratorFilters(database, contexts, labelExpression);
        return new ChangeLogIterator(changeLog, changesetFilters.toArray(new ChangeSetFilter[0]));
    }

    @Beta
    public ChangeLogIterator getStatusChangelogIterator(CommandScope commandScope, Database database, Contexts contexts, LabelExpression labelExpression, DatabaseChangeLog changeLog) throws DatabaseException {
        List<ChangeSetFilter> changesetFilters = this.getStandardChangelogIteratorFilters(database, contexts, labelExpression);
        changesetFilters.add(new ShouldRunChangeSetFilter(database));
        return new StatusChangeLogIterator(changeLog, changesetFilters.toArray(new ChangeSetFilter[0]));
    }

    protected List<ChangeSetFilter> getStandardChangelogIteratorFilters(Database database, Contexts contexts, LabelExpression labelExpression) {
        return new ArrayList<>(Arrays.asList(new ContextChangeSetFilter(contexts),
                new LabelChangeSetFilter(labelExpression),
                new DbmsChangeSetFilter(database),
                new IgnoreChangeSetFilter()));
    }

    /**
     * Performs check of the historyService to determine if there is no unrun changesets without obtaining an exclusive write lock.
     * This allows multiple peer services to boot in parallel in the common case where there are no changelogs to run.
     * <p>
     * If we see that there is nothing in the changelog to run and this returns <b>true</b>, then regardless of the lock status we already know we are "done" and can finish up without waiting for the lock.
     * <p>
     * But, if there are changelogs that might have to be ran and this returns <b>false</b>, you MUST get a lock and do a real check to know what changesets actually need to run.
     * <p>
     * NOTE: to reduce the number of queries to the databasehistory table, this method will cache the "fast check" results within this instance under the assumption that the total changesets will not change within this instance.
     */
    private final Map<String, Boolean> upToDateFastCheck = new ConcurrentHashMap<>();

    public boolean isUpToDateFastCheck(CommandScope commandScope, Database database, DatabaseChangeLog databaseChangeLog, Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        String cacheKey = String.format("%s/%s/%s/%s/%s", contexts, labelExpression, database.getDefaultSchemaName(), database.getDefaultCatalogName(), database.getConnection().getURL());
        if (!upToDateFastCheck.containsKey(cacheKey)) {
            ChangeLogHistoryService changeLogService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database);
            try {
                if (changeLogService.isDatabaseChecksumsCompatible() && listUnrunChangeSets(database, databaseChangeLog, contexts, labelExpression).isEmpty()) {
                    Scope.getCurrentScope().getLog(getClass()).fine("Fast check found no un-run changesets");
                    upToDateFastCheck.put(cacheKey, true);
                } else {
                    upToDateFastCheck.put(cacheKey, false);
                }
            } catch (DatabaseException e) {
                Scope.getCurrentScope().getLog(getClass()).info("Error querying Liquibase tables, disabling fast check for this execution. Reason: " + e.getMessage());
                upToDateFastCheck.put(cacheKey, false);
            } finally {
                // Discard the cached fetched un-run changeset list, as if
                // another peer is running the changesets in parallel, we may
                // get a different answer after taking out the write lock
                changeLogService.reset();
            }
        }
        return upToDateFastCheck.get(cacheKey);
    }

    /**
     * Get list of ChangeSet which have not been applied
     *
     * @param database          the target database
     * @param databaseChangeLog the database changelog
     * @param contexts          the command contexts
     * @param labels            the command label expressions
     * @return a list of ChangeSet that have not been applied
     * @throws LiquibaseException if there was a problem building our ChangeLogIterator or checking the database
     */
    private List<ChangeSet> listUnrunChangeSets(Database database, DatabaseChangeLog databaseChangeLog, Contexts contexts, LabelExpression labels) throws LiquibaseException {
        ListVisitor visitor = new ListVisitor();
        databaseChangeLog.validate(database, contexts, labels);
        List<ChangeSetFilter> changesetFilters = this.getStandardChangelogIteratorFilters(database, contexts, labels);
        changesetFilters.add(new ShouldRunChangeSetFilter(database));
        ChangeLogIterator logIterator = new ChangeLogIterator(databaseChangeLog, changesetFilters.toArray(new ChangeSetFilter[0]));
        logIterator.run(visitor, new RuntimeEnvironment(database, contexts, labels));
        return visitor.getSeenChangeSets();
    }


    /**
     * Checks if the database is up-to-date.
     *
     * @param commandScope
     * @param database          the database to check
     * @param databaseChangeLog the databaseChangeLog of the database
     * @param contexts          the command contexts
     * @param labelExpression   the command label expressions
     * @param outputStream      the current global OutputStream
     * @return true if there are no additional changes to execute, otherwise false
     * @throws LiquibaseException if there was a problem running any queries
     * @throws IOException        if there was a problem handling the update summary
     */
    @Beta
    public boolean isUpToDate(CommandScope commandScope, Database database, DatabaseChangeLog databaseChangeLog, Contexts contexts, LabelExpression labelExpression, OutputStream outputStream) throws LiquibaseException, IOException {
        if (isUpToDateFastCheck(commandScope, database, databaseChangeLog, contexts, labelExpression)) {
            Scope.getCurrentScope().getUI().sendMessage("Database is up to date, no changesets to execute");
            StatusVisitor statusVisitor = getStatusVisitor(commandScope, database, contexts, labelExpression, databaseChangeLog);
            UpdateSummaryEnum showSummary = getShowSummary(commandScope);
            ShowSummaryUtil.showUpdateSummary(databaseChangeLog, showSummary, statusVisitor, outputStream);
            return true;
        }
        return false;
    }

    public void setFastCheckEnabled(boolean fastCheckEnabled) {
        isFastCheckEnabled = fastCheckEnabled;
    }

    /**
     * Log
     */
    @Beta
    public void postUpdateLog(int rowsAffected) {

    }

    protected void setDBLock(boolean locked) {
        isDBLocked = locked;
    }
}
