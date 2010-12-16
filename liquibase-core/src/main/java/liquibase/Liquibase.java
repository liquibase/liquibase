package liquibase;

import liquibase.changelog.*;
import liquibase.changelog.filter.*;
import liquibase.changelog.visitor.*;
import liquibase.database.Database;
import liquibase.database.DatabaseConnection;
import liquibase.database.DatabaseFactory;
import liquibase.diff.Diff;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.executor.Executor;
import liquibase.executor.ExecutorService;
import liquibase.executor.LoggingExecutor;
import liquibase.lockservice.DatabaseChangeLogLock;
import liquibase.lockservice.LockService;
import liquibase.logging.LogFactory;
import liquibase.logging.Logger;
import liquibase.parser.ChangeLogParserFactory;
import liquibase.resource.ResourceAccessor;
import liquibase.statement.core.UpdateStatement;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StreamUtil;
import liquibase.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.text.DateFormat;
import java.util.*;

/**
 * Core Liquibase facade.
 * Although there are several ways of executing Liquibase (Ant, command line, etc.) they are all wrappers around this class.
 */
public class Liquibase {

    public static final String SHOULD_RUN_SYSTEM_PROPERTY = "liquibase.should.run";

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
            this.changeLogFile = changeLogFile.replace('\\', '/');  //convert to standard / if usign absolute path on windows
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

        LockService lockService = LockService.getInstance(database);
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
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        Executor oldTemplate = ExecutorService.getInstance().getExecutor(database);
        LoggingExecutor loggingExecutor = new LoggingExecutor(ExecutorService.getInstance().getExecutor(database), output, database);
        ExecutorService.getInstance().setExecutor(database, loggingExecutor);

        outputHeader("Update Database Script");

        LockService lockService = LockService.getInstance(database);
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

        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        LockService lockService = LockService.getInstance(database);
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
    }

    public void rollback(int changesToRollback, String contexts, Writer output) throws LiquibaseException {
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
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        LockService lockService = LockService.getInstance(database);
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
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        LockService lockService = LockService.getInstance(database);
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
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        LockService lockService = LockService.getInstance(database);
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
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        LockService lockService = LockService.getInstance(database);
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
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        LockService lockService = LockService.getInstance(database);
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
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        LoggingExecutor outputTemplate = new LoggingExecutor(ExecutorService.getInstance().getExecutor(database), output, database);
        Executor oldTemplate = ExecutorService.getInstance().getExecutor(database);
        ExecutorService.getInstance().setExecutor(database, outputTemplate);

        outputHeader("SQL to roll back currently unexecuted changes");

        LockService lockService = LockService.getInstance(database);
        lockService.waitForLock();

        try {
            DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor).parse(changeLogFile, changeLogParameters, resourceAccessor);
            checkDatabaseChangeLogTable(false, changeLog, contexts);
            changeLog.validate(database, contexts);

            ChangeLogIterator logIterator = new ChangeLogIterator(changeLog,
                    new NotRanChangeSetFilter(database.getRanChangeSetList()),
                    new ContextChangeSetFilter(contexts),
                    new DbmsChangeSetFilter(database));

            logIterator.run(new RollbackVisitor(database), database);
        } finally {
            ExecutorService.getInstance().setExecutor(database, oldTemplate);
            lockService.releaseLock();
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
        dropAll(getDatabase().getDefaultSchemaName());
    }

    /**
     * Drops all database objects owned by the current user.
     */
    public final void dropAll(String... schemas) throws DatabaseException {
        try {
            LockService.getInstance(database).waitForLock();

            for (String schema : schemas) {
                log.info("Dropping Database Objects in schema: " + database.convertRequestedSchemaToSchema(schema));
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
                LockService.getInstance(database).releaseLock();
            } catch (LockException e) {
                log.severe("Unable to release lock: " + e.getMessage());
            }
        }
    }

    /**
     * 'Tags' the database for future rollback
     */
    public void tag(String tagString) throws LiquibaseException {
        LockService lockService = LockService.getInstance(database);
        lockService.waitForLock();

        try {
            checkDatabaseChangeLogTable(false, null, null);
            getDatabase().tag(tagString);
        } finally {
            lockService.releaseLock();
        }
    }


    public void updateTestingRollback(String contexts) throws LiquibaseException {
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        Date baseDate = new Date();
        update(contexts);
        rollback(baseDate, contexts);
        update(contexts);
    }

    public void checkDatabaseChangeLogTable(boolean updateExistingNullChecksums, DatabaseChangeLog databaseChangeLog, String contexts) throws LiquibaseException {
        if (updateExistingNullChecksums && databaseChangeLog == null) {
            throw new LiquibaseException("changeLog parameter is required if updating existing checksums");
        }
        String[] splitContexts = null;
        if (StringUtils.trimToNull(contexts) != null) {
            splitContexts = contexts.split(",");
        }
        getDatabase().checkDatabaseChangeLogTable(updateExistingNullChecksums, databaseChangeLog, splitContexts);
        if (!LockService.getInstance(database).hasChangeLogLock()) {
            getDatabase().checkDatabaseChangeLogLockTable();
        }
    }

    /**
     * Returns true if it is "save" to migrate the database.
     * Currently, "safe" is defined as running in an output-sql mode or against a database on localhost.
     * It is fine to run Liquibase against a "non-safe" database, the method is mainly used to determine if the user
     * should be prompted before continuing.
     */
    public boolean isSafeToRunMigration() throws DatabaseException {
        return getDatabase().isLocalDatabase();
    }

    /**
     * Display change log lock information.
     */
    public DatabaseChangeLogLock[] listLocks() throws LiquibaseException {
        checkDatabaseChangeLogTable(false, null, null);

        return LockService.getInstance(getDatabase()).listLocks();
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

        LockService.getInstance(getDatabase()).forceReleaseLock();
    }

    public List<ChangeSet> listUnrunChangeSets(String contexts) throws LiquibaseException {
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));

        DatabaseChangeLog changeLog = ChangeLogParserFactory.getInstance().getParser(changeLogFile, resourceAccessor).parse(changeLogFile, changeLogParameters, resourceAccessor);

        changeLog.validate(database, contexts);

        ChangeLogIterator logIterator = getStandardChangelogIterator(contexts, changeLog);

        ListVisitor visitor = new ListVisitor();
        logIterator.run(visitor, database);
        return visitor.getSeenChangeSets();
    }

    public void reportStatus(boolean verbose, String contexts, Writer out) throws LiquibaseException {
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

    /**
     * Sets checksums to null so they will be repopulated next run
     */
    public void clearCheckSums() throws LiquibaseException {
        log.info("Clearing database change log checksums");
        LockService lockService = LockService.getInstance(database);
        lockService.waitForLock();

        try {
            checkDatabaseChangeLogTable(false, null, null);

            UpdateStatement updateStatement = new UpdateStatement(getDatabase().getLiquibaseSchemaName(), getDatabase().getDatabaseChangeLogTableName());
            updateStatement.addNewColumnValue("MD5SUM", null);
            ExecutorService.getInstance().getExecutor(database).execute(updateStatement);
            getDatabase().commit();
        } finally {
            lockService.releaseLock();
        }
    }

    public void generateDocumentation(String outputDirectory) throws LiquibaseException {
    	// call without context
    	generateDocumentation(outputDirectory, null);
    }
    
    public void generateDocumentation(String outputDirectory, String contexts) throws LiquibaseException {
        log.info("Generating Database Documentation");
        changeLogParameters.setContexts(StringUtils.splitAndTrim(contexts, ","));
        LockService lockService = LockService.getInstance(database);
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

    public Diff diff(Database referenceDatabase, Database targetDatabase) {
        return new Diff(referenceDatabase, targetDatabase);
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
            setChangeLogParameter("database.autoIncrementClause", database.getAutoIncrementClause());
            setChangeLogParameter("database.currentDateTimeFunction", database.getCurrentDateTimeFunction());
            setChangeLogParameter("database.databaseChangeLogLockTableName", database.getDatabaseChangeLogLockTableName());
            setChangeLogParameter("database.databaseChangeLogTableName", database.getDatabaseChangeLogTableName());
            setChangeLogParameter("database.databaseMajorVersion", database.getDatabaseMajorVersion());
            setChangeLogParameter("database.databaseMinorVersion", database.getDatabaseMinorVersion());
            setChangeLogParameter("database.databaseProductName", database.getDatabaseProductName());
            setChangeLogParameter("database.databaseProductVersion", database.getDatabaseProductVersion());
            setChangeLogParameter("database.defaultCatalogName", database.getDefaultCatalogName());
            setChangeLogParameter("database.defaultSchemaName", database.getDefaultSchemaName());
            setChangeLogParameter("database.lineComment", database.getLineComment());
            setChangeLogParameter("database.liquibaseSchemaName", database.getLiquibaseSchemaName());
            setChangeLogParameter("database.typeName", database.getTypeName());
            setChangeLogParameter("database.isLocalDatabase", database.isLocalDatabase());
            setChangeLogParameter("database.requiresPassword", database.requiresPassword());
            setChangeLogParameter("database.requiresUsername", database.requiresUsername());
            setChangeLogParameter("database.supportsForeignKeyDisable", database.supportsForeignKeyDisable());
            setChangeLogParameter("database.supportsInitiallyDeferrableColumns", database.supportsInitiallyDeferrableColumns());
            setChangeLogParameter("database.supportsRestrictForeignKeys", database.supportsRestrictForeignKeys());
            setChangeLogParameter("database.supportsSchemas", database.supportsSchemas());
            setChangeLogParameter("database.supportsSequences", database.supportsSequences());
            setChangeLogParameter("database.supportsTablespaces", database.supportsTablespaces());
    }
}
