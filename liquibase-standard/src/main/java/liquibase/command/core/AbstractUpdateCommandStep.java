package liquibase.command.core;

import liquibase.*;
import liquibase.changelog.*;
import liquibase.changelog.filter.*;
import liquibase.changelog.visitor.ChangeExecListener;
import liquibase.changelog.visitor.DefaultChangeExecListener;
import liquibase.changelog.visitor.StatusVisitor;
import liquibase.changelog.visitor.UpdateVisitor;
import liquibase.command.AbstractCommandStep;
import liquibase.command.CleanUpCommandStep;
import liquibase.command.CommandResultsBuilder;
import liquibase.command.CommandScope;
import liquibase.command.core.helpers.DatabaseChangelogCommandStep;
import liquibase.database.Database;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.executor.ExecutorService;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.mdc.MdcKey;
import liquibase.logging.mdc.MdcObject;
import liquibase.logging.mdc.MdcValue;
import liquibase.logging.mdc.customobjects.ChangesetsUpdated;
import liquibase.report.UpdateReportParameters;
import liquibase.util.ShowSummaryUtil;
import liquibase.util.StringUtil;
import liquibase.util.UpdateSummaryDetails;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static liquibase.Liquibase.MSG_COULD_NOT_RELEASE_LOCK;
import static liquibase.executor.jvm.JdbcExecutor.ROWS_AFFECTED_SCOPE_KEY;

public abstract class AbstractUpdateCommandStep extends AbstractCommandStep implements CleanUpCommandStep {
    public static final String DEFAULT_CHANGE_EXEC_LISTENER_RESULT_KEY = "defaultChangeExecListener";
    private static final String DATABASE_UP_TO_DATE_MESSAGE = "Database is up to date, no changesets to execute";
    private boolean isFastCheckEnabled = true;

    private final ThreadLocal<Boolean> isDBLocked = ThreadLocal.withInitial(() -> true);

    public abstract String getChangelogFileArg(CommandScope commandScope);

    public abstract String getContextsArg(CommandScope commandScope);

    public abstract String getLabelFilterArg(CommandScope commandScope);

    public abstract String[] getCommandName();

    public abstract UpdateSummaryEnum getShowSummary(CommandScope commandScope);

    public UpdateSummaryOutputEnum getShowSummaryOutput(CommandScope commandScope) {
        return (UpdateSummaryOutputEnum) commandScope.getDependency(UpdateSummaryOutputEnum.class);
    }

    @Override
    public List<Class<?>> requiredDependencies() {
        return Arrays.asList(Database.class, LockService.class, DatabaseChangeLog.class, ChangeExecListener.class, ChangeLogParameters.class);
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        Scope scope = Scope.getCurrentScope();
        UpdateReportParameters updateReportParameters = new UpdateReportParameters();
        String[] commandName = getCommandName();
        String formattedCommandName = getFormattedCommandName(commandName).replace("Sql", "SQL");
        updateReportParameters.setCommandTitle(formattedCommandName);
        resultsBuilder.addResult("updateReport", updateReportParameters);
        CommandScope commandScope = resultsBuilder.getCommandScope();
        Database database = (Database) commandScope.getDependency(Database.class);
        updateReportParameters.getDatabaseInfo().setDatabaseType(database.getDatabaseProductName());
        updateReportParameters.getDatabaseInfo().setVersion(database.getDatabaseProductVersion());
        if (database.getConnection() == null) {
            throw new LiquibaseException("Database connection is not available");
        }
        String visibleUrl = database.getConnection().getVisibleUrl();
        String connectionUrl = database.getConnection().getURL();
        updateReportParameters.getDatabaseInfo().setDatabaseUrl(visibleUrl);
        updateReportParameters.setJdbcUrl(connectionUrl);
        final ChangeLogParameters changeLogParameters = (ChangeLogParameters) commandScope.getDependency(ChangeLogParameters.class);
        Contexts contexts = new Contexts(getContextsArg(commandScope));
        LabelExpression labelExpression = new LabelExpression(getLabelFilterArg(commandScope));
        updateReportParameters.getOperationInfo().setLabels(labelExpression.getOriginalString());
        updateReportParameters.getOperationInfo().setContexts(contexts.toString());
        DatabaseChangelogCommandStep.addCommandFiltersMdc(labelExpression, contexts);
        customMdcLogging(commandScope);

        ChangeExecListener changeExecListener = getChangeExecListener(resultsBuilder, commandScope);
        ChangeLogIterator runChangeLogIterator = null;
        try {
            DatabaseChangeLog databaseChangeLog = (DatabaseChangeLog) commandScope.getDependency(DatabaseChangeLog.class);
            updateReportParameters.setChangelogArgValue(databaseChangeLog.getFilePath());
            runChangeLogIterator = getStandardChangelogIterator(commandScope, database, contexts, labelExpression, databaseChangeLog);
            preRun(commandScope, runChangeLogIterator, changeLogParameters);
            if (isFastCheckEnabled && isUpToDate(commandScope, database, databaseChangeLog, contexts, labelExpression, resultsBuilder.getOutputStream())) {
                updateReportParameters.getOperationInfo().setRowsAffected(0);
                updateReportParameters.getOperationInfo().setUpdateSummaryMsg(DATABASE_UP_TO_DATE_MESSAGE);
                return;
            }
            if (!isDBLocked.get()) {
                LockServiceFactory.getInstance().getLockService(database).waitForLock();
                isDBLocked.set(true);
            }

            Scope.getCurrentScope().addMdcValue(MdcKey.DEPLOYMENT_ID, scope.getDeploymentId());
            Scope.getCurrentScope().getLog(getClass()).info(String.format("Using deploymentId: %s", scope.getDeploymentId()));

            StatusVisitor statusVisitor = getStatusVisitor(commandScope, database, contexts, labelExpression, databaseChangeLog);

            AtomicInteger rowsAffected = new AtomicInteger(0);
            HashMap<String, Object> scopeValues = new HashMap<>();
            scopeValues.put("showSummary", getShowSummary(commandScope));
            scopeValues.put(ROWS_AFFECTED_SCOPE_KEY, rowsAffected);
            ChangeLogIterator finalRunChangeLogIterator = runChangeLogIterator;
            Scope.child(scopeValues, () -> {
                try {
                    finalRunChangeLogIterator.run(new UpdateVisitor(database, changeExecListener, new ShouldRunChangeSetFilter(database)),
                            new RuntimeEnvironment(database, contexts, labelExpression));
                } finally {
                    UpdateSummaryDetails details = ShowSummaryUtil.buildSummaryDetails(databaseChangeLog, getShowSummary(commandScope), getShowSummaryOutput(commandScope), statusVisitor, resultsBuilder.getOutputStream(), finalRunChangeLogIterator, changeExecListener);
                    if (details != null) {
                        updateReportParameters.getOperationInfo().setUpdateSummaryMsg(details.getOutput());
                        updateReportParameters.getChangesetInfo().addAllToPendingChangesetInfoList(details.getSkipped());
                        updateReportParameters.getChangesetInfo().setPendingChangesetCount(updateReportParameters.getChangesetInfo().getPendingChangesetInfoList().size());
                    }
                }
            });
            updateReportParameters.getOperationInfo().setRowsAffected(rowsAffected.get());
            database.afterUpdate();
            resultsBuilder.addResult("statusCode", 0);
            addChangelogFileToMdc(getChangelogFileArg(commandScope), databaseChangeLog);
            Scope.getCurrentScope().addMdcValue(MdcKey.ROWS_AFFECTED, String.valueOf(rowsAffected.get()));
            logDeploymentOutcomeMdc(changeExecListener, true, updateReportParameters, runChangeLogIterator);
            postUpdateLog(rowsAffected.get());
        } catch (Exception e) {
            DatabaseChangeLog databaseChangeLog = (DatabaseChangeLog) commandScope.getDependency(DatabaseChangeLog.class);
            addChangelogFileToMdc(getChangelogFileArg(commandScope), databaseChangeLog);
            logDeploymentOutcomeMdc(changeExecListener, false, updateReportParameters, runChangeLogIterator);
            updateReportParameters.getOperationInfo().setException(e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            resultsBuilder.addResult("statusCode", 1);
            throw e;
        } finally {
            if (isDBLocked.get()) {
                try {
                    LockServiceFactory.getInstance().getLockService(database).releaseLock();
                } catch (LockException e) {
                    Scope.getCurrentScope().getLog(getClass()).severe(MSG_COULD_NOT_RELEASE_LOCK, e);
                }
            }
        }
    }

    /**
     * Executed before running any updates against the database.
     */
    protected void preRun(CommandScope commandScope, ChangeLogIterator runChangeLogIterator, ChangeLogParameters changeLogParameters) throws LiquibaseException {
        // do nothing by default
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
            ((DefaultChangeExecListener) changeExecListener).reset();
        }
        resultsBuilder.addResult(DEFAULT_CHANGE_EXEC_LISTENER_RESULT_KEY, changeExecListener);
        return changeExecListener;
    }

    private void addChangelogFileToMdc(String changeLogFile, DatabaseChangeLog databaseChangeLog) {
        if (StringUtil.isNotEmpty(databaseChangeLog.getLogicalFilePath())) {
            Scope.getCurrentScope().addMdcValue(MdcKey.CHANGELOG_FILE, databaseChangeLog.getLogicalFilePath());
        } else if (StringUtil.isNotEmpty(changeLogFile)) {
            Scope.getCurrentScope().addMdcValue(MdcKey.CHANGELOG_FILE, changeLogFile);
        }
    }

    protected void customMdcLogging(CommandScope commandScope) {
        // do nothing by default
    }

    @Override
    public void cleanUp(CommandResultsBuilder resultsBuilder) {
        isDBLocked.remove();
        LockServiceFactory.getInstance().resetAll();
        Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).resetAll();
        Scope.getCurrentScope().getSingleton(ExecutorService.class).reset();
    }

    private void logDeploymentOutcomeMdc(ChangeExecListener defaultListener, boolean success, UpdateReportParameters updateReportParameters,
                                         ChangeLogIterator runChangeLogIterator) {
        String successLog = "Update command completed successfully.";
        String failureLog = "Update command encountered an exception.";
        if (defaultListener instanceof DefaultChangeExecListener) {
            List<ChangeSet> deployedChangeSets = ((DefaultChangeExecListener) defaultListener).getDeployedChangeSets();
            int deployedChangeSetCount = deployedChangeSets.size();
            List<ChangeSet> failedChangeSets = ((DefaultChangeExecListener) defaultListener).getFailedChangeSets();
            int exceptionChangeSetsCount = 0;
            int failedChangeSetCount = failedChangeSets.size();
            //
            // Use the changelog iterator to retrieve information about change sets which
            // had an exception, but were marked as fail_on_error=false
            //
            List<ChangeSet> exceptionChangeSets =
                    runChangeLogIterator != null ? uniqueExceptionChangesets(runChangeLogIterator.getExceptionChangeSets(), failedChangeSets) :
                                                   new ArrayList<>();
            if (runChangeLogIterator != null && ! exceptionChangeSets.isEmpty()) {
                exceptionChangeSetsCount = exceptionChangeSets.size();
                deployedChangeSetCount -= exceptionChangeSetsCount;
                failedChangeSetCount += exceptionChangeSetsCount;
            } else {
                exceptionChangeSets = new ArrayList<>();
            }
            ChangesetsUpdated changesetsUpdated = new ChangesetsUpdated(deployedChangeSets);
            updateReportParameters.getChangesetInfo().setChangesetCount(deployedChangeSetCount + failedChangeSetCount - exceptionChangeSetsCount);
            updateReportParameters.getChangesetInfo().setFailedChangesetCount(failedChangeSetCount);
            updateReportParameters.getChangesetInfo().addAllToChangesetInfoList(deployedChangeSets, false);
            updateReportParameters.getChangesetInfo().addAllToChangesetInfoList(failedChangeSets, false);
            if (!updateReportParameters.getChangesetInfo().getPendingChangesetInfoList().isEmpty()) {
                // If there are failures remove these changes from the pending changeset list
                // and update the count to reflect only skipped(pending) changes by removing the failed count
                updateReportParameters.getChangesetInfo().getPendingChangesetInfoList()
                        .removeIf(pendingChangesetInfo -> failedChangeSets.stream().anyMatch(changeSet -> changeSet.equals(pendingChangesetInfo.getChangeSet())));
                List<ChangeSet> finalExceptionChangeSets = exceptionChangeSets;
                updateReportParameters.getChangesetInfo().getPendingChangesetInfoList()
                        .removeIf(pendingChangesetInfo -> finalExceptionChangeSets.stream().anyMatch(changeSet -> changeSet.equals(pendingChangesetInfo.getChangeSet())));
                updateReportParameters.getChangesetInfo().setPendingChangesetCount(updateReportParameters.getChangesetInfo().getPendingChangesetCount() - failedChangeSetCount);
            }
            Scope.getCurrentScope().addMdcValue(MdcKey.DEPLOYMENT_OUTCOME_COUNT, String.valueOf(deployedChangeSetCount));
            Scope.getCurrentScope().addMdcValue(MdcKey.CHANGESETS_UPDATED, changesetsUpdated);
            // Now that the command is completed reset the generated sql in case we are running these changes in some chain
            // like during update-testing-rollback
            deployedChangeSets.forEach(changeSet -> changeSet.setGeneratedSql(new ArrayList<>()));
        }
        String deploymentOutcome = success ? MdcValue.COMMAND_SUCCESSFUL : MdcValue.COMMAND_FAILED;
        updateReportParameters.setSuccess(success);
        updateReportParameters.getOperationInfo().setOperationOutcome(deploymentOutcome);
        try (MdcObject deploymentOutcomeMdc = Scope.getCurrentScope().addMdcValue(MdcKey.DEPLOYMENT_OUTCOME, deploymentOutcome)) {
            Scope.getCurrentScope().getLog(getClass()).info(success ? successLog : failureLog);
        }
    }

    private List<ChangeSet> uniqueExceptionChangesets(List<ChangeSet> exceptionChangeSets, List<ChangeSet> failedChangeSets) {
        return exceptionChangeSets.stream().filter(e -> {
            return failedChangeSets.stream().noneMatch(f -> f.equals(e));
        }).collect(Collectors.toList());
    }

    @Beta
    public ChangeLogIterator getStandardChangelogIterator(CommandScope commandScope, Database database, Contexts contexts, LabelExpression labelExpression, DatabaseChangeLog changeLog) throws LiquibaseException {
        List<ChangeSetFilter> changesetFilters = this.getStandardChangelogIteratorFilters(database, contexts, labelExpression);
        return new ChangeLogIterator(changeLog, changesetFilters.toArray(new ChangeSetFilter[0]));
    }

    @Beta
    public ChangeLogIterator getStatusChangelogIterator(CommandScope commandScope, Database database, Contexts contexts, LabelExpression labelExpression, DatabaseChangeLog changeLog) throws LiquibaseException {
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
        FastCheckService fastCheck = Scope.getCurrentScope().getSingleton(FastCheckService.class);
        List<ChangeSetFilter> filters = this.getStandardChangelogIteratorFilters(database, contexts, labelExpression);

        if (fastCheck.isUpToDateFastCheck(filters, database, databaseChangeLog, contexts, labelExpression)) {
            Scope.getCurrentScope().getUI().sendMessage(DATABASE_UP_TO_DATE_MESSAGE);
            StatusVisitor statusVisitor = getStatusVisitor(commandScope, database, contexts, labelExpression, databaseChangeLog);
            UpdateSummaryEnum showSummary = getShowSummary(commandScope);
            UpdateSummaryOutputEnum showSummaryOutput = getShowSummaryOutput(commandScope);
            ShowSummaryUtil.showUpdateSummary(databaseChangeLog, showSummary, showSummaryOutput, statusVisitor, outputStream, null);
            return true;
        }
        return false;
    }

    public void setFastCheckEnabled(boolean fastCheckEnabled) {
        isFastCheckEnabled = fastCheckEnabled;
    }


    /**
     * Generate post update log messages
     * @param rowsAffected # of rows affected
     * @param exceptionChangeSets list of changesets that failed
     */
    public void postUpdateLog(int rowsAffected, List<ChangeSet> exceptionChangeSets) {
    }

    /**
     * Generic method called by all Update commands that actually apply the change to the database (ie !update-sql)
     * @param rowsAffected # of rows affected
     * @param exceptionChangeSets list of changesets that failed
     * @param messageWithRowCount message to display when rowsAffected > -1
     * @param messageWithoutRowCount message to display when rowsAffected == -1
     */
    protected void postUpdateLogForActualUpdate(int rowsAffected, List<ChangeSet> exceptionChangeSets, String messageWithRowCount, String messageWithoutRowCount) {
        if (exceptionChangeSets != null && !exceptionChangeSets.isEmpty()) {
            Scope.getCurrentScope().getUI().sendMessage("Errors encountered while deploying the following changesets: ");
            for (ChangeSet changeSet : exceptionChangeSets) {
                Scope.getCurrentScope().getUI().sendMessage("     " + changeSet.toString(false));
            }
            Scope.getCurrentScope().getUI().sendMessage("For more information use the --log-level flag.\n");
        }
        if (rowsAffected > -1) {
            Scope.getCurrentScope().getUI().sendMessage(String.format(messageWithRowCount, rowsAffected));
        } else {
            Scope.getCurrentScope().getUI().sendMessage(messageWithoutRowCount);
        }
    }


    /**
     * @deprecated Use {@link #postUpdateLog(int, List)} instead
     */
    @Deprecated
    public void postUpdateLog(int rowsAffected) {
        this.postUpdateLog(rowsAffected, Collections.emptyList());
    }

    protected void setDBLock(boolean locked) {
        isDBLocked.set(locked);
    }

    /**
     * Given a camel case command name array, format into a user-friendly command name string.
     * Ex: Given {"updateToTag"}, produces "Update To Tag"
     *
     * @param commandName the command name to reformat
     * @return the formatted command name
     */
    private String getFormattedCommandName(String[] commandName) {
        return Arrays.stream(commandName)
                .filter(Objects::nonNull)
                .map(camelCaseName -> StringUtil.join(StringUtil.splitCamelCase(camelCaseName), " "))
                .map(uc -> uc.equalsIgnoreCase("sql") ? "SQL" : StringUtils.capitalize(uc))
                .collect(Collectors.joining(" "));
    }
}
