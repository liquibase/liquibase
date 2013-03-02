package liquibase;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.text.DateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import liquibase.change.CheckSum;
import liquibase.changelog.ChangeLogIterator;
import liquibase.changelog.ChangeLogParameters;
import liquibase.changelog.ChangeSet;
import liquibase.changelog.DatabaseChangeLog;
import liquibase.changelog.RanChangeSet;
import liquibase.changelog.filter.AfterTagChangeSetFilter;
import liquibase.changelog.filter.AlreadyRanChangeSetFilter;
import liquibase.changelog.filter.ChangeSetFilter;
import liquibase.changelog.filter.ContextChangeSetFilter;
import liquibase.changelog.filter.CountChangeSetFilter;
import liquibase.changelog.filter.DbmsChangeSetFilter;
import liquibase.changelog.filter.ExecutedAfterChangeSetFilter;
import liquibase.changelog.filter.NotRanChangeSetFilter;
import liquibase.changelog.filter.ShouldRunChangeSetFilter;
import liquibase.changelog.visitor.*;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.core.OracleDatabase;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.compare.CompareControl;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.lockservice.DatabaseChangeLogLock;
import liquibase.lockservice.LockService;
import liquibase.lockservice.LockServiceFactory;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.core.RawSqlStatement;
import liquibase.statement.core.UpdateStatement;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

/**
 * Core Liquibase facade.
 * Although there are several ways of executing Liquibase (Ant, command line, etc.) they are all wrappers around this class.
 */
public class Liquibase {

    public static final String SHOULD_RUN_SYSTEM_PROPERTY = "liquibase.should.run";
    public static final String ENABLE_CHANGELOG_PROP_ESCAPING = "liquibase.enableEscaping";

    private String changeLogFile;
    private ResourceAccessor resourceAccessor;

    protected Database database;
    private Logger log;

    private ChangeLogParameters changeLogParameters;

    public Liquibase(String changeLogFile, ResourceAccessor resourceAccessor, DatabaseConnection conn) throws LiquibaseException {
        this(changeLogFile, resourceAccessor, DatabaseFactory.getInstance().findCorrectDatabaseImplementation(conn));
    }

    public Liquibase(String changeLogFile, ResourceAccessor resourceAccessor, Database database) throws LiquibaseException {
        log = LogFactory.getLogger();

        if (changeLogFile != null) {
            this.changeLogFile = changeLogFile.replace('\\', '/');  //convert to standard / if using absolute path on windows
        }
        this.resourceAccessor = resourceAccessor;

        changeLogParameters = new ChangeLogParameters(database);
        setDatabase(database);
        
    }

    public ChangeLogParameters getChangeLogParameters() {
        return changeLogParameters;
    }

    public Database getDatabase() {
        return database;
    }

    private void setDatabase(Database database) throws DatabaseException {
        this.database=database;
        if(database!=null) //Some tests use a null database
            setDatabasePropertiesAsChangelogParameters(database);
    }

    /**
     * FileOpener to use for accessing changelog files.
     */
    public ResourceAccessor getFileOpener() {
        return resourceAccessor;
    }

    /**
     * Use this function to override the current date/time function used to insert dates into the database.
     * Especially useful when using an unsupported database.
     */
    public void setCurrentDateTimeFunction(String currentDateTimeFunction) {
        if (currentDateTimeFunction != null) {
            this.database.setCurrentDateTimeFunction(currentDateTimeFunction);
        }
    }

    public void update(String contexts) throws LiquibaseException {
        contexts = StringUtils.trimToNull(contexts);
        LockService lockService = getLockService();
        lockService.waitForLock();

        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        try {
            DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor).parse(changeLogFile, changeLogParameters, resourceAccessor);

            checkDatabaseChangeLogTable(true, changeLog, contexts);

            changeLog.validate(database, contexts);
            ChangeLogIterator changeLogIterator = getStandardChangelogIterator(contexts, changeLog);

            changeLogIterator.run(new UpdateVisitor(database), database);
        } finally {
            try {
                database.setObjectQuotingStrategy(ObjectQuotingStrategy.LEGACY);
                lockService.releaseLock();
            } catch (LockException e) {
                log.severe("Could not release lock", e);
            }
        }
    }

    private ChangeLogIterator getStandardChangelogIterator(String contexts, DatabaseChangeLog changeLog) throws DatabaseException {
        return new ChangeLogIterator(changeLog,
                new ShouldRunChangeSetFilter(database),
                new ContextChangeSetFilter(contexts),
                new DbmsChangeSetFilter(database));
    }

    public void update(String contexts, Writer output) throws LiquibaseException {
        contexts = StringUtils.trimToNull(contexts);
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        Executor oldTemplate = ExecutorService.getInstance().getExecutor(database);
        LoggingExecutor loggingExecutor = new LoggingExecutor(ExecutorService.getInstance().getExecutor(database), output, database);
        ExecutorService.getInstance().setExecutor(database, loggingExecutor);

        outputHeader("Update Database Script");

        LockService lockService = getLockService();
        lockService.waitForLock();

        try {

            update(contexts);

            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        } finally {
            lockService.releaseLock();
        }

        ExecutorService.getInstance().setExecutor(database, oldTemplate);
    }

    public void update(int changesToApply, String contexts) throws LiquibaseException {
        contexts = StringUtils.trimToNull(contexts);
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        LockService lockService = getLockService();
        lockService.waitForLock();

        try {

            DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor).parse(changeLogFile, changeLogParameters, resourceAccessor);

            checkDatabaseChangeLogTable(true, changeLog, contexts);
            changeLog.validate(database, contexts);

            ChangeLogIterator logIterator = new ChangeLogIterator(changeLog,
                    new ShouldRunChangeSetFilter(database),
                    new ContextChangeSetFilter(contexts),
                    new DbmsChangeSetFilter(database),
                    new CountChangeSetFilter(changesToApply));

            logIterator.run(new UpdateVisitor(database), database);
        } finally {
            lockService.releaseLock();
        }
    }

    public void update(int changesToApply, String contexts, Writer output) throws LiquibaseException {
        contexts = StringUtils.trimToNull(contexts);
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        Executor oldTemplate = ExecutorService.getInstance().getExecutor(database);
        LoggingExecutor loggingExecutor = new LoggingExecutor(ExecutorService.getInstance().getExecutor(database), output, database);
        ExecutorService.getInstance().setExecutor(database, loggingExecutor);

        outputHeader("Update " + changesToApply + " Change Sets Database Script");

        update(changesToApply, contexts);

        try {
            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }

        ExecutorService.getInstance().setExecutor(database, oldTemplate);
    }

    private void outputHeader(String message) throws DatabaseException {
        Executor executor = ExecutorService.getInstance().getExecutor(database);
        executor.comment("*********************************************************************");
        executor.comment(message);
        executor.comment("*********************************************************************");
        executor.comment("Change Log: " + changeLogFile);
        executor.comment("Ran at: " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date()));
        executor.comment("Against: " + getDatabase().getConnection().getConnectionUserName() + "@" + getDatabase().getConnection().getURL());
        executor.comment("Liquibase version: " + LiquibaseUtil.getBuildVersion());
        executor.comment("*********************************************************************" + StreamUtil.getLineSeparator());
        
        if (database instanceof OracleDatabase) {
        	executor.execute(new RawSqlStatement("SET DEFINE OFF;"));
        }
    }

    public void rollback(int changesToRollback, String contexts, Writer output) throws LiquibaseException {
        contexts = StringUtils.trimToNull(contexts);
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        Executor oldTemplate = ExecutorService.getInstance().getExecutor(database);
        ExecutorService.getInstance().setExecutor(database, new LoggingExecutor(ExecutorService.getInstance().getExecutor(database), output, database));

        outputHeader("Rollback " + changesToRollback + " Change(s) Script");

        rollback(changesToRollback, contexts);

        try {
            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }
        ExecutorService.getInstance().setExecutor(database, oldTemplate);
    }

    public void rollback(int changesToRollback, String contexts) throws LiquibaseException {
        contexts = StringUtils.trimToNull(contexts);
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        LockService lockService = getLockService();
        lockService.waitForLock();

        try {
            DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor).parse(changeLogFile, changeLogParameters, resourceAccessor);
            checkDatabaseChangeLogTable(false, changeLog, contexts);

            changeLog.validate(database, contexts);

            ChangeLogIterator logIterator = new ChangeLogIterator(database.getRanChangeSetList(), changeLog,
                    new AlreadyRanChangeSetFilter(database.getRanChangeSetList()),
                    new ContextChangeSetFilter(contexts),
                    new DbmsChangeSetFilter(database),
                    new CountChangeSetFilter(changesToRollback));

            logIterator.run(new RollbackVisitor(database), database);
        } finally {
            try {
                lockService.releaseLock();
            } catch (LockException e) {
                log.severe("Error releasing lock", e);
            }
        }
    }

    public void rollback(String tagToRollBackTo, String contexts, Writer output) throws LiquibaseException {
        contexts = StringUtils.trimToNull(contexts);
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        Executor oldTemplate = ExecutorService.getInstance().getExecutor(database);
        ExecutorService.getInstance().setExecutor(database, new LoggingExecutor(ExecutorService.getInstance().getExecutor(database), output, database));

        outputHeader("Rollback to '" + tagToRollBackTo + "' Script");

        rollback(tagToRollBackTo, contexts);

        try {
            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }
        ExecutorService.getInstance().setExecutor(database, oldTemplate);
    }

    public void rollback(String tagToRollBackTo, String contexts) throws LiquibaseException {
        contexts = StringUtils.trimToNull(contexts);
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        LockService lockService = getLockService();
        lockService.waitForLock();

        try {

            DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor).parse(changeLogFile, changeLogParameters, resourceAccessor);
            checkDatabaseChangeLogTable(false, changeLog, contexts);

            changeLog.validate(database, contexts);

            List<RanChangeSet> ranChangeSetList = database.getRanChangeSetList();
            ChangeLogIterator logIterator = new ChangeLogIterator(ranChangeSetList, changeLog,
                    new AfterTagChangeSetFilter(tagToRollBackTo, ranChangeSetList),
                    new AlreadyRanChangeSetFilter(ranChangeSetList),
                    new ContextChangeSetFilter(contexts),
                    new DbmsChangeSetFilter(database));

            logIterator.run(new RollbackVisitor(database), database);
        } finally {
            lockService.releaseLock();
        }
    }

    public void rollback(Date dateToRollBackTo, String contexts, Writer output) throws LiquibaseException {
        contexts = StringUtils.trimToNull(contexts);
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        Executor oldTemplate = ExecutorService.getInstance().getExecutor(database);
        ExecutorService.getInstance().setExecutor(database, new LoggingExecutor(ExecutorService.getInstance().getExecutor(database), output, database));

        outputHeader("Rollback to " + dateToRollBackTo + " Script");

        rollback(dateToRollBackTo, contexts);

        try {
            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }
        ExecutorService.getInstance().setExecutor(database, oldTemplate);
    }

    public void rollback(Date dateToRollBackTo, String contexts) throws LiquibaseException {
        contexts = StringUtils.trimToNull(contexts);
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        LockService lockService = getLockService();
        lockService.waitForLock();

        try {
            DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor).parse(changeLogFile, changeLogParameters, resourceAccessor);
            checkDatabaseChangeLogTable(false, changeLog, contexts);
            changeLog.validate(database, contexts);

            List<RanChangeSet> ranChangeSetList = database.getRanChangeSetList();
            ChangeLogIterator logIterator = new ChangeLogIterator(ranChangeSetList, changeLog,
                    new ExecutedAfterChangeSetFilter(dateToRollBackTo, ranChangeSetList),
                    new AlreadyRanChangeSetFilter(ranChangeSetList),
                    new ContextChangeSetFilter(contexts),
                    new DbmsChangeSetFilter(database));

            logIterator.run(new RollbackVisitor(database), database);
        } finally {
            lockService.releaseLock();
        }
    }

    public void changeLogSync(String contexts, Writer output) throws LiquibaseException {
        contexts = StringUtils.trimToNull(contexts);
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        LoggingExecutor outputTemplate = new LoggingExecutor(ExecutorService.getInstance().getExecutor(database), output, database);
        Executor oldTemplate = ExecutorService.getInstance().getExecutor(database);
        ExecutorService.getInstance().setExecutor(database, outputTemplate);

        outputHeader("SQL to add all changesets to database history table");

        changeLogSync(contexts);

        try {
            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }

        ExecutorService.getInstance().setExecutor(database, oldTemplate);
    }

    public void changeLogSync(String contexts) throws LiquibaseException {
        contexts = StringUtils.trimToNull(contexts);
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        LockService lockService = LockServiceFactory.getInstance().getLockService(database);
        lockService.waitForLock();

        try {
            DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor).parse(changeLogFile, changeLogParameters, resourceAccessor);
            checkDatabaseChangeLogTable(true, changeLog, contexts);
            changeLog.validate(database, contexts);

            ChangeLogIterator logIterator = new ChangeLogIterator(changeLog,
                    new NotRanChangeSetFilter(database.getRanChangeSetList()),
                    new ContextChangeSetFilter(contexts),
                    new DbmsChangeSetFilter(database));

            logIterator.run(new ChangeLogSyncVisitor(database), database);
        } finally {
            lockService.releaseLock();
        }
    }

    public void markNextChangeSetRan(String contexts, Writer output) throws LiquibaseException {
        contexts = StringUtils.trimToNull(contexts);
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));


        LoggingExecutor outputTemplate = new LoggingExecutor(ExecutorService.getInstance().getExecutor(database), output, database);
        Executor oldTemplate = ExecutorService.getInstance().getExecutor(database);
        ExecutorService.getInstance().setExecutor(database, outputTemplate);

        outputHeader("SQL to add all changesets to database history table");

        markNextChangeSetRan(contexts);

        try {
            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }

        ExecutorService.getInstance().setExecutor(database, oldTemplate);
    }

    public void markNextChangeSetRan(String contexts) throws LiquibaseException {
        contexts = StringUtils.trimToNull(contexts);
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        LockService lockService = getLockService();
        lockService.waitForLock();

        try {
            DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor).parse(changeLogFile, changeLogParameters, resourceAccessor);
            checkDatabaseChangeLogTable(false, changeLog, contexts);
            changeLog.validate(database, contexts);

            ChangeLogIterator logIterator = new ChangeLogIterator(changeLog,
                    new NotRanChangeSetFilter(database.getRanChangeSetList()),
                    new ContextChangeSetFilter(contexts),
                    new DbmsChangeSetFilter(database),
                    new CountChangeSetFilter(1));

            logIterator.run(new ChangeLogSyncVisitor(database), database);
        } finally {
            lockService.releaseLock();
        }
    }

    public void futureRollbackSQL(String contexts, Writer output) throws LiquibaseException {
        futureRollbackSQL(null, contexts, output);
    }

    public void futureRollbackSQL(Integer count, String contexts, Writer output) throws LiquibaseException {
        contexts = StringUtils.trimToNull(contexts);
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        LoggingExecutor outputTemplate = new LoggingExecutor(ExecutorService.getInstance().getExecutor(database), output, database);
        Executor oldTemplate = ExecutorService.getInstance().getExecutor(database);
        ExecutorService.getInstance().setExecutor(database, outputTemplate);

        outputHeader("SQL to roll back currently unexecuted changes");

        LockService lockService = getLockService();
        lockService.waitForLock();

        try {
            DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor).parse(changeLogFile, changeLogParameters, resourceAccessor);
            checkDatabaseChangeLogTable(false, changeLog, contexts);
            changeLog.validate(database, contexts);

            ChangeLogIterator logIterator;
            if (count == null) {
                logIterator = new ChangeLogIterator(changeLog,
                        new NotRanChangeSetFilter(database.getRanChangeSetList()),
                        new ContextChangeSetFilter(contexts),
                        new DbmsChangeSetFilter(database));
            } else {
                ChangeLogIterator forwardIterator = new ChangeLogIterator(changeLog,
                        new NotRanChangeSetFilter(database.getRanChangeSetList()),
                        new ContextChangeSetFilter(contexts),
                        new DbmsChangeSetFilter(database),
                        new CountChangeSetFilter(count));
                final ListVisitor listVisitor = new ListVisitor();
                forwardIterator.run(listVisitor, database);

                logIterator = new ChangeLogIterator(changeLog,
                        new NotRanChangeSetFilter(database.getRanChangeSetList()),
                        new ContextChangeSetFilter(contexts),
                        new DbmsChangeSetFilter(database),
                        new ChangeSetFilter() {
                            public boolean accepts(ChangeSet changeSet) {
                                return listVisitor.getSeenChangeSets().contains(changeSet);
                            }
                        });
            }

            logIterator.run(new RollbackVisitor(database), database);
        } finally {
            lockService.releaseLock();
            ExecutorService.getInstance().setExecutor(database, oldTemplate);
        }

        try {
            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }

    }

    /**
     * Drops all database objects owned by the current user.
     */
    public final void dropAll() throws DatabaseException, LockException {
        dropAll(new CatalogAndSchema(getDatabase().getDefaultCatalogName(), getDatabase().getDefaultSchemaName()));
    }

    /**
     * Drops all database objects owned by the current user.
     */                                      
    public final void dropAll(CatalogAndSchema... schemas) throws DatabaseException {
        try {
            getLockService().waitForLock();

            for (CatalogAndSchema schema : schemas) {
                log.info("Dropping Database Objects in schema: " + schema);
                checkDatabaseChangeLogTable(false, null, null);
                getDatabase().dropDatabaseObjects(schema);
                checkDatabaseChangeLogTable(false, null, null);
                log.debug("Objects dropped successfully");
            }
        } catch (DatabaseException e) {
            throw e;
        } catch (Exception e) {
            throw new DatabaseException(e);
        } finally {
            try {
                getLockService().releaseLock();
            } catch (LockException e) {
                log.severe("Unable to release lock: " + e.getMessage());
            }
        }
    }

    /**
     * 'Tags' the database for future rollback
     */
    public void tag(String tagString) throws LiquibaseException {
        LockService lockService = getLockService();
        lockService.waitForLock();

        try {
            checkDatabaseChangeLogTable(false, null, null);
            getDatabase().tag(tagString);
        } finally {
            lockService.releaseLock();
        }
    }


    public void updateTestingRollback(String contexts) throws LiquibaseException {
        contexts = StringUtils.trimToNull(contexts);
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        Date baseDate = new Date();
        update(contexts);
        rollback(baseDate, contexts);
        update(contexts);
    }

    public void checkDatabaseChangeLogTable(boolean updateExistingNullChecksums, DatabaseChangeLog databaseChangeLog, String contexts) throws LiquibaseException {
        contexts = StringUtils.trimToNull(contexts);
        if (updateExistingNullChecksums && databaseChangeLog == null) {
            throw new LiquibaseException("changeLog parameter is required if updating existing checksums");
        }
        String[] splitContexts = null;
        if (StringUtils.trimToNull(contexts) != null) {
            splitContexts = contexts.split(",");
        }
        getDatabase().checkDatabaseChangeLogTable(updateExistingNullChecksums, databaseChangeLog, splitContexts);
        if (!getLockService().hasChangeLogLock()) {
            getDatabase().checkDatabaseChangeLogLockTable();
        }
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
        checkDatabaseChangeLogTable(false, null, null);

        return getLockService().listLocks();
    }

    public void reportLocks(PrintStream out) throws LiquibaseException {
        DatabaseChangeLogLock[] locks = listLocks();
        out.println("Database change log locks for " + getDatabase().getConnection().getConnectionUserName() + "@" + getDatabase().getConnection().getURL());
        if (locks.length == 0) {
            out.println(" - No locks");
        }
        for (DatabaseChangeLogLock lock : locks) {
            out.println(" - " + lock.getLockedBy() + " at " + DateFormat.getDateTimeInstance().format(lock.getLockGranted()));
        }

    }

    public void forceReleaseLocks() throws LiquibaseException {
        checkDatabaseChangeLogTable(false, null, null);

        getLockService().forceReleaseLock();
    }

    public List<ChangeSet> listUnrunChangeSets(String contexts) throws LiquibaseException {
        contexts = StringUtils.trimToNull(contexts);
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor).parse(changeLogFile, changeLogParameters, resourceAccessor);

        changeLog.validate(database, contexts);

        ChangeLogIterator logIterator = getStandardChangelogIterator(contexts, changeLog);

        ListVisitor visitor = new ListVisitor();
        logIterator.run(visitor, database);
        return visitor.getSeenChangeSets();
    }

    public void reportStatus(boolean verbose, String contexts, Writer out) throws LiquibaseException {
        contexts = StringUtils.trimToNull(contexts);
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        try {
            List<ChangeSet> unrunChangeSets = listUnrunChangeSets(contexts);
            if (unrunChangeSets.size() == 0) {
                out.append(getDatabase().getConnection().getConnectionUserName());
                out.append("@");
                out.append(getDatabase().getConnection().getURL());
                out.append(" is up to date");
                out.append(StreamUtil.getLineSeparator());
            } else {
                out.append(String.valueOf(unrunChangeSets.size()));
                out.append(" change sets have not been applied to ");
                out.append(getDatabase().getConnection().getConnectionUserName());
                out.append("@");
                out.append(getDatabase().getConnection().getURL());
                out.append(StreamUtil.getLineSeparator());
                if (verbose) {
                    for (ChangeSet changeSet : unrunChangeSets) {
                        out.append("     ").append(changeSet.toString(false)).append(StreamUtil.getLineSeparator());
                    }
                }
            }

            out.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }

    }

    public Collection<RanChangeSet> listUnexpectedChangeSets(String contexts) throws LiquibaseException {
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor).parse(changeLogFile, changeLogParameters, resourceAccessor);
        changeLog.validate(database, contexts);

        ChangeLogIterator logIterator = new ChangeLogIterator(changeLog,
                new ContextChangeSetFilter(contexts),
                new DbmsChangeSetFilter(database));
        ExpectedChangesVisitor visitor = new ExpectedChangesVisitor(database.getRanChangeSetList());
        logIterator.run(visitor, database);
        return visitor.getUnexpectedChangeSets();
    }


    public void reportUnexpectedChangeSets(boolean verbose, String contexts, Writer out) throws LiquibaseException {
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        try {
            Collection<RanChangeSet> unexpectedChangeSets = listUnexpectedChangeSets(contexts);
            if (unexpectedChangeSets.size() == 0) {
                out.append(getDatabase().getConnection().getConnectionUserName());
                out.append("@");
                out.append(getDatabase().getConnection().getURL());
                out.append(" contains no unexpected changes!");
                out.append(StreamUtil.getLineSeparator());
            } else {
                out.append(String.valueOf(unexpectedChangeSets.size()));
                out.append(" unexpected changes were found in ");
                out.append(getDatabase().getConnection().getConnectionUserName());
                out.append("@");
                out.append(getDatabase().getConnection().getURL());
                out.append(StreamUtil.getLineSeparator());
                if (verbose) {
                    for (RanChangeSet ranChangeSet : unexpectedChangeSets) {
                        out.append("     ").append(ranChangeSet.toString()).append(StreamUtil.getLineSeparator());
                    }
                }
            }

            out.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }

    }
    
    /**
     * Sets checksums to null so they will be repopulated next run
     */
    public void clearCheckSums() throws LiquibaseException {
        log.info("Clearing database change log checksums");
        LockService lockService = getLockService();
        lockService.waitForLock();

        try {
            checkDatabaseChangeLogTable(false, null, null);

            UpdateStatement updateStatement = new UpdateStatement(getDatabase().getLiquibaseCatalogName(), getDatabase().getLiquibaseSchemaName(), getDatabase().getDatabaseChangeLogTableName());
            updateStatement.addNewColumnValue("MD5SUM", null);
            ExecutorService.getInstance().getExecutor(database).execute(updateStatement);
            getDatabase().commit();
        } finally {
            lockService.releaseLock();
        }
    }

    public final CheckSum calculateCheckSum(final String changeSetIdentifier) throws LiquibaseException {
        if (changeSetIdentifier == null) {
            throw new LiquibaseException(new IllegalArgumentException("changeSetIdentifier"));
        }
        final List<String> parts = StringUtils.splitAndTrim(changeSetIdentifier, "::");
        if (parts == null || parts.size() < 3) {
            throw new LiquibaseException(new IllegalArgumentException("Invalid changeSet identifier: " + changeSetIdentifier));
        }
        return this.calculateCheckSum(parts.get(0), parts.get(1), parts.get(2));
    }

    public CheckSum calculateCheckSum(final String filename, final String id, final String author) throws LiquibaseException {
        log.info(String.format("Calculating checksum for changeset %s::%s::%s", filename, id, author));
        final ChangeLogParameters changeLogParameters = this.getChangeLogParameters();
        final ResourceAccessor resourceAccessor = this.getFileOpener();
        final DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance().getParser(this.changeLogFile, resourceAccessor).parse(this.changeLogFile, changeLogParameters, resourceAccessor);

        // TODO: validate?

        final ChangeSet changeSet = changeLog.getChangeSet(filename, author, id);
        if (changeSet == null) {
          throw new LiquibaseException(new IllegalArgumentException("No such changeSet: " + filename + "::" + id + "::" + author));
        }

        return changeSet.generateCheckSum();
    }

    public void generateDocumentation(String outputDirectory) throws LiquibaseException {
    	// call without context
    	generateDocumentation(outputDirectory, null);
    }
    
    public void generateDocumentation(String outputDirectory, String contexts) throws LiquibaseException {
        contexts = StringUtils.trimToNull(contexts);
        log.info("Generating Database Documentation");
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));
        LockService lockService = getLockService();
        lockService.waitForLock();

        try {
            DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor).parse(changeLogFile, changeLogParameters, resourceAccessor);
            checkDatabaseChangeLogTable(false, changeLog, null);

            String[] splitContexts = null;
            if (StringUtils.trimToNull(contexts) != null) {
                splitContexts = contexts.split(",");
            }
            changeLog.validate(database, splitContexts);

            ChangeLogIterator logIterator = new ChangeLogIterator(changeLog,
                    new DbmsChangeSetFilter(database));

            DBDocVisitor visitor = new DBDocVisitor(database);
            logIterator.run(visitor, database);

            visitor.writeHTML(new File(outputDirectory), resourceAccessor);
        } catch (IOException e) {
            throw new LiquibaseException(e);
        } finally {
            lockService.releaseLock();
        }

//        try {
//            if (!LockService.getExecutor(database).waitForLock()) {
//                return;
//            }
//
//            DBDocChangeLogHandler changeLogHandler = new DBDocChangeLogHandler(outputDirectory, this, changeLogFile,resourceAccessor);
//            runChangeLogs(changeLogHandler);
//
//            changeLogHandler.writeHTML(this);
//        } finally {
//            releaseLock();
//        }
    }

    public DiffResult diff(Database referenceDatabase, Database targetDatabase, CompareControl compareControl) throws LiquibaseException {
        return DiffGeneratorFactory.getInstance().compare(referenceDatabase, targetDatabase, compareControl);
    }

    /**
     * Checks changelogs for bad MD5Sums and preconditions before attempting a migration
     */
    public void validate() throws LiquibaseException {

        DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor).parse(changeLogFile, changeLogParameters, resourceAccessor);
        changeLog.validate(database);
    }

    public void setChangeLogParameter(String key, Object value) {
        this.changeLogParameters.set(key, value);
    }

    /**
     * Add safe database properties as changelog parameters.<br/>
     * Safe properties are the ones that doesn't have side effects in liquibase state and also don't change in during the liquibase execution
     * @param database Database which propeties are put in the changelog
     * @throws DatabaseException
     */
    private void setDatabasePropertiesAsChangelogParameters(Database database) throws DatabaseException {            
            setChangeLogParameter("database.autoIncrementClause", database.getAutoIncrementClause(null, null));
            setChangeLogParameter("database.currentDateTimeFunction", database.getCurrentDateTimeFunction());
            setChangeLogParameter("database.databaseChangeLogLockTableName", database.getDatabaseChangeLogLockTableName());
            setChangeLogParameter("database.databaseChangeLogTableName", database.getDatabaseChangeLogTableName());
            setChangeLogParameter("database.databaseMajorVersion", database.getDatabaseMajorVersion());
            setChangeLogParameter("database.databaseMinorVersion", database.getDatabaseMinorVersion());
            setChangeLogParameter("database.databaseProductName", database.getDatabaseProductName());
            setChangeLogParameter("database.databaseProductVersion", database.getDatabaseProductVersion());
            setChangeLogParameter("database.defaultCatalogName", database.getDefaultCatalogName());
            setChangeLogParameter("database.defaultSchemaName", database.getDefaultSchemaName());
            setChangeLogParameter("database.defaultSchemaNamePrefix", StringUtils.trimToNull(database.getDefaultSchemaName())==null?"":"."+database.getDefaultSchemaName());
            setChangeLogParameter("database.lineComment", database.getLineComment());
            setChangeLogParameter("database.liquibaseSchemaName", database.getLiquibaseSchemaName());
            setChangeLogParameter("database.typeName", database.getShortName());
            setChangeLogParameter("database.isSafeToRunUpdate", database.isSafeToRunUpdate());
            setChangeLogParameter("database.requiresPassword", database.requiresPassword());
            setChangeLogParameter("database.requiresUsername", database.requiresUsername());
            setChangeLogParameter("database.supportsForeignKeyDisable", database.supportsForeignKeyDisable());
            setChangeLogParameter("database.supportsInitiallyDeferrableColumns", database.supportsInitiallyDeferrableColumns());
            setChangeLogParameter("database.supportsRestrictForeignKeys", database.supportsRestrictForeignKeys());
            setChangeLogParameter("database.supportsSchemas", database.supportsSchemas());
            setChangeLogParameter("database.supportsSequences", database.supportsSequences());
            setChangeLogParameter("database.supportsTablespaces", database.supportsTablespaces());
    }

    private LockService getLockService() {
        return LockServiceFactory.getInstance().getLockService(database);
    }
}
