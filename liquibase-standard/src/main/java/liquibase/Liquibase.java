package liquibase;

import liquibase.change.CheckSum;
import liquibase.changelog.*;
import liquibase.changelog.filter.*;
import liquibase.changelog.visitor.*;
import liquibase.command.CommandResults;
import liquibase.command.CommandScope;
import liquibase.command.core.*;
import liquibase.command.core.helpers.ChangeExecListenerCommandStep;
import liquibase.command.core.helpers.DatabaseChangelogCommandStep;
import liquibase.command.core.helpers.DbUrlConnectionCommandStep;
import liquibase.command.core.helpers.PreCompareCommandStep;
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
import liquibase.io.WriterOutputStream;
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
import liquibase.util.StringUtil;

import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.util.*;
import java.util.function.Supplier;

import static java.util.ResourceBundle.getBundle;

/**
 * Primary facade class for interacting with Liquibase.
 * The built in command line, Ant, Maven and other ways of running Liquibase are wrappers around methods in this class.
 */
public class Liquibase implements AutoCloseable {

    private static final Logger LOG = Scope.getCurrentScope().getLog(Liquibase.class);
    private static final ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");
    public static final String MSG_COULD_NOT_RELEASE_LOCK = coreBundle.getString("could.not.release.lock");

    protected Database database;
    private DatabaseChangeLog databaseChangeLog;
    private String changeLogFile;
    private final ResourceAccessor resourceAccessor;
    private final ChangeLogParameters changeLogParameters;
    private ChangeExecListener changeExecListener;
    private final DefaultChangeExecListener defaultChangeExecListener = new DefaultChangeExecListener();
    private final Map<String, Boolean> upToDateFastCheck = new HashMap<>();

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
     * Return the change log file used by this Liquibase instance.
     */
    public String getChangeLogFile() {
        return changeLogFile;
    }

    /**
     * Return the log used by this Liquibase instance.
     */
    public Logger getLog() {
        return LOG;
    }

    /**
     * Returns the ChangeLogParameters container used by this Liquibase instance.
     */
    public ChangeLogParameters getChangeLogParameters() {
        return changeLogParameters;
    }

    /**
     * Returns the Database used by this Liquibase instance.
     */
    public Database getDatabase() {
        return database;
    }

    /**
     * Return ResourceAccessor used by this Liquibase instance.
     */
    public ResourceAccessor getResourceAccessor() {
        return resourceAccessor;
    }

    /**
     * Convenience method for {@link #update(Contexts)} that runs in "no context mode".
     *
     * @see <a href="https://docs.liquibase.com/concepts/changelogs/attributes/contexts.html" target="_top">contexts</a> in documentation
     */
    @Deprecated
    public void update() throws LiquibaseException {
        this.update(new Contexts());
    }

    /**
     * Convenience method for {@link #update(Contexts)} that constructs the Context object from the passed string.
     * To run in "no context mode", pass a null or empty "".
     *
     * @see <a href="https://docs.liquibase.com/concepts/changelogs/attributes/contexts.html" target="_top">contexts</a> in documentation
     */
    @Deprecated
    public void update(String contexts) throws LiquibaseException {
        this.update(new Contexts(contexts));
    }

    /**
     * Executes Liquibase "update" logic which ensures that the configured {@link Database} is up to date according to
     * the configured changelog file. To run in "no context mode", pass a null or empty context object.
     *
     * @see <a href="https://docs.liquibase.com/concepts/changelogs/attributes/contexts.html" target="_top">contexts</a> in documentation
     */
    @Deprecated
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
    @Deprecated
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
    @Deprecated
    public void update(Contexts contexts, LabelExpression labelExpression, boolean checkLiquibaseTables) throws LiquibaseException {
        runInScope(() -> {
            CommandScope updateCommand = new CommandScope(UpdateCommandStep.COMMAND_NAME);
            updateCommand.addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, getDatabase());
            updateCommand.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, changeLogFile);
            updateCommand.addArgumentValue(UpdateCommandStep.CONTEXTS_ARG, contexts != null ? contexts.toString() : null);
            updateCommand.addArgumentValue(UpdateCommandStep.LABEL_FILTER_ARG, labelExpression != null ? labelExpression.getOriginalString() : null);
            updateCommand.addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_ARG, changeExecListener);
            updateCommand.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters);
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
     * @deprecated this method has been moved to {@link AbstractUpdateCommandStep}, use that one instead.
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
            if (StringUtil.isNotEmpty(databaseChangeLog.getLogicalFilePath())) {
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
            updateCommand.addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, getDatabase());
            updateCommand.addArgumentValue(UpdateSqlCommandStep.CHANGELOG_FILE_ARG, changeLogFile);
            updateCommand.addArgumentValue(UpdateSqlCommandStep.CONTEXTS_ARG, contexts != null ? contexts.toString() : null);
            updateCommand.addArgumentValue(UpdateSqlCommandStep.LABEL_FILTER_ARG, labelExpression != null ? labelExpression.getOriginalString() : null);
            updateCommand.addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_ARG, changeExecListener);
            updateCommand.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters);
            updateCommand.setOutput(new WriterOutputStream(output, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()));
            updateCommand.execute();
        });
    }

    @Deprecated
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
    @Deprecated
    public void update(int changesToApply, Contexts contexts, LabelExpression labelExpression)
            throws LiquibaseException {
        runInScope(() -> {
            CommandScope updateCommand = new CommandScope(UpdateCountCommandStep.COMMAND_NAME);
            updateCommand.addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, getDatabase());
            updateCommand.addArgumentValue(UpdateCountCommandStep.CHANGELOG_FILE_ARG, changeLogFile);
            updateCommand.addArgumentValue(UpdateCountCommandStep.CONTEXTS_ARG, contexts != null ? contexts.toString() : null);
            updateCommand.addArgumentValue(UpdateCountCommandStep.LABEL_FILTER_ARG, labelExpression != null ? labelExpression.getOriginalString() : null);
            updateCommand.addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_ARG, changeExecListener);
            updateCommand.addArgumentValue(UpdateCountCommandStep.COUNT_ARG, changesToApply);
            updateCommand.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters);
            updateCommand.execute();
        });
    }

    @Deprecated
    public void update(String tag, String contexts) throws LiquibaseException {
        update(tag, new Contexts(contexts), new LabelExpression());
    }

    @Deprecated
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
    @Deprecated
    public void update(String tag, Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        if (tag == null) {
            update(contexts, labelExpression);
            return;
        }

        runInScope(() -> {
            CommandScope updateCommand = new CommandScope(UpdateToTagCommandStep.COMMAND_NAME);
            updateCommand.addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, getDatabase());
            updateCommand.addArgumentValue(UpdateToTagCommandStep.CHANGELOG_FILE_ARG, changeLogFile);
            updateCommand.addArgumentValue(UpdateToTagCommandStep.CONTEXTS_ARG, contexts != null ? contexts.toString() : null);
            updateCommand.addArgumentValue(UpdateToTagCommandStep.LABEL_FILTER_ARG, labelExpression != null ? labelExpression.getOriginalString() : null);
            updateCommand.addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_ARG, changeExecListener);
            updateCommand.addArgumentValue(UpdateToTagCommandStep.TAG_ARG, tag);
            updateCommand.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters);
            updateCommand.execute();
        });
    }

    @Deprecated
    public void update(int changesToApply, String contexts, Writer output) throws LiquibaseException {
        this.update(changesToApply, new Contexts(contexts), new LabelExpression(), output);
    }

    @Deprecated
    public void update(int changesToApply, Contexts contexts, LabelExpression labelExpression, Writer output) throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);

        runInScope(() -> {

            /* We have no other choice than to save the current Executer here. */
            @SuppressWarnings("squid:S1941")
            Executor oldTemplate = getAndReplaceJdbcExecutor(output);
            outputHeader("Update " + changesToApply + " Changesets Database Script");

            update(changesToApply, contexts, labelExpression);

            flushOutputWriter(output);

            resetServices();
            Scope.getCurrentScope().getSingleton(ExecutorService.class).setExecutor("jdbc", database, oldTemplate);
        });

    }

    @Deprecated
    public void update(String tag, String contexts, Writer output) throws LiquibaseException {
        update(tag, new Contexts(contexts), new LabelExpression(), output);
    }

    @Deprecated
    public void update(String tag, Contexts contexts, Writer output) throws LiquibaseException {
        update(tag, contexts, new LabelExpression(), output);
    }

    @Deprecated
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

            outputHeader("Update to '" + tag + "' Database Script");

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
        new CommandScope(RollbackCountSqlCommandStep.COMMAND_NAME)
                .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, Liquibase.this.getDatabase())
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null? contexts.toString() : null))
                .addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null))
                .addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_ARG, changeExecListener)
                .addArgumentValue(RollbackCountCommandStep.COUNT_ARG, changesToRollback)
                .addArgumentValue(AbstractRollbackCommandStep.ROLLBACK_SCRIPT_ARG, rollbackScript)
                .setOutput(new WriterOutputStream(output, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()))
                .execute();
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
        new CommandScope(RollbackCountCommandStep.COMMAND_NAME)
                .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, Liquibase.this.getDatabase())
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null? contexts.toString() : null))
                .addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null))
                .addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_ARG, changeExecListener)
                .addArgumentValue(RollbackCountCommandStep.COUNT_ARG, changesToRollback)
                .addArgumentValue(AbstractRollbackCommandStep.ROLLBACK_SCRIPT_ARG, rollbackScript)
                .execute();
    }
    // ---------- End RollbackCount Family of methods

    // ---------- RollbackSQL Family of methods
    @Deprecated
    public void rollback(String tagToRollBackTo, String contexts, Writer output) throws LiquibaseException {
        rollback(tagToRollBackTo, null, contexts, output);
    }

    @Deprecated
    public void rollback(String tagToRollBackTo, Contexts contexts, Writer output) throws LiquibaseException {
        rollback(tagToRollBackTo, null, contexts, output);
    }

    @Deprecated
    public void rollback(String tagToRollBackTo, Contexts contexts, LabelExpression labelExpression, Writer output)
            throws LiquibaseException {
        rollback(tagToRollBackTo, null, contexts, labelExpression, output);
    }

    @Deprecated
    public void rollback(String tagToRollBackTo, String rollbackScript, String contexts, Writer output)
            throws LiquibaseException {
        rollback(tagToRollBackTo, rollbackScript, new Contexts(contexts), output);
    }

    @Deprecated
    public void rollback(String tagToRollBackTo, String rollbackScript, Contexts contexts, Writer output)
            throws LiquibaseException {
        rollback(tagToRollBackTo, rollbackScript, contexts, new LabelExpression(), output);
    }

    @Deprecated
    public void rollback(String tagToRollBackTo, String rollbackScript, Contexts contexts,
                         LabelExpression labelExpression, Writer output) throws LiquibaseException {
        new CommandScope(RollbackSqlCommandStep.COMMAND_NAME)
                .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, Liquibase.this.getDatabase())
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null? contexts.toString() : null))
                .addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null))
                .addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_ARG, changeExecListener)
                .addArgumentValue(RollbackCommandStep.TAG_ARG, tagToRollBackTo)
                .addArgumentValue(AbstractRollbackCommandStep.ROLLBACK_SCRIPT_ARG, rollbackScript)
                .setOutput(new WriterOutputStream(output, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()))
                .execute();
    }
    // ---------- End RollbackSQL Family of methods

    // ---------- Rollback (To Tag) Family of methods
    @Deprecated
    public void rollback(String tagToRollBackTo, String contexts) throws LiquibaseException {
        rollback(tagToRollBackTo, null, contexts);
    }

    @Deprecated
    public void rollback(String tagToRollBackTo, Contexts contexts) throws LiquibaseException {
        rollback(tagToRollBackTo, null, contexts);
    }

    @Deprecated
    public void rollback(String tagToRollBackTo, Contexts contexts, LabelExpression labelExpression)
            throws LiquibaseException {
        rollback(tagToRollBackTo, null, contexts, labelExpression);
    }

    @Deprecated
    public void rollback(String tagToRollBackTo, String rollbackScript, String contexts) throws LiquibaseException {
        rollback(tagToRollBackTo, rollbackScript, new Contexts(contexts));
    }

    @Deprecated
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
        new CommandScope(RollbackCommandStep.COMMAND_NAME)
                .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, Liquibase.this.getDatabase())
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null? contexts.toString() : null))
                .addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null))
                .addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_ARG, changeExecListener)
                .addArgumentValue(RollbackCommandStep.TAG_ARG, tagToRollBackTo)
                .addArgumentValue(RollbackCommandStep.ROLLBACK_SCRIPT_ARG, rollbackScript)
                .execute();
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
        new CommandScope(RollbackToDateSqlCommandStep.COMMAND_NAME)
                .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, Liquibase.this.getDatabase())
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null? contexts.toString() : null))
                .addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null))
                .addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_ARG, changeExecListener)
                .addArgumentValue(RollbackToDateCommandStep.DATE_ARG, dateToRollBackTo)
                .addArgumentValue(AbstractRollbackCommandStep.ROLLBACK_SCRIPT_ARG, rollbackScript)
                .setOutput(new WriterOutputStream(output, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()))
                .execute();
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

        new CommandScope(RollbackToDateCommandStep.COMMAND_NAME)
                .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, Liquibase.this.getDatabase())
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null? contexts.toString() : null))
                .addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null))
                .addArgumentValue(ChangeExecListenerCommandStep.CHANGE_EXEC_LISTENER_ARG, changeExecListener)
                .addArgumentValue(RollbackToDateCommandStep.DATE_ARG, dateToRollBackTo)
                .addArgumentValue(AbstractRollbackCommandStep.ROLLBACK_SCRIPT_ARG, rollbackScript)
                .execute();

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
        String commandToRun = StringUtil.isEmpty(tag) ? ChangelogSyncCommandStep.COMMAND_NAME[0] : ChangelogSyncToTagCommandStep.COMMAND_NAME[0];
        runInScope(() -> {
            new CommandScope(commandToRun)
                    .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, Liquibase.this.getDatabase())
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
        String commandToRun = StringUtil.isEmpty(tag) ? ChangelogSyncSqlCommandStep.COMMAND_NAME[0] : ChangelogSyncToTagSqlCommandStep.COMMAND_NAME[0];
        runInScope(() -> new CommandScope(commandToRun)
                .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, Liquibase.this.getDatabase())
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null? contexts.toString() : null))
                .addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null))
                .addArgumentValue(ChangelogSyncToTagSqlCommandStep.TAG_ARG, tag)
                .setOutput(new WriterOutputStream(output, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()))
                .execute());
    }

    @Deprecated
    public void markNextChangeSetRan(String contexts, Writer output) throws LiquibaseException {
        markNextChangeSetRan(new Contexts(contexts), new LabelExpression(), output);
    }

    @Deprecated
    public void markNextChangeSetRan(Contexts contexts, LabelExpression labelExpression, Writer output)
            throws LiquibaseException {
        runInScope(() -> new CommandScope(MarkNextChangesetRanSqlCommandStep.COMMAND_NAME)
                .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, getDatabase())
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters)
                .addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null ? contexts.toString() : null))
                .addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null))
                .setOutput(new WriterOutputStream(output, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()))
                .execute());
    }

    @Deprecated
    public void markNextChangeSetRan(String contexts) throws LiquibaseException {
        markNextChangeSetRan(new Contexts(contexts), new LabelExpression());
    }

    @Deprecated
    public void markNextChangeSetRan(Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        runInScope(() -> new CommandScope(MarkNextChangesetRanCommandStep.COMMAND_NAME)
                .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, getDatabase())
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters)
                .addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null ? contexts.toString() : null))
                .addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null))
                .execute());
    }

    @Deprecated
    public void futureRollbackSQL(String contexts, Writer output) throws LiquibaseException {
        futureRollbackSQL(null, contexts, output, true);
    }

    @Deprecated
    public void futureRollbackSQL(Writer output) throws LiquibaseException {
        futureRollbackSQL(null, null, new Contexts(), new LabelExpression(), output);
    }

    @Deprecated
    public void futureRollbackSQL(String contexts, Writer output, boolean checkLiquibaseTables)
            throws LiquibaseException {
        futureRollbackSQL(null, contexts, output, checkLiquibaseTables);
    }

    @Deprecated
    public void futureRollbackSQL(Integer count, String contexts, Writer output) throws LiquibaseException {
        futureRollbackSQL(count, new Contexts(contexts), new LabelExpression(), output, true);
    }

    @Deprecated
    public void futureRollbackSQL(Contexts contexts, LabelExpression labelExpression, Writer output)
            throws LiquibaseException {
        futureRollbackSQL(null, null, contexts, labelExpression, output);
    }

    @Deprecated
    public void futureRollbackSQL(Integer count, String contexts, Writer output, boolean checkLiquibaseTables)
            throws LiquibaseException {
        futureRollbackSQL(count, new Contexts(contexts), new LabelExpression(), output, checkLiquibaseTables);
    }

    @Deprecated
    public void futureRollbackSQL(Integer count, Contexts contexts, LabelExpression labelExpression, Writer output)
            throws LiquibaseException {
        futureRollbackSQL(count, contexts, labelExpression, output, true);
    }

    @Deprecated
    public void futureRollbackSQL(Integer count, Contexts contexts, LabelExpression labelExpression, Writer output,
                                  boolean checkLiquibaseTables) throws LiquibaseException {
        futureRollbackSQL(count, null, contexts, labelExpression, output);
    }

    @Deprecated
    public void futureRollbackSQL(String tag, Contexts contexts, LabelExpression labelExpression, Writer output)
            throws LiquibaseException {
        futureRollbackSQL(null, tag, contexts, labelExpression, output);
    }

    @Deprecated
    protected void futureRollbackSQL(Integer count, String tag, Contexts contexts, LabelExpression labelExpression,
                                     Writer output) throws LiquibaseException {
        futureRollbackSQL(count, tag, contexts, labelExpression, output, true);
    }

    @Deprecated
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

        commandScope.addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, getDatabase())
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
     * Drops all database objects in the passed schema(s).
     */
    public final void dropAll(CatalogAndSchema... schemas) throws DatabaseException {

        CatalogAndSchema[] finalSchemas = schemas;
        try {
            CommandScope dropAll = new CommandScope("dropAll")
                    .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, Liquibase.this.getDatabase())
                    .addArgumentValue(DropAllCommandStep.CATALOG_AND_SCHEMAS_ARG, finalSchemas);

            try {
                dropAll.execute();
            } catch (CommandExecutionException e) {
                throw new DatabaseException(e);
            }
        } catch (LiquibaseException e) {
            if (e instanceof DatabaseException) {
                throw (DatabaseException) e;
            } else {
                throw new DatabaseException(e);
            }
        }
    }

    /**
     * 'Tags' the database for future rollback
     *
     * @deprecated Use {@link CommandScope(String)} to tag instead of this method.
     */
    @Deprecated
    public void tag(String tagString) throws LiquibaseException {
        new CommandScope("tag")
                .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, database)
                .addArgumentValue(TagCommandStep.TAG_ARG, tagString)
                .execute();
    }

    /**
     *  Verifies if a given tag exist in the database
     *
     * @deprecated Use {link {@link CommandScope(String)} to verify tag exist instead of this method.
     */
    @Deprecated
    public boolean tagExists(String tagString) throws LiquibaseException {
        CommandResults commandResults = new CommandScope("tagExists")
                .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, database)
                .addArgumentValue(TagExistsCommandStep.TAG_ARG, tagString)
                .execute();
        return commandResults.getResult(TagExistsCommandStep.TAG_EXISTS_RESULT);
    }

    @Deprecated
    public void updateTestingRollback(String contexts) throws LiquibaseException {
        updateTestingRollback(new Contexts(contexts), new LabelExpression());
    }

    @Deprecated
    public void updateTestingRollback(Contexts contexts, LabelExpression labelExpression) throws LiquibaseException {
        updateTestingRollback(null, contexts, labelExpression);

    }

    @Deprecated
    public void updateTestingRollback(String tag, Contexts contexts, LabelExpression labelExpression)
            throws LiquibaseException {
        runInScope(() -> {
            CommandScope updateTestingRollback = new CommandScope(UpdateTestingRollbackCommandStep.COMMAND_NAME);
            updateTestingRollback.addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, getDatabase());
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
    @Deprecated
    public DatabaseChangeLogLock[] listLocks() throws LiquibaseException {
        return ListLocksCommandStep.listLocks(database);
    }

    @Deprecated
    public void reportLocks(PrintStream out) throws LiquibaseException {
        runInScope(() -> {
            CommandScope listLocksCommand = new CommandScope(ListLocksCommandStep.COMMAND_NAME);
            listLocksCommand.addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, getDatabase());
            listLocksCommand.setOutput(out);
            listLocksCommand.execute();
        });
    }

    public void forceReleaseLocks() throws LiquibaseException {
        new CommandScope(ReleaseLocksCommandStep.COMMAND_NAME[0])
                .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, getDatabase())
                .execute();
    }

    /**
     * @deprecated use version with LabelExpression
     */
    @Deprecated
    public List<ChangeSet> listUnrunChangeSets(Contexts contexts) throws LiquibaseException {
        return listUnrunChangeSets(contexts, new LabelExpression());
    }

    @Deprecated
    public List<ChangeSet> listUnrunChangeSets(Contexts contexts, LabelExpression labels) throws LiquibaseException {
        return listUnrunChangeSets(contexts, labels, true);
    }

    @Deprecated
    public List<ChangeSet> listUnrunChangeSets(Contexts contexts, LabelExpression labels, boolean checkLiquibaseTables) throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labels);

        ListVisitor visitor = new ListVisitor();

        runInScope(() -> {

            DatabaseChangeLog changeLog = getDatabaseChangeLog();

            if (checkLiquibaseTables) {
                checkLiquibaseTables(false, changeLog, contexts, labels);
            }

            changeLog.validate(database, contexts, labels);

            ChangeLogIterator logIterator = getStandardChangelogIterator(contexts, labels, changeLog);

            logIterator.run(visitor, new RuntimeEnvironment(database, contexts, labels));
        });
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

        runInScope(() -> {

            DatabaseChangeLog changeLog = getDatabaseChangeLog();

            if (checkLiquibaseTables) {
                checkLiquibaseTables(false, changeLog, contexts, labelExpression);
            }

            changeLog.validate(database, contexts, labelExpression);

            ChangeLogIterator logIterator = getStandardChangelogIterator(contexts, labelExpression, changeLog);

            logIterator.run(visitor, new RuntimeEnvironment(database, contexts, labelExpression));
        });
        return visitor.getStatuses();
    }

    @Deprecated
    public void reportStatus(boolean verbose, String contexts, Writer out) throws LiquibaseException {
        reportStatus(verbose, new Contexts(contexts), new LabelExpression(), out);
    }

    @Deprecated
    public void reportStatus(boolean verbose, Contexts contexts, Writer out) throws LiquibaseException {
        reportStatus(verbose, contexts, new LabelExpression(), out);
    }

    @Deprecated
    public void reportStatus(boolean verbose, Contexts contexts, LabelExpression labels, Writer out)
            throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labels);
        runInScope(() -> {
            CommandScope statusCommand = new CommandScope(StatusCommandStep.COMMAND_NAME);
            statusCommand.addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, getDatabase());
            statusCommand.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters);
            statusCommand.addArgumentValue(StatusCommandStep.VERBOSE_ARG, verbose);
            statusCommand.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile);
            statusCommand.setOutput(new WriterOutputStream(out, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()));
            statusCommand.execute();
        });
    }

    @Deprecated
    public Collection<RanChangeSet> listUnexpectedChangeSets(String contexts) throws LiquibaseException {
        return listUnexpectedChangeSets(new Contexts(contexts), new LabelExpression());
    }

    @Deprecated
    public Collection<RanChangeSet> listUnexpectedChangeSets(Contexts contexts, LabelExpression labelExpression)
            throws LiquibaseException {
        return UnexpectedChangesetsCommandStep.listUnexpectedChangeSets(getDatabase(), getDatabaseChangeLog(), contexts, labelExpression);
    }

    @Deprecated
    public void reportUnexpectedChangeSets(boolean verbose, String contexts, Writer out) throws LiquibaseException {
        reportUnexpectedChangeSets(verbose, new Contexts(contexts), new LabelExpression(), out);
    }

    @Deprecated
    public void reportUnexpectedChangeSets(boolean verbose, Contexts contexts, LabelExpression labelExpression,
                                           Writer out) throws LiquibaseException {
        changeLogParameters.setContexts(contexts);
        changeLogParameters.setLabels(labelExpression);
        runInScope(() -> {
            CommandScope unexpectedChangesetsCommand = new CommandScope(UnexpectedChangesetsCommandStep.COMMAND_NAME);
            unexpectedChangesetsCommand.addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, getDatabase());
            unexpectedChangesetsCommand.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters);
            unexpectedChangesetsCommand.addArgumentValue(UnexpectedChangesetsCommandStep.VERBOSE_ARG, verbose);
            unexpectedChangesetsCommand.addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile);
            unexpectedChangesetsCommand.addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null? contexts.toString() : null));
            unexpectedChangesetsCommand.addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null));
            unexpectedChangesetsCommand.setOutput(new WriterOutputStream(out, GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue()));
            unexpectedChangesetsCommand.execute();
        });
    }

    /**
     * Sets checksums to null, so they will be repopulated next run
     *
     * @deprecated Use {@link CommandScope(String)}
     */
    @Deprecated
    public void clearCheckSums() throws LiquibaseException {
        CommandResults commandResults = new CommandScope(ClearChecksumsCommandStep.COMMAND_NAME)
                .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, database)
                .addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, database.getConnection().getURL())
                .execute();
    }

    /**
     * Calculate the checksum for a given identifier
     *
     * @deprecated Use {link {@link CommandScope(String)}.
     */
    @Deprecated
    public final CheckSum calculateCheckSum(final String changeSetIdentifier) throws LiquibaseException {
        CommandResults commandResults = new CommandScope("calculateChecksum")
                .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, database)
                .addArgumentValue(CalculateChecksumCommandStep.CHANGESET_IDENTIFIER_ARG, changeSetIdentifier)
                .addArgumentValue(CalculateChecksumCommandStep.CHANGELOG_FILE_ARG, this.changeLogFile)
                .execute();
        return commandResults.getResult(CalculateChecksumCommandStep.CHECKSUM_RESULT);
    }

    /**
     * Calculates the checksum for the values that form a given identifier
     *
     * @deprecated Use {link {@link CommandScope(String)}.
     */
    @Deprecated
    public CheckSum calculateCheckSum(final String filename, final String id, final String author)
            throws LiquibaseException {
        return this.calculateCheckSum(String.format("%s::%s::%s", filename, id, author));
    }

    @Deprecated
    public void generateDocumentation(String outputDirectory) throws LiquibaseException {
        // call without context
        generateDocumentation(outputDirectory, new Contexts(), new LabelExpression(), new CatalogAndSchema(null, null));
    }

    @Deprecated
    public void generateDocumentation(String outputDirectory, String contexts) throws LiquibaseException {
        generateDocumentation(outputDirectory, new Contexts(contexts), new LabelExpression(), new CatalogAndSchema(null, null));
    }

    @Deprecated
    public void generateDocumentation(String outputDirectory, String contexts, CatalogAndSchema... schemaList) throws LiquibaseException {
        generateDocumentation(outputDirectory, new Contexts(contexts), new LabelExpression(), schemaList);
    }

    /**
     * @deprecated Use {@link CommandScope} to generate dbDoc instead of this method.
     */
    @Deprecated
    public void generateDocumentation(String outputDirectory, Contexts contexts,
                                      LabelExpression labelExpression, CatalogAndSchema... schemaList) throws LiquibaseException {
        runInScope(() -> new CommandScope(DbDocCommandStep.COMMAND_NAME[0])
                .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, Liquibase.this.getDatabase())
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters)
                .addArgumentValue(DatabaseChangelogCommandStep.CONTEXTS_ARG, (contexts != null ? contexts.toString() : null))
                .addArgumentValue(DatabaseChangelogCommandStep.LABEL_FILTER_ARG, (labelExpression != null ? labelExpression.getOriginalString() : null))
                .addArgumentValue(DbDocCommandStep.CATALOG_AND_SCHEMAS_ARG, schemaList)
                .addArgumentValue(DbDocCommandStep.OUTPUT_DIRECTORY_ARG, outputDirectory)
                .execute());
    }

    /**
     * @deprecated Use {link {@link CommandScope(String)} to generate diff instead of this method.
     */
    @Deprecated
    public DiffResult diff(Database referenceDatabase, Database targetDatabase, CompareControl compareControl)
            throws LiquibaseException {
        return DiffGeneratorFactory.getInstance().compare(referenceDatabase, targetDatabase, compareControl);
    }

    /**
     * Checks changelogs for bad MD5Sums and preconditions before attempting a migration
     *
     * @deprecated use {@link CommandScope}
     */
    @Deprecated
    public void validate() throws LiquibaseException {
        new CommandScope("validate")
                .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, database)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DatabaseChangelogCommandStep.CHANGELOG_PARAMETERS, changeLogParameters)
                .execute();
    }

    public void setChangeLogParameter(String key, Object value) {
        this.changeLogParameters.set(key, value);
    }

    public void setChangeExecListener(ChangeExecListener listener) {
        this.changeExecListener = listener;
    }

    public DefaultChangeExecListener getDefaultChangeExecListener() {
        return defaultChangeExecListener;
    }

    /**
     * @deprecated Use {link {@link CommandScope(String)} to generateChangelog instead of this method.
     */
    @Deprecated
    @SafeVarargs
    public final void generateChangeLog(CatalogAndSchema catalogAndSchema, DiffToChangeLog changeLogWriter,
                                        PrintStream outputStream, Class<? extends DatabaseObject>... snapshotTypes)
            throws DatabaseException, CommandExecutionException {
        generateChangeLog(catalogAndSchema, changeLogWriter, outputStream, null, snapshotTypes);
    }

    /**
     * @deprecated Use {link {@link CommandScope(String)} to generateChangelog instead of this method.
     */
    @Deprecated
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
                .addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, getDatabase())
                .addArgumentValue(PreCompareCommandStep.SNAPSHOT_TYPES_ARG, snapshotTypes)
                .setOutput(outputStream)
                .execute();
    }

    private void runInScope(Scope.ScopedRunner scopedRunner) throws LiquibaseException {
        Map<String, Object> scopeObjects = new HashMap<>();
        scopeObjects.put(Scope.Attr.database.name(), getDatabase());
        scopeObjects.put(Scope.Attr.resourceAccessor.name(), getResourceAccessor());

        try {
            Scope.child(scopeObjects, scopedRunner);
        } catch (Exception e) {
            if (e instanceof LiquibaseException) {
                throw (LiquibaseException) e;
            } else {
                throw new LiquibaseException(e);
            }
        }
    }

    @Override
    public void close() throws LiquibaseException {
        if (database != null) {
            database.close();
        }
    }
}
