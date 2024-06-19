package liquibase;

import liquibase.change.CheckSum;
import liquibase.changelog.*;
import liquibase.changelog.filter.*;
import liquibase.changelog.visitor.*;
import liquibase.command.CommandResults;
import liquibase.command.CommandScope;
import liquibase.command.core.*;
import liquibase.command.core.helpers.*;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.exception.CommandExecutionException;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.lockservice.DatabaseChangeLogLock;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.Logger;
import liquibase.logging.mdc.MdcKey;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.ChangeLogSerializer;
import liquibase.structure.DatabaseObject;
import liquibase.util.LoggingExecutorTextUtil;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.*;
import java.util.function.Supplier;

import static java.util.ResourceBundle.getBundle;

/**
 * Primary facade class for interacting with Liquibase. The methods are in their majority wrappers around CommandScope instances
 * that exists here to provide a simple and single point of entry for all Liquibase operations. If a method is not provided here,
 * it can be accessed by creating the correspoding CommandScope instance and executing it.
 * <p>
 * As of Liquibase 4.* some of the built-in command line, Ant, Maven, tests and other ways of running Liquibase are wrappers around
 * methods in this class, but this may change in future releases as we continue to refactor the codebase and move to CommandScope instances.
 *
 * @see <a href="https://contribute.liquibase.com/code/api/command-commandscope/"> CommandScope documentation</a>
 */
public class Liquibase implements AutoCloseable {

    private static final Logger LOG = Scope.getCurrentScope().getLog(Liquibase.class);
    private static final ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");
    public static final String MSG_COULD_NOT_RELEASE_LOCK = coreBundle.getString("could.not.release.lock");

    /**
     *  Returns the Database used by this Liquibase instance.
     */
    @Getter
    protected Database database;
    private DatabaseChangeLog databaseChangeLog;
    /**
     *  Return the change log file used by this Liquibase instance.
     */
    @Getter
    private String changeLogFile;
    @Setter
    private UpdateSummaryOutputEnum showSummaryOutput;
    @Setter
    private UpdateSummaryEnum showSummary;
    /**
     *  Return ResourceAccessor used by this Liquibase instance.
     */
    @Getter
    private final ResourceAccessor resourceAccessor;
    /**
     *  Returns the ChangeLogParameters container used by this Liquibase instance.
     */
    @Getter
    private final ChangeLogParameters changeLogParameters;
    @Setter
    private ChangeExecListener changeExecListener;
    @Getter
    private final DefaultChangeExecListener defaultChangeExecListener = new DefaultChangeExecListener();

    /**
     * Creates a Liquibase instance for a given DatabaseConnection. The Database instance used will be found with {@link DatabaseFactory#findCorrectDatabaseImplementation(liquibase.database.DatabaseConnection)}
     *
     * @see DatabaseConnection
     * @see Database
     * @see #Liquibase(String, liquibase.resource.ResourceAccessor, liquibase.database.Database)
     * @see ResourceAccessor
     */
    public Liquibase(String changeLogFile, ResourceAccessor resourceAccessor, DatabaseConnection conn)
            throws LiquibaseException {
        this(changeLogFile, resourceAccessor, DatabaseFactory.getInstance().findCorrectDatabaseImplementation(conn));
    }

    /**
     * Creates a Liquibase instance. The changeLogFile parameter must be a path that can be resolved by the passed
     * ResourceAccessor. If windows style path separators are used for the changeLogFile, they will be standardized to
     * unix style for better cross-system compatibility.
     *
     * @see DatabaseConnection
     * @see Database
     * @see ResourceAccessor
     */
    public Liquibase(String changeLogFile, ResourceAccessor resourceAccessor, Database database) {
        if (changeLogFile != null) {
            // Convert to STANDARD / if using absolute path on windows:
            this.changeLogFile = changeLogFile.replace('\\', '/');
        }

        this.resourceAccessor = resourceAccessor;
        this.changeLogParameters = new ChangeLogParameters(database);
        this.database = database;
    }

    public Liquibase(DatabaseChangeLog changeLog, ResourceAccessor resourceAccessor, Database database) {
        this.databaseChangeLog = changeLog;

        if (changeLog != null) {
            this.changeLogFile = changeLog.getPhysicalFilePath();
        }
        if (this.changeLogFile != null) {
            // Convert to STANDARD "/" if using an absolute path on Windows:
            changeLogFile = changeLogFile.replace('\\', '/');
        }
        this.resourceAccessor = resourceAccessor;
        this.database = database;
        this.changeLogParameters = new ChangeLogParameters(database);
    }

    /**
     * Return the log used by this Liquibase instance.
     */
    public Logger getLog() {
        return LOG;
    }

    /**
     * Convenience method for {@link #update(Contexts)} that runs in "no context mode".
     *
     * @see <a href="https://docs.liquibase.com/concepts/changelogs/attributes/contexts.html" target="_top">contexts</a> in documentation
     */
    public void update() throws LiquibaseException {
        this.update(new Contexts());
    }

    /**
     * Convenience method for {@link #update(Contexts)} that constructs the Context object from the passed string.
     * To run in "no context mode", pass a null or empty "".
     *
     * @see <a href="https://docs.liquibase.com/concepts/changelogs/attributes/contexts.html" target="_top">contexts</a> in documentation
     */
    public void update(String contexts) throws LiquibaseException {
        this.update(new Contexts(contexts));
    }

    /**
     * Executes Liquibase "update" logic which ensures that the configured {@link Database} is up to date according to
     * the configured changelog file. To run in "no context mode", pass a null or empty context object.
     *
     * @see <a href="https://docs.liquibase.com/concepts/changelogs/attributes/contexts.html" target="_top">contexts</a> in documentation
     */
    public void update(Contexts contexts) throws LiquibaseException {
        update(contexts, new LabelExpression());
    }

    /**
     * Executes Liquibase update with given contexts and label expression.
     *
     * @param contexts        the set of contexts to execute the update against. If empty or {@code null}, all contexts are used.
     * @param labelExpression the label expression to use during the update. If empty or {@code null}, no labels are used.
     * @throws LiquibaseException If an error occurs while executing the update.
     * @see <a href="https://docs.liquibase.com/concepts/changelogs/attributes/contexts.html" target="_top">Liquibase Contexts</a> in the Liquibase documentation
     * @see <a href="https://docs.liquibase.com/concepts/changelogs/attributes/labels.html" target="_top">Liquibase Labels</a> in the Liquibase documentation
     */
    public void update(Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        update(contexts, labelExpression, true);
    }

    /**
     * Updates the database schema to the latest version. This method performs a Liquibase update operation,
     * which can include applying changesets that modify the database schema.
     *
     * @param contexts the set of contexts to execute the update against. If empty or null, all contexts are used.
     * @param labelExpression the label expression to use during the update. If empty or null, no labels are used.
     * @param checkLiquibaseTables whether to check for Liquibase metadata tables before updating. If false, Liquibase will
     *                             assume that the metadata tables already exist and will not attempt to create them.
     * @throws LiquibaseException if an error occurs while updating the database schema.
     *
     * @see <a href="https://docs.liquibase.com/concepts/changelogs/attributes/contexts.html" target="_top">Liquibase Contexts</a>
     * @see <a href="https://docs.liquibase.com/concepts/changelogs/attributes/labels.html" target="_top">Liquibase Labels</a>
     */
    public void update(Contexts contexts, LabelExpression labelExpression, boolean checkLiquibaseTables) throws LiquibaseException {
        runInScope(() -> {
            CommandScope updateCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME);
            updateCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, getDatabase());
            updateCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_ARG, databaseChangeLog);
            updateCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changeLogFile);
            updateCommand.addArgumentValue(UpdateCommandStep.CONTEXTS_ARG, contexts != null ? contexts.toString() : null);
            updateCommand.addArgumentValue(UpdateCommandStep.LABEL_FILTER_ARG, labelExpression != null ? labelExpression.getOriginalString() : null);
            updateCommand.addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_ARG, changeExecListener);
            updateCommand.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY_OUTPUT, showSummaryOutput);
            updateCommand.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters);
            updateCommand.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY, showSummary);
            updateCommand.execute();
        });
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
     * @deprecated this method has been moved to {@link FastCheckService}, use that one instead.
     */
    @Deprecated
    protected boolean isUpToDateFastCheck(Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        return new UpdateCommandStep().isUpToDateFastCheck(null, database, databaseChangeLog, contexts, labelExpression);
    }

    public DatabaseChangeLog getDatabaseChangeLog() throws LiquibaseException {
        return getDatabaseChangeLog(false);
    }

    /**
     * @param shouldWarnOnMismatchedXsdVersion When set to true, a warning will be printed to the console if the XSD
     *                                         version used does not match the version of Liquibase. If "latest" is used
     *                                         as the XSD version, no warning is printed. If the changelog is not xml
     *                                         format, no warning is printed.
     */
    private DatabaseChangeLog getDatabaseChangeLog(boolean shouldWarnOnMismatchedXsdVersion) throws LiquibaseException {
        if (databaseChangeLog == null && changeLogFile != null) {
            ChangeLogParser parser = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor);
            if (parser instanceof XMLChangeLogSAXParser) {
                ((XMLChangeLogSAXParser) parser).setShouldWarnOnMismatchedXsdVersion(shouldWarnOnMismatchedXsdVersion);
            }
            databaseChangeLog = parser.parse(changeLogFile, changeLogParameters, resourceAccessor);
            Scope.getCurrentScope().getLog(Liquibase.class).info("Parsed changelog file '" + changeLogFile + "'");
            if (StringUtils.isNotEmpty(databaseChangeLog.getLogicalFilePath())) {
                Scope.getCurrentScope().addMdcValue(MdcKey.CHANGELOG_FILE, databaseChangeLog.getLogicalFilePath());
            } else {
                Scope.getCurrentScope().addMdcValue(MdcKey.CHANGELOG_FILE, changeLogFile);
            }
        }

        return databaseChangeLog;
    }

    /**
     * Return a ChangeLogIterator constructed with standard filters for processing the specified changelog.
     *
     * @param contexts          The contexts to filter for.
     * @param labelExpression   The labels to filter for.
     * @param changeLog         The changelog to process.
     * @return a ChangeLogIterator instance.
     * @throws DatabaseException if there is an error with the database.
     */
    protected ChangeLogIterator getStandardChangelogIterator(Contexts contexts, LabelExpression labelExpression,
                                                             DatabaseChangeLog changeLog) throws DatabaseException {
        return new ChangeLogIterator(changeLog,
                new ShouldRunChangeSetFilter(database),
                new ContextChangeSetFilter(contexts),
                new LabelChangeSetFilter(labelExpression),
                new DbmsChangeSetFilter(database),
                new IgnoreChangeSetFilter());
    }

    public void update(String contexts, Writer output) throws LiquibaseException {
        this.update(new Contexts(contexts), output);
    }

    public void update(Contexts contexts, Writer output) throws LiquibaseException {
        update(contexts, new LabelExpression(), output);
    }

    public void update(Contexts contexts, LabelExpression labelExpression, Writer output) throws LiquibaseException {
        update(contexts, labelExpression, output, true);
    }

    public void update(Contexts contexts, LabelExpression labelExpression, Writer output, boolean checkLiquibaseTables)
            throws LiquibaseException {
        runInScope(() -> {
            CommandScope updateCommand = new CommandScope(UpdateSqlCommandStep.COMMAND_NAME);
            updateCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, getDatabase());
            updateCommand.addArgumentValue(UpdateSqlCommandStep.CHANGELOG_FILE_ARG, changeLogFile);
            updateCommand.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_ARG, databaseChangeLog);
            updateCommand.addArgumentValue(UpdateSqlCommandStep.CONTEXTS_ARG, contexts != null ? contexts.toString() : null);
            updateCommand.addArgumentValue(UpdateSqlCommandStep.LABEL_FILTER_ARG, labelExpression != null ? labelExpression.getOriginalString() : null);
            updateCommand.addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_ARG, changeExecListener);
            updateCommand.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters);
            updateCommand.setOutput(WriterOutputStream.builder().setWriter(output).setCharset(GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()).get());
            updateCommand.execute();
        });
    }

    public void update(int changesToApply, String contexts) throws LiquibaseException {
        update(changesToApply, new Contexts(contexts), new LabelExpression());
    }

    /**
     * Updates the database schema with the specified number of changesets, within the given contexts and matching the
     * given label expression.
     *
     * @param changesToApply  the number of changesets to apply.
     * @param contexts        the contexts in which the changesets should be applied.
     * @param labelExpression the label expression used to filter the changesets.
     * @throws LiquibaseException if there is an error while updating the schema.
     */
    public void update(int changesToApply, Contexts contexts, LabelExpression labelExpression)
            throws LiquibaseException {
        runInScope(() -> {
            CommandScope updateCommand = new CommandScope(UpdateCountCommandStep.COMMAND_NAME);
            updateCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, getDatabase());
            updateCommand.addArgumentValue(UpdateCountCommandStep.CHANGELOG_FILE_ARG, changeLogFile);
            updateCommand.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_ARG, databaseChangeLog);
            updateCommand.addArgumentValue(UpdateCountCommandStep.CONTEXTS_ARG, contexts != null ? contexts.toString() : null);
            updateCommand.addArgumentValue(UpdateCountCommandStep.LABEL_FILTER_ARG, labelExpression != null ? labelExpression.getOriginalString() : null);
            updateCommand.addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_ARG, changeExecListener);
            updateCommand.addArgumentValue(UpdateCountCommandStep.COUNT_ARG, changesToApply);
            updateCommand.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY_OUTPUT, showSummaryOutput);
            updateCommand.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters);
            updateCommand.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY, showSummary);
            updateCommand.execute();
        });
    }

    public void update(String tag, String contexts) throws LiquibaseException {
        update(tag, new Contexts(contexts), new LabelExpression());
    }

    public void update(String tag, Contexts contexts) throws LiquibaseException {
        update(tag, contexts, new LabelExpression());
    }


    /**
     * Updates the database to a specified tag.
     *
     * @param tag             The tag to update the database to.
     * @param contexts        The contexts to execute in.
     * @param labelExpression The label expression to execute with.
     * @throws LiquibaseException if there is an error updating the database.
     */
    public void update(String tag, Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        if (tag == null) {
            update(contexts, labelExpression);
            return;
        }

        runInScope(() -> {
            CommandScope updateCommand = new CommandScope(UpdateToTagCommandStep.COMMAND_NAME);
            updateCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, getDatabase());
            updateCommand.addArgumentValue(UpdateToTagCommandStep.CHANGELOG_FILE_ARG, changeLogFile);
            updateCommand.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_ARG, databaseChangeLog);
            updateCommand.addArgumentValue(UpdateToTagCommandStep.CONTEXTS_ARG, contexts != null ? contexts.toString() : null);
            updateCommand.addArgumentValue(UpdateToTagCommandStep.LABEL_FILTER_ARG, labelExpression != null ? labelExpression.getOriginalString() : null);
            updateCommand.addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_ARG, changeExecListener);
            updateCommand.addArgumentValue(UpdateToTagCommandStep.TAG_ARG, tag);
            updateCommand.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY_OUTPUT, showSummaryOutput);
            updateCommand.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters);
            updateCommand.addArgumentValue(ShowSummaryArgument.SHOW_SUMMARY, showSummary);
            updateCommand.execute();
        });
    }

    public void update(int changesToApply, String contexts, Writer output) throws LiquibaseException {
        this.update(changesToApply, new Contexts(contexts), new LabelExpression(), output);
    }

    public void update(int changesToApply, Contexts contexts, LabelExpression labelExpression, Writer output) throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        runInScope(() -> {

            /* We have no other choice than to save the current Executer here. */
            @SuppressWarnings("squid:S1941")
            Executor oldTemplate = getAndReplaceJdbcExecutor(output);
            LoggingExecutorTextUtil.outputHeader("Update " + changesToApply + " Changesets Database Script", database, changeLogFile);

            update(changesToApply, contexts, labelExpression);

            flushOutputWriter(output);

            resetServices();
            Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("jdbc", database, oldTemplate);
        });

    }

    public void update(String tag, String contexts, Writer output) throws LiquibaseException {
        update(tag, new Contexts(contexts), new LabelExpression(), output);
    }

    public void update(String tag, Contexts contexts, Writer output) throws LiquibaseException {
        update(tag, contexts, new LabelExpression(), output);
    }

    public void update(String tag, Contexts contexts, LabelExpression labelExpression, Writer output)
            throws LiquibaseException {
        if (tag == null) {
            update(contexts, labelExpression, output);
            return;
        }
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        runInScope(() -> {

            /* We have no other choice than to save the current Executer here. */
            @SuppressWarnings("squid:S1941")
            Executor oldTemplate = getAndReplaceJdbcExecutor(output);

            LoggingExecutorTextUtil.outputHeader("Update to '" + tag + "' Database Script", database, changeLogFile);

            update(tag, contexts, labelExpression);

            flushOutputWriter(output);

            resetServices();
            Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("jdbc", database, oldTemplate);
        });
    }

    private void addCommandFiltersMdc(LabelExpression labelExpression, Contexts contexts) {
        String labelFilterMdc = labelExpression != null && labelExpression.getOriginalString() != null ? labelExpression.getOriginalString() : "";
        String contextFilterMdc = contexts != null ? contexts.toString() : "";
        Scope.getCurrentScope().addMdcValue(MdcKey.COMMAND_LABEL_FILTER, labelFilterMdc);
        Scope.getCurrentScope().addMdcValue(MdcKey.COMMAND_CONTEXT_FILTER, contextFilterMdc);
    }
    /**
     * @deprecated use {@link LoggingExecutorTextUtil#outputHeader(String, Database, String))}
     */
    @Deprecated
    public void outputHeader(String message) throws DatabaseException {
        LoggingExecutorTextUtil.outputHeader(message, database, changeLogFile);
    }

    // ---------- RollbackCountSql Family of methods
    public void rollback(int changesToRollback, String contexts, Writer output) throws LiquibaseException {
        rollback(changesToRollback, null, contexts, output);
    }

    public void rollback(int changesToRollback, Contexts contexts, Writer output) throws LiquibaseException {
        rollback(changesToRollback, null, contexts, output);
    }

    public void rollback(int changesToRollback, Contexts contexts, LabelExpression labelExpression, Writer output)
            throws LiquibaseException {
        rollback(changesToRollback, null, contexts, labelExpression, output);
    }

    public void rollback(int changesToRollback, String rollbackScript, String contexts, Writer output)
            throws LiquibaseException {
        rollback(changesToRollback, rollbackScript, new Contexts(contexts), output);
    }

    public void rollback(int changesToRollback, String rollbackScript, Contexts contexts, Writer output)
            throws LiquibaseException {
        rollback(changesToRollback, rollbackScript, contexts, new LabelExpression(), output);
    }

    public void rollback(int changesToRollback, String rollbackScript, Contexts contexts,
                         LabelExpression labelExpression, Writer output) throws LiquibaseException {
        runInScope(() ->
            new CommandScope(RollbackCountSqlCommandStep.COMMAND_NAME)
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, Liquibase.this.getDatabase())
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_ARG, databaseChangeLog)
                .addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null? contexts.toString() : null))
                .addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null))
                .addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_ARG, changeExecListener)
                .addArgumentValue(RollbackCountCommandStep.COUNT_ARG, changesToRollback)
                .addArgumentValue(AbstractRollbackCommandStep.ROLLBACK_SCRIPT_ARG, rollbackScript)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters)
                .setOutput(WriterOutputStream.builder().setWriter(output).setCharset(GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()).get())
                .execute()
        );
    }
    // ---------- End RollbackCountSql Family of methods

    // ---------- RollbackCount Family of methods
    public void rollback(int changesToRollback, String contexts) throws LiquibaseException {
        rollback(changesToRollback, null, contexts);
    }

    public void rollback(int changesToRollback, Contexts contexts, LabelExpression labelExpression)
            throws LiquibaseException {
        rollback(changesToRollback, null, contexts, labelExpression);
    }

    public void rollback(int changesToRollback, String rollbackScript, String contexts) throws LiquibaseException {
        rollback(changesToRollback, rollbackScript, new Contexts(contexts), new LabelExpression());
    }

    /**
     * Rolls back a specified number of changesets. The `changesToRollback` parameter specifies how many changesets to roll
     * back, and the `rollbackScript` parameter specifies the path to a custom SQL script to use for the rollback. The
     * `contexts` parameter specifies which contexts to include in the rollback, and the `labelExpression` parameter specifies
     * which labels to include in the rollback.
     *
     * @param changesToRollback the number of changesets to roll back
     * @param rollbackScript    the path to a custom SQL script to use for the rollback, or `null` to use Liquibase's built-in rollback functionality
     * @param contexts          the contexts to include in the rollback, or `null` to include all contexts
     * @param labelExpression   the labels to include in the rollback, or `null` to include all labels
     * @throws LiquibaseException if an error occurs while rolling back the changesets
     */
    public void rollback(int changesToRollback, String rollbackScript, Contexts contexts,
                         LabelExpression labelExpression) throws LiquibaseException {
        runInScope(() ->
            new CommandScope(RollbackCountCommandStep.COMMAND_NAME)
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, Liquibase.this.getDatabase())
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_ARG, databaseChangeLog)
                .addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null? contexts.toString() : null))
                .addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null))
                .addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_ARG, changeExecListener)
                .addArgumentValue(RollbackCountCommandStep.COUNT_ARG, changesToRollback)
                .addArgumentValue(AbstractRollbackCommandStep.ROLLBACK_SCRIPT_ARG, rollbackScript)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters)
                .execute()
        );
    }
    // ---------- End RollbackCount Family of methods

    // ---------- RollbackSQL Family of methods
    public void rollback(String tagToRollBackTo, String contexts, Writer output) throws LiquibaseException {
        rollback(tagToRollBackTo, null, contexts, output);
    }

    public void rollback(String tagToRollBackTo, Contexts contexts, Writer output) throws LiquibaseException {
        rollback(tagToRollBackTo, null, contexts, output);
    }

    public void rollback(String tagToRollBackTo, Contexts contexts, LabelExpression labelExpression, Writer output)
            throws LiquibaseException {
        rollback(tagToRollBackTo, null, contexts, labelExpression, output);
    }

    public void rollback(String tagToRollBackTo, String rollbackScript, String contexts, Writer output)
            throws LiquibaseException {
        rollback(tagToRollBackTo, rollbackScript, new Contexts(contexts), output);
    }

    public void rollback(String tagToRollBackTo, String rollbackScript, Contexts contexts, Writer output)
            throws LiquibaseException {
        rollback(tagToRollBackTo, rollbackScript, contexts, new LabelExpression(), output);
    }

    public void rollback(String tagToRollBackTo, String rollbackScript, Contexts contexts,
                         LabelExpression labelExpression, Writer output) throws LiquibaseException {
        runInScope(() ->
            new CommandScope(RollbackSqlCommandStep.COMMAND_NAME)
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, Liquibase.this.getDatabase())
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_ARG, databaseChangeLog)
                .addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null? contexts.toString() : null))
                .addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null))
                .addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_ARG, changeExecListener)
                .addArgumentValue(RollbackCommandStep.TAG_ARG, tagToRollBackTo)
                .addArgumentValue(AbstractRollbackCommandStep.ROLLBACK_SCRIPT_ARG, rollbackScript)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters)
                .setOutput(WriterOutputStream.builder().setWriter(output).setCharset(GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()).get())
                .execute()
        );
    }
    // ---------- End RollbackSQL Family of methods

    // ---------- Rollback (To Tag) Family of methods
    public void rollback(String tagToRollBackTo, String contexts) throws LiquibaseException {
        rollback(tagToRollBackTo, null, contexts);
    }

    public void rollback(String tagToRollBackTo, Contexts contexts) throws LiquibaseException {
        rollback(tagToRollBackTo, null, contexts);
    }

    public void rollback(String tagToRollBackTo, Contexts contexts, LabelExpression labelExpression)
            throws LiquibaseException {
        rollback(tagToRollBackTo, null, contexts, labelExpression);
    }

    public void rollback(String tagToRollBackTo, String rollbackScript, String contexts) throws LiquibaseException {
        rollback(tagToRollBackTo, rollbackScript, new Contexts(contexts));
    }

    public void rollback(String tagToRollBackTo, String rollbackScript, Contexts contexts) throws LiquibaseException {
        rollback(tagToRollBackTo, rollbackScript, contexts, new LabelExpression());
    }

    /**
     * Rolls back the database to a specific tag, using either a generated or user-defined rollback script.
     *
     * @param tagToRollBackTo the tag to which the database should be rolled back.
     * @param rollbackScript  an optional path to a user-defined rollback script. If null, Liquibase will generate the rollback SQL automatically.
     * @param contexts        a list of contexts to include when rolling back the database. May be null.
     * @param labelExpression a label expression to filter the change sets to rollback. May be null.
     * @throws LiquibaseException if there is a problem rolling back the database.
     */
    public void rollback(String tagToRollBackTo, String rollbackScript, Contexts contexts,
                         LabelExpression labelExpression) throws LiquibaseException {
        runInScope(() ->
            new CommandScope(RollbackCommandStep.COMMAND_NAME)
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, Liquibase.this.getDatabase())
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_ARG, databaseChangeLog)
                .addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null? contexts.toString() : null))
                .addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null))
                .addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_ARG, changeExecListener)
                .addArgumentValue(RollbackCommandStep.TAG_ARG, tagToRollBackTo)
                .addArgumentValue(AbstractRollbackCommandStep.ROLLBACK_SCRIPT_ARG, rollbackScript)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters)
                .execute()
        );
    }
    // ---------- End Rollback (To Tag) Family of methods

    // ---------- RollbackToDateSql Family of methods
    public void rollback(Date dateToRollBackTo, String contexts, Writer output) throws LiquibaseException {
        rollback(dateToRollBackTo, null, contexts, output);
    }

    public void rollback(Date dateToRollBackTo, String rollbackScript, String contexts, Writer output)
            throws LiquibaseException {
        rollback(dateToRollBackTo, new Contexts(contexts), new LabelExpression(), output);
    }

    public void rollback(Date dateToRollBackTo, Contexts contexts, LabelExpression labelExpression, Writer output)
            throws LiquibaseException {
        rollback(dateToRollBackTo, null, contexts, labelExpression, output);
    }

    public void rollback(Date dateToRollBackTo, String rollbackScript, Contexts contexts,
                         LabelExpression labelExpression, Writer output) throws LiquibaseException {
        runInScope(() ->
            new CommandScope(RollbackToDateSqlCommandStep.COMMAND_NAME)
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, Liquibase.this.getDatabase())
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_ARG, databaseChangeLog)
                .addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null? contexts.toString() : null))
                .addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null))
                .addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_ARG, changeExecListener)
                .addArgumentValue(RollbackToDateCommandStep.DATE_ARG, dateToRollBackTo)
                .addArgumentValue(AbstractRollbackCommandStep.ROLLBACK_SCRIPT_ARG, rollbackScript)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters)
                .setOutput(WriterOutputStream.builder().setWriter(output).setCharset(GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()).get())
                .execute()
        );
    }
    // ---------- End RollbackToDateSql Family of methods

    // ---------- RollbackToDate Family of methods
    public void rollback(Date dateToRollBackTo, String contexts) throws LiquibaseException {
        rollback(dateToRollBackTo, null, contexts);
    }

    public void rollback(Date dateToRollBackTo, Contexts contexts, LabelExpression labelExpression)
            throws LiquibaseException {
        rollback(dateToRollBackTo, null, contexts, labelExpression);
    }

    public void rollback(Date dateToRollBackTo, String rollbackScript, String contexts) throws LiquibaseException {
        rollback(dateToRollBackTo, new Contexts(contexts), new LabelExpression());
    }


    /**
     * Rolls back all changesets that were applied after the specified date. If a rollback script is provided,
     * the changesets are rolled back in reverse order until the script is reached. Otherwise, the changesets
     * are rolled back in reverse order until the rollback point is reached.
     *
     * @param dateToRollBackTo the date to roll back to
     * @param rollbackScript   the path to a SQL script to execute for the rollback (optional)
     * @param contexts         the contexts to execute the rollback in (optional)
     * @param labelExpression  the label expression to use for filtering change sets (optional)
     * @throws LiquibaseException if there was an error rolling back the changes
     */
    public void rollback(Date dateToRollBackTo, String rollbackScript, Contexts contexts,
                         LabelExpression labelExpression) throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);
        addCommandFiltersMdc(labelExpression, contexts);

        runInScope(() ->
            new CommandScope(RollbackToDateCommandStep.COMMAND_NAME)
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, Liquibase.this.getDatabase())
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_ARG, databaseChangeLog)
                .addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null? contexts.toString() : null))
                .addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null))
                .addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_ARG, changeExecListener)
                .addArgumentValue(RollbackToDateCommandStep.DATE_ARG, dateToRollBackTo)
                .addArgumentValue(AbstractRollbackCommandStep.ROLLBACK_SCRIPT_ARG, rollbackScript)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters)
                .execute()
        );

    }
    // ---------- End RollbackToDate Family of methods

    private Executor getAndReplaceJdbcExecutor(Writer output) {
        /* We have no other choice than to save the current Executor here. */
        @SuppressWarnings("squid:S1941")
        Executor oldTemplate = Scope.getCurrentScope().getSingleton(ExecutorService.class).getExecutor("jdbc", database);
        final LoggingExecutor loggingExecutor = new LoggingExecutor(oldTemplate, output, database);
        Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("logging", database, loggingExecutor);
        Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("jdbc", database, loggingExecutor);
        return oldTemplate;
    }

    public void changeLogSync(String contexts, Writer output) throws LiquibaseException {
        changeLogSync(new Contexts(contexts), new LabelExpression(), output);
    }

    public void changeLogSync(Contexts contexts, LabelExpression labelExpression, Writer output)
        throws LiquibaseException {

        doChangeLogSyncSql(null, contexts, labelExpression, output,
            () -> "SQL to add all changesets to database history table");
    }

    private void flushOutputWriter(Writer output) throws LiquibaseException {
        if (output == null) {
            return;
        }

        try {
            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }
    }

    public void changeLogSync(String contexts) throws LiquibaseException {
        changeLogSync(new Contexts(contexts), new LabelExpression());
    }

    /**
     * @deprecated use version with LabelExpression
     */
    @Deprecated
    public void changeLogSync(Contexts contexts) throws LiquibaseException {
        changeLogSync(contexts, new LabelExpression());
    }

    public void changeLogSync(Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        changeLogSync(null, contexts, labelExpression);
    }

    public void changeLogSync(String tag, String contexts) throws LiquibaseException {
        changeLogSync(tag, new Contexts(contexts), new LabelExpression());
    }

    /**
     * Synchronizes the changelog with the database up to a specified tag.
     *
     * @param tag             the tag up to which the changelog should be synchronized
     * @param contexts        the contexts to use for the synchronization
     * @param labelExpression the label expression to use for the synchronization
     * @throws LiquibaseException if an error occurs during the synchronization
     */
    public void changeLogSync(String tag, Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        String commandToRun = StringUtils.isEmpty(tag) ? ChangelogSyncCommandStep.COMMAND_NAME[0] : ChangelogSyncToTagCommandStep.COMMAND_NAME[0];
        runInScope(() -> {
            new CommandScope(commandToRun)
                    .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, Liquibase.this.getDatabase())
                    .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                    .addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null? contexts.toString() : null))
                    .addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null))
                    .addArgumentValue(ChangelogSyncToTagCommandStep.TAG_ARG, tag)
                    .execute();
        });

    }

    public void changeLogSync(String tag, String contexts, Writer output) throws LiquibaseException {
        changeLogSync(tag, new Contexts(contexts), new LabelExpression(), output);
    }

    public void changeLogSync(String tag, Contexts contexts, LabelExpression labelExpression, Writer output)
        throws LiquibaseException {
        doChangeLogSyncSql(tag, contexts, labelExpression, output,
            () -> "SQL to add changesets upto '" + tag + "' to database history table");
    }

    private void doChangeLogSyncSql(String tag, Contexts contexts, LabelExpression labelExpression, Writer output,
                                    Supplier<String> header) throws LiquibaseException {
        String commandToRun = StringUtils.isEmpty(tag) ? ChangelogSyncSqlCommandStep.COMMAND_NAME[0] : ChangelogSyncToTagSqlCommandStep.COMMAND_NAME[0];
        runInScope(() -> new CommandScope(commandToRun)
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, Liquibase.this.getDatabase())
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null? contexts.toString() : null))
                .addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null))
                .addArgumentValue(ChangelogSyncToTagSqlCommandStep.TAG_ARG, tag)
                .setOutput(WriterOutputStream.builder().setWriter(output).setCharset(GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()).get())
                .execute());
    }

    public void markNextChangeSetRan(String contexts, Writer output) throws LiquibaseException {
        markNextChangeSetRan(new Contexts(contexts), new LabelExpression(), output);
    }

    public void markNextChangeSetRan(Contexts contexts, LabelExpression labelExpression, Writer output)
            throws LiquibaseException {
        runInScope(() -> new CommandScope(MarkNextChangesetRanSqlCommandStep.COMMAND_NAME)
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, getDatabase())
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters)
                .addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null ? contexts.toString() : null))
                .addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null))
                .setOutput(WriterOutputStream.builder().setWriter(output).setCharset(GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()).get())
                .execute());
    }

    public void markNextChangeSetRan(String contexts) throws LiquibaseException {
        markNextChangeSetRan(new Contexts(contexts), new LabelExpression());
    }

    public void markNextChangeSetRan(Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        runInScope(() -> new CommandScope(MarkNextChangesetRanCommandStep.COMMAND_NAME)
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, getDatabase())
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters)
                .addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null ? contexts.toString() : null))
                .addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null))
                .execute());
    }

    public void futureRollbackSQL(String contexts, Writer output) throws LiquibaseException {
        futureRollbackSQL(null, contexts, output, true);
    }

    public void futureRollbackSQL(Writer output) throws LiquibaseException {
        futureRollbackSQL(null, null, new Contexts(), new LabelExpression(), output);
    }

    public void futureRollbackSQL(String contexts, Writer output, boolean checkLiquibaseTables)
            throws LiquibaseException {
        futureRollbackSQL(null, contexts, output, checkLiquibaseTables);
    }

    public void futureRollbackSQL(Integer count, String contexts, Writer output) throws LiquibaseException {
        futureRollbackSQL(count, new Contexts(contexts), new LabelExpression(), output, true);
    }

    public void futureRollbackSQL(Contexts contexts, LabelExpression labelExpression, Writer output)
            throws LiquibaseException {
        futureRollbackSQL(null, null, contexts, labelExpression, output);
    }

    public void futureRollbackSQL(Integer count, String contexts, Writer output, boolean checkLiquibaseTables)
            throws LiquibaseException {
        futureRollbackSQL(count, new Contexts(contexts), new LabelExpression(), output, checkLiquibaseTables);
    }

    public void futureRollbackSQL(Integer count, Contexts contexts, LabelExpression labelExpression, Writer output)
            throws LiquibaseException {
        futureRollbackSQL(count, contexts, labelExpression, output, true);
    }

    public void futureRollbackSQL(Integer count, Contexts contexts, LabelExpression labelExpression, Writer output,
                                  boolean checkLiquibaseTables) throws LiquibaseException {
        futureRollbackSQL(count, null, contexts, labelExpression, output);
    }

    public void futureRollbackSQL(String tag, Contexts contexts, LabelExpression labelExpression, Writer output)
            throws LiquibaseException {
        futureRollbackSQL(null, tag, contexts, labelExpression, output);
    }

    protected void futureRollbackSQL(Integer count, String tag, Contexts contexts, LabelExpression labelExpression,
                                     Writer output) throws LiquibaseException {
        futureRollbackSQL(count, tag, contexts, labelExpression, output, true);
    }

    protected void futureRollbackSQL(Integer count, String tag, Contexts contexts, LabelExpression labelExpression,
                                     Writer output, boolean checkLiquibaseTables) throws LiquibaseException {
        CommandScope commandScope;
        if ((count == null) && (tag == null)) {
            commandScope = new CommandScope(FutureRollbackSqlCommandStep.COMMAND_NAME);
        } else if (count != null) {
            commandScope = new CommandScope(FutureRollbackCountSqlCommandStep.COMMAND_NAME);
            commandScope.addArgumentValue(FutureRollbackCountSqlCommandStep.COUNT_ARG, count);
        } else {
            commandScope = new CommandScope(FutureRollbackFromTagSqlCommandStep.COMMAND_NAME);
            commandScope.addArgumentValue(FutureRollbackFromTagSqlCommandStep.TAG_ARG, tag);
        }

        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, getDatabase())
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters)
                .addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null ? contexts.toString() : null))
                .addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null))
                .addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_ARG, changeExecListener)
                .setOutput(new WriterOutputStream(output, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()));

        runInScope(commandScope::execute);
    }

    protected void resetServices() {
        LockServiceFactory.getInstance().resetAll();
        Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).resetAll();
        Scope.getCurrentScope().getSingleton(ExecutorService.class).reset();
    }

    /**
     * Drops all database objects in the default schema.
     */
    public final void dropAll() throws DatabaseException {
        dropAll(new CatalogAndSchema(getDatabase().getDefaultCatalogName(), getDatabase().getDefaultSchemaName()));
    }

    /**
     * Drops all database objects in the default schema.
     * @param dropDbclhistory If true, the database changelog history table will be dropped. Requires pro license.
     */
    public final void dropAll(Boolean dropDbclhistory) throws DatabaseException {
        dropAll(dropDbclhistory, new CatalogAndSchema(getDatabase().getDefaultCatalogName(), getDatabase().getDefaultSchemaName()));
    }

    /**
     * Drops all database objects in the passed schema(s).
     */
    public final void dropAll(CatalogAndSchema... schemas) throws DatabaseException {
        dropAll(null, schemas);
    }

    /**
     * Drops all database objects in the passed schema(s).
     * @param dropDbclhistory If true, the database changelog history table will be dropped. Requires pro license.
     */
    public final void dropAll(Boolean dropDbclhistory, CatalogAndSchema... schemas) throws DatabaseException {

        try {
            CommandScope dropAll = new CommandScope("dropAll")
                    .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, Liquibase.this.getDatabase())
                    .addArgumentValue(DropAllCommandStep.CATALOG_AND_SCHEMAS_ARG, schemas)
                    .addArgumentValue("dropDbclhistory", dropDbclhistory);

            dropAllThrowingDatabaseException(dropAll);
        } catch (LiquibaseException e) {
            throw (e instanceof DatabaseException) ? (DatabaseException) e : new DatabaseException(e);
        }
    }

    private static void dropAllThrowingDatabaseException(CommandScope dropAll) throws DatabaseException {
        try {
            dropAll.execute();
        } catch (CommandExecutionException e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * 'Tags' the database for future rollback
     */
    public void tag(String tagString) throws LiquibaseException {
        new CommandScope("tag")
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database)
                .addArgumentValue(TagCommandStep.TAG_ARG, tagString)
                .execute();
    }

    /**
     *  Verifies if a given tag exist in the database
     */
    public boolean tagExists(String tagString) throws LiquibaseException {
        CommandResults commandResults = new CommandScope("tagExists")
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database)
                .addArgumentValue(TagExistsCommandStep.TAG_ARG, tagString)
                .execute();
        return commandResults.getResult(TagExistsCommandStep.TAG_EXISTS_RESULT);
    }

    public void updateTestingRollback(String contexts) throws LiquibaseException {
        updateTestingRollback(new Contexts(contexts), new LabelExpression());
    }

    public void updateTestingRollback(Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        updateTestingRollback(null, contexts, labelExpression);

    }

    public void updateTestingRollback(String tag, Contexts contexts, LabelExpression labelExpression)
            throws LiquibaseException {
        runInScope(() -> {
            CommandScope updateTestingRollback = new CommandScope(UpdateTestingRollbackCommandStep.COMMAND_NAME);
            updateTestingRollback.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, getDatabase());
            updateTestingRollback.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters);
            updateTestingRollback.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile);
            updateTestingRollback.addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null? contexts.toString() : null));
            updateTestingRollback.addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null));
            updateTestingRollback.addArgumentValue(UpdateTestingRollbackCommandStep.TAG_ARG, tag);
            updateTestingRollback.addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_ARG, changeExecListener);
            updateTestingRollback.execute();
        });
    }

    public void checkLiquibaseTables(boolean updateExistingNullChecksums, DatabaseChangeLog databaseChangeLog,
                                     Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        ChangeLogHistoryService changeLogHistoryService =
            Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(getDatabase());
        changeLogHistoryService.init();
        if (updateExistingNullChecksums) {
            changeLogHistoryService.upgradeChecksums(databaseChangeLog, contexts, labelExpression);
        }
        LockServiceFactory.getInstance().getLockService(getDatabase()).init();
    }

    /**
     * Returns true if it is "save" to migrate the database.
     * Currently, "safe" is defined as running in an output-sql mode or against a database on localhost.
     * It is fine to run Liquibase against a "non-safe" database, the method is mainly used to determine if the user
     * should be prompted before continuing.
     */
    public boolean isSafeToRunUpdate() throws DatabaseException {
        return getDatabase().isSafeToRunUpdate();
    }

    /**
     * Display change log lock information.
     */
    public DatabaseChangeLogLock[] listLocks() throws LiquibaseException {
        return ListLocksCommandStep.listLocks(database);
    }

    public void reportLocks(PrintStream out) throws LiquibaseException {
        runInScope(() -> {
            CommandScope listLocksCommand = new CommandScope(ListLocksCommandStep.COMMAND_NAME);
            listLocksCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, getDatabase());
            listLocksCommand.setOutput(out);
            listLocksCommand.execute();
        });
    }

    public void forceReleaseLocks() throws LiquibaseException {
        new CommandScope(ReleaseLocksCommandStep.COMMAND_NAME[0])
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, getDatabase())
                .execute();
    }

    /**
     * @deprecated use version with LabelExpression
     */
    @Deprecated
    public List<ChangeSet> listUnrunChangeSets(Contexts contexts) throws LiquibaseException {
        return listUnrunChangeSets(contexts, new LabelExpression());
    }

    public List<ChangeSet> listUnrunChangeSets(Contexts contexts, LabelExpression labels) throws LiquibaseException {
        return listUnrunChangeSets(contexts, labels, true);
    }

    public List<ChangeSet> listUnrunChangeSets(Contexts contexts, LabelExpression labels, boolean checkLiquibaseTables) throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labels);

        ListVisitor visitor = (ListVisitor) visitInScope(contexts, labels, checkLiquibaseTables, new ListVisitor());
        return visitor.getSeenChangeSets();
    }

    /**
     * @deprecated use version with LabelExpression
     */
    @Deprecated
    public List<ChangeSetStatus> getChangeSetStatuses(Contexts contexts) throws LiquibaseException {
        return getChangeSetStatuses(contexts, new LabelExpression());
    }

    public List<ChangeSetStatus> getChangeSetStatuses(Contexts contexts, LabelExpression labelExpression)
            throws LiquibaseException {
        return getChangeSetStatuses(contexts, labelExpression, true);
    }

    /**
     * Returns the ChangeSetStatuses of all changesets in the change log file and history in the order they
     * would be ran.
     */
    public List<ChangeSetStatus> getChangeSetStatuses(Contexts contexts, LabelExpression labelExpression,
                                                      boolean checkLiquibaseTables) throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);
        StatusVisitor visitor = new StatusVisitor(database);

        visitInScope(contexts, labelExpression, checkLiquibaseTables, visitor);
        return visitor.getStatuses();
    }

    /**
     * Populate a Visitor with the statuses of all changesets in the change log file and history in the order they appear
     */
    private ChangeSetVisitor visitInScope(Contexts contexts, LabelExpression labelExpression, boolean checkLiquibaseTables, ChangeSetVisitor visitor) throws LiquibaseException {
        runInScope(() -> {
            DatabaseChangeLog changeLog = getDatabaseChangeLog();
            if (checkLiquibaseTables) {
                checkLiquibaseTables(false, changeLog, contexts, labelExpression);
            }
            changeLog.validate(database, contexts, labelExpression);
            ChangeLogIterator logIterator = getStandardChangelogIterator(contexts, labelExpression, changeLog);
            logIterator.run(visitor, new RuntimeEnvironment(database, contexts, labelExpression));
        });
        return visitor;
    }

    public void reportStatus(boolean verbose, String contexts, Writer out) throws LiquibaseException {
        reportStatus(verbose, new Contexts(contexts), new LabelExpression(), out);
    }

    public void reportStatus(boolean verbose, Contexts contexts, Writer out) throws LiquibaseException {
        reportStatus(verbose, contexts, new LabelExpression(), out);
    }

    public void reportStatus(boolean verbose, Contexts contexts, LabelExpression labels, Writer out)
            throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labels);
        runInScope(() -> {
            CommandScope statusCommand = new CommandScope(StatusCommandStep.COMMAND_NAME);
            statusCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, getDatabase());
            statusCommand.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters);
            statusCommand.addArgumentValue(StatusCommandStep.VERBOSE_ARG, verbose);
            statusCommand.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile);
            statusCommand.setOutput(WriterOutputStream.builder().setWriter(out).setCharset(GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()).get());
            statusCommand.execute();
        });
    }

    public Collection<RanChangeSet> listUnexpectedChangeSets(String contexts) throws LiquibaseException {
        return listUnexpectedChangeSets(new Contexts(contexts), new LabelExpression());
    }

    public Collection<RanChangeSet> listUnexpectedChangeSets(Contexts contexts, LabelExpression labelExpression)
            throws LiquibaseException {
        return UnexpectedChangesetsCommandStep.listUnexpectedChangeSets(getDatabase(), getDatabaseChangeLog(), contexts, labelExpression);
    }

    public void reportUnexpectedChangeSets(boolean verbose, String contexts, Writer out) throws LiquibaseException {
        reportUnexpectedChangeSets(verbose, new Contexts(contexts), new LabelExpression(), out);
    }

    public void reportUnexpectedChangeSets(boolean verbose, Contexts contexts, LabelExpression labelExpression,
                                           Writer out) throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);
        runInScope(() -> {
            CommandScope unexpectedChangesetsCommand = new CommandScope(UnexpectedChangesetsCommandStep.COMMAND_NAME);
            unexpectedChangesetsCommand.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, getDatabase());
            unexpectedChangesetsCommand.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters);
            unexpectedChangesetsCommand.addArgumentValue(UnexpectedChangesetsCommandStep.VERBOSE_ARG, verbose);
            unexpectedChangesetsCommand.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile);
            unexpectedChangesetsCommand.addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null? contexts.toString() : null));
            unexpectedChangesetsCommand.addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null));
            unexpectedChangesetsCommand.setOutput(WriterOutputStream.builder().setWriter(out).setCharset(GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()).get());
            unexpectedChangesetsCommand.execute();
        });
    }

    /**
     * Sets checksums to null, so they will be repopulated next run
     */
    public void clearCheckSums() throws LiquibaseException {
       new CommandScope(ClearChecksumsCommandStep.COMMAND_NAME)
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database)
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, database.getConnection().getURL())
                .execute();
    }

    /**
     * Calculate the checksum for a given identifier
     */
    public final CheckSum calculateCheckSum(final String changeSetIdentifier) throws LiquibaseException {
        String[] changeSetAttributes = changeSetIdentifier.split("::");
        //validate changeSet parameters and return an error or removed/ignore any other '::' occurrence when processing either a path, id or author.
        return this.calculateCheckSum(changeSetAttributes[0], changeSetAttributes[1], changeSetAttributes[2]);
    }

    /**
     * Calculate the checksum for a given changeset specified by path, changeset id and author
     */
    public CheckSum calculateCheckSum(final String changeSetPath, final String changeSetId, final String changeSetAuthor)
            throws LiquibaseException {
        CommandResults commandResults = new CommandScope("calculateChecksum")
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database)
                .addArgumentValue(CalculateChecksumCommandStep.CHANGESET_PATH_ARG, changeSetPath)
                .addArgumentValue(CalculateChecksumCommandStep.CHANGESET_ID_ARG, changeSetId)
                .addArgumentValue(CalculateChecksumCommandStep.CHANGESET_AUTHOR_ARG, changeSetAuthor)
                .addArgumentValue(CalculateChecksumCommandStep.CHANGELOG_FILE_ARG, this.changeLogFile)
                .execute();
        return commandResults.getResult(CalculateChecksumCommandStep.CHECKSUM_RESULT);
    }

    public void generateDocumentation(String outputDirectory) throws LiquibaseException {
        // call without context
        generateDocumentation(outputDirectory, new Contexts(), new LabelExpression(), new CatalogAndSchema(null, null));
    }

    public void generateDocumentation(String outputDirectory, String contexts) throws LiquibaseException {
        generateDocumentation(outputDirectory, new Contexts(contexts), new LabelExpression(), new CatalogAndSchema(null, null));
    }

    public void generateDocumentation(String outputDirectory, String contexts, CatalogAndSchema... schemaList) throws LiquibaseException {
        generateDocumentation(outputDirectory, new Contexts(contexts), new LabelExpression(), schemaList);
    }

    public void generateDocumentation(String outputDirectory, Contexts contexts,
                                      LabelExpression labelExpression, CatalogAndSchema... schemaList) throws LiquibaseException {
        runInScope(() -> new CommandScope(DbDocCommandStep.COMMAND_NAME[0])
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, Liquibase.this.getDatabase())
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters)
                .addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null ? contexts.toString() : null))
                .addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null))
                .addArgumentValue(DbDocCommandStep.CATALOG_AND_SCHEMAS_ARG, schemaList)
                .addArgumentValue(DbDocCommandStep.OUTPUT_DIRECTORY_ARG, outputDirectory)
                .execute());
    }

    public DiffResult diff(Database referenceDatabase, Database targetDatabase, CompareControl compareControl)
            throws LiquibaseException {
        return DiffGeneratorFactory.getInstance().compare(referenceDatabase, targetDatabase, compareControl);
    }

    /**
     * Checks changelogs for bad MD5Sums and preconditions before attempting a migration
     */
    public void validate() throws LiquibaseException {
        runInScope(() ->
            new CommandScope("validate")
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, database)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters)
                .execute()
        );
    }

    public void setChangeLogParameter(String key, Object value) {
        this.changeLogParameters.set(key, value);
    }

    @SafeVarargs
    public final void generateChangeLog(CatalogAndSchema catalogAndSchema, DiffToChangeLog changeLogWriter,
                                        PrintStream outputStream, Class<? extends DatabaseObject>... snapshotTypes)
            throws DatabaseException, CommandExecutionException {
        generateChangeLog(catalogAndSchema, changeLogWriter, outputStream, null, snapshotTypes);
    }

    @SafeVarargs
    public final void generateChangeLog(CatalogAndSchema catalogAndSchema, DiffToChangeLog changeLogWriter,
                                        PrintStream outputStream, ChangeLogSerializer changeLogSerializer,
                                        Class<? extends DatabaseObject>... snapshotTypes)
            throws DatabaseException, CommandExecutionException {
        Set<Class<? extends DatabaseObject>> finalCompareTypes = null;
        if ((snapshotTypes != null) && (snapshotTypes.length > 0)) {
            finalCompareTypes = new HashSet<>(Arrays.asList(snapshotTypes));
        }
        CompareControl compareControl = new CompareControl(new CompareControl.SchemaComparison[]{
                new CompareControl.SchemaComparison(catalogAndSchema, catalogAndSchema)
        }, finalCompareTypes);

        new CommandScope(GenerateChangelogCommandStep.COMMAND_NAME[0])
                .addArgumentValue(GenerateChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(PreCompareCommandStep.COMPARE_CONTROL_ARG, compareControl)
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, getDatabase())
                .addArgumentValue(PreCompareCommandStep.SNAPSHOT_TYPES_ARG, snapshotTypes)
                .setOutput(outputStream)
                .execute();
    }

    private void runInScope(Scope.ScopedRunner<?> scopedRunner) throws LiquibaseException {
        Map<String, Object> scopeObjects = new HashMap<>();
        scopeObjects.put(Scope.Attr.database.name(), getDatabase());
        scopeObjects.put(Scope.Attr.resourceAccessor.name(), getResourceAccessor());

        try {
            Scope.child(scopeObjects, scopedRunner);
        } catch (Exception e) {
            throw e instanceof LiquibaseException ? (LiquibaseException) e : new LiquibaseException(e);
        }
    }

    @Override
    public void close() throws LiquibaseException {
        if (database != null) {
            database.close();
        }
    }
}
