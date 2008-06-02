package liquibase;

import liquibase.ChangeSet;
import liquibase.DatabaseChangeLog;
import liquibase.DatabaseChangeLogLock;
import liquibase.FileOpener;
import liquibase.database.Database;
import liquibase.database.sql.UpdateStatement;
import liquibase.database.template.JdbcOutputTemplate;
import liquibase.database.template.JdbcTemplate;
import liquibase.exception.JDBCException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LockException;
import liquibase.lock.LockHandler;
import liquibase.log.LogFactory;
import liquibase.parser.ChangeLogIterator;
import liquibase.parser.ChangeLogParser;
import liquibase.parser.filter.*;
import liquibase.parser.visitor.*;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StreamUtil;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Writer;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Core LiquiBase facade.
 * Although there are several ways of executing LiquiBase (Ant, command line, etc.) they are all wrappers around this class.
 */
public class Liquibase {

    public static final String SHOULD_RUN_SYSTEM_PROPERTY = "liquibase.should.run";

    private String changeLogFile;
    private FileOpener fileOpener;

    private Database database;
    private Logger log;

    public Liquibase(String changeLogFile, FileOpener fileOpener, Database database) {
        log = LogFactory.getLogger();

        if (changeLogFile != null) {
            this.changeLogFile = changeLogFile.replace('\\', '/');  //convert to standard / if usign absolute path on windows
        }
        this.fileOpener = fileOpener;

        this.database = database;
    }

    public Database getDatabase() {
        return database;
    }

    /**
     * FileOpener to use for accessing changelog files.
     */
    public FileOpener getFileOpener() {
        return fileOpener;
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

        LockHandler lockHandler = LockHandler.getInstance(database);
        lockHandler.waitForLock();

        try {
            database.checkDatabaseChangeLogTable();

            DatabaseChangeLog changeLog = new ChangeLogParser().parse(changeLogFile, fileOpener);
            changeLog.validate(database);
            ChangeLogIterator logIterator = new ChangeLogIterator(changeLog,
                    new ShouldRunChangeSetFilter(database),
                    new ContextChangeSetFilter(contexts),
                    new DbmsChangeSetFilter(database));

            logIterator.run(new UpdateVisitor(database));
        } catch (LiquibaseException e) {
            throw e;
        } finally {
            try {
                lockHandler.releaseLock();
            } catch (LockException e) {
                log.log(Level.SEVERE, "Could not release lock", e);
            }
        }
    }

    public void update(String contexts, Writer output) throws LiquibaseException {
        JdbcTemplate oldTemplate = database.getJdbcTemplate();
        JdbcOutputTemplate outputTemplate = new JdbcOutputTemplate(output, database);
        database.setJdbcTemplate(outputTemplate);

        outputHeader("Update Database Script");

        update(contexts);

        try {
            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }

        database.setJdbcTemplate(oldTemplate);
    }

    public void update(int changesToApply, String contexts) throws LiquibaseException {

        LockHandler lockHandler = LockHandler.getInstance(database);
        lockHandler.waitForLock();

        try {
            database.checkDatabaseChangeLogTable();

            DatabaseChangeLog changeLog = new ChangeLogParser().parse(changeLogFile, fileOpener);
            changeLog.validate(database);

            ChangeLogIterator logIterator = new ChangeLogIterator(changeLog,
                    new ShouldRunChangeSetFilter(database),
                    new ContextChangeSetFilter(contexts),
                    new DbmsChangeSetFilter(database),
                    new CountChangeSetFilter(changesToApply));

            logIterator.run(new UpdateVisitor(database));
        } finally {
            lockHandler.releaseLock();
        }
    }

    public void update(int changesToApply, String contexts, Writer output) throws LiquibaseException {
        JdbcTemplate oldTemplate = database.getJdbcTemplate();
        JdbcOutputTemplate outputTemplate = new JdbcOutputTemplate(output, database);
        database.setJdbcTemplate(outputTemplate);

        outputHeader("Update "+changesToApply+" Change Sets Database Script");

        update(changesToApply, contexts);

        try {
            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }

        database.setJdbcTemplate(oldTemplate);
    }

    private void outputHeader(String message) throws JDBCException {
        database.getJdbcTemplate().comment("*********************************************************************");
        database.getJdbcTemplate().comment(message);
        database.getJdbcTemplate().comment("*********************************************************************");
        database.getJdbcTemplate().comment("Change Log: " + changeLogFile);
        database.getJdbcTemplate().comment("Ran at: " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date()));
        database.getJdbcTemplate().comment("Against: " + getDatabase().getConnectionUsername() + "@" + getDatabase().getConnectionURL());
        database.getJdbcTemplate().comment("LiquiBase version: " + LiquibaseUtil.getBuildVersion());
        database.getJdbcTemplate().comment("*********************************************************************" + StreamUtil.getLineSeparator());
    }

    public void rollback(int changesToRollback, String contexts, Writer output) throws LiquibaseException {
        JdbcTemplate oldTemplate = database.getJdbcTemplate();
        database.setJdbcTemplate(new JdbcOutputTemplate(output, database));

        outputHeader("Rollback " + changesToRollback + " Change(s) Script");

        rollback(changesToRollback, contexts);

        try {
            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }
        database.setJdbcTemplate(oldTemplate);
    }

    public void rollback(int changesToRollback, String contexts) throws LiquibaseException {
        LockHandler lockHandler = LockHandler.getInstance(database);
        lockHandler.waitForLock();

        try {
            database.checkDatabaseChangeLogTable();

            DatabaseChangeLog changeLog = new ChangeLogParser().parse(changeLogFile, fileOpener);
            changeLog.validate(database);

            ChangeLogIterator logIterator = new ChangeLogIterator(changeLog,
                    new AlreadyRanChangeSetFilter(database.getRanChangeSetList()),
                    new ContextChangeSetFilter(contexts),
                    new DbmsChangeSetFilter(database),
                    new CountChangeSetFilter(changesToRollback));

            logIterator.run(new RollbackVisitor(database));
        } finally {
            try {
                lockHandler.releaseLock();
            } catch (LockException e) {
                log.log(Level.SEVERE, "Error releasing lock", e);
            }
        }
    }

    public void rollback(String tagToRollBackTo, String contexts, Writer output) throws LiquibaseException {
        JdbcTemplate oldTemplate = database.getJdbcTemplate();
        database.setJdbcTemplate(new JdbcOutputTemplate(output, database));

        outputHeader("Rollback to '" + tagToRollBackTo + "' Script");

        rollback(tagToRollBackTo, contexts);

        try {
            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }
        database.setJdbcTemplate(oldTemplate);
    }

    public void rollback(String tagToRollBackTo, String contexts) throws LiquibaseException {
        LockHandler lockHandler = LockHandler.getInstance(database);
        lockHandler.waitForLock();

        try {
            database.checkDatabaseChangeLogTable();

            DatabaseChangeLog changeLog = new ChangeLogParser().parse(changeLogFile, fileOpener);
            changeLog.validate(database);
            
            ChangeLogIterator logIterator = new ChangeLogIterator(changeLog,
                    new AfterTagChangeSetFilter(tagToRollBackTo, database.getRanChangeSetList()),
                    new ContextChangeSetFilter(contexts),
                    new DbmsChangeSetFilter(database));

            logIterator.run(new RollbackVisitor(database));
        } finally {
            lockHandler.releaseLock();
        }
    }

    public void rollback(Date dateToRollBackTo, String contexts, Writer output) throws LiquibaseException {
        JdbcTemplate oldTemplate = database.getJdbcTemplate();
        database.setJdbcTemplate(new JdbcOutputTemplate(output, database));

        outputHeader("Rollback to " + dateToRollBackTo + " Script");

        rollback(dateToRollBackTo, contexts);

        try {
            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }
        database.setJdbcTemplate(oldTemplate);
    }

    public void rollback(Date dateToRollBackTo, String contexts) throws LiquibaseException {
        LockHandler lockHandler = LockHandler.getInstance(database);
        lockHandler.waitForLock();

        try {
            database.checkDatabaseChangeLogTable();

            DatabaseChangeLog changeLog = new ChangeLogParser().parse(changeLogFile, fileOpener);
            changeLog.validate(database);
            
            ChangeLogIterator logIterator = new ChangeLogIterator(changeLog,
                    new ExecutedAfterChangeSetFilter(dateToRollBackTo, database.getRanChangeSetList()),
                    new ContextChangeSetFilter(contexts),
                    new DbmsChangeSetFilter(database));

            logIterator.run(new RollbackVisitor(database));
        } finally {
            lockHandler.releaseLock();
        }
    }

    public void changeLogSync(String contexts, Writer output) throws LiquibaseException {

        JdbcOutputTemplate outputTemplate = new JdbcOutputTemplate(output, database);
        JdbcTemplate oldTemplate = database.getJdbcTemplate();
        database.setJdbcTemplate(outputTemplate);

        outputHeader("SQL to add all changesets to database history table");

        changeLogSync(contexts);

        try {
            output.flush();
        } catch (IOException e) {
            throw new LiquibaseException(e);
        }

        database.setJdbcTemplate(oldTemplate);
    }

    public void changeLogSync(String contexts) throws LiquibaseException {
        LockHandler lockHandler = LockHandler.getInstance(database);
        lockHandler.waitForLock();

        try {
            database.checkDatabaseChangeLogTable();

            DatabaseChangeLog changeLog = new ChangeLogParser().parse(changeLogFile, fileOpener);
            changeLog.validate(database);

            ChangeLogIterator logIterator = new ChangeLogIterator(changeLog,
                    new NotRanChangeSetFilter(database.getRanChangeSetList()),
                    new ContextChangeSetFilter(contexts),
                    new DbmsChangeSetFilter(database));

            logIterator.run(new ChangeLogSyncVisitor(database));
        } finally {
            lockHandler.releaseLock();
        }
    }

    public void futureRollbackSQL(String contexts, Writer output) throws LiquibaseException {
        JdbcOutputTemplate outputTemplate = new JdbcOutputTemplate(output, database);
        JdbcTemplate oldTemplate = database.getJdbcTemplate();
        database.setJdbcTemplate(outputTemplate);

        outputHeader("SQL to roll back currently unexecuted changes");

        LockHandler lockHandler = LockHandler.getInstance(database);
        lockHandler.waitForLock();

        try {
            database.checkDatabaseChangeLogTable();

            DatabaseChangeLog changeLog = new ChangeLogParser().parse(changeLogFile, fileOpener);
            changeLog.validate(database);

            ChangeLogIterator logIterator = new ChangeLogIterator(changeLog,
                    new NotRanChangeSetFilter(database.getRanChangeSetList()),
                    new ContextChangeSetFilter(contexts),
                    new DbmsChangeSetFilter(database));

            logIterator.run(new RollbackVisitor(database));
        } finally {
            database.setJdbcTemplate(oldTemplate);
            lockHandler.releaseLock();
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
    public final void dropAll() throws JDBCException, LockException {
        dropAll(getDatabase().getDefaultSchemaName());
    }

    /**
     * Drops all database objects owned by the current user.
     */
    public final void dropAll(String... schemas) throws JDBCException {
        try {
            LockHandler.getInstance(database).waitForLock();

            for (String schema : schemas) {
                log.info("Dropping Database Objects in schema: " + database.convertRequestedSchemaToSchema(schema));
                checkDatabaseChangeLogTable();
                getDatabase().dropDatabaseObjects(schema);
                checkDatabaseChangeLogTable();
                log.finest("Objects dropped successfully");
            }
        } catch (JDBCException e) {
            throw e;
        } catch (Exception e) {
            throw new JDBCException(e);
        } finally {
            try {
                LockHandler.getInstance(database).releaseLock();
            } catch (LockException e) {
                log.severe("Unable to release lock: " + e.getMessage());
            }
        }
    }

    /**
     * 'Tags' the database for future rollback
     */
    public void tag(String tagString) throws JDBCException {
        getDatabase().tag(tagString);
    }


    public void checkDatabaseChangeLogTable() throws JDBCException {
        getDatabase().checkDatabaseChangeLogTable();
        getDatabase().checkDatabaseChangeLogLockTable();
    }

    /**
     * Returns true if it is "save" to migrate the database.
     * Currently, "safe" is defined as running in an output-sql mode or against a database on localhost.
     * It is fine to run LiquiBase against a "non-safe" database, the method is mainly used to determine if the user
     * should be prompted before continuing.
     */
    public boolean isSafeToRunMigration() throws JDBCException {
        return !getDatabase().getJdbcTemplate().executesStatements() || getDatabase().getConnectionURL().indexOf("localhost") >= 0;
    }

    /**
     * Display change log lock information.
     */
    public DatabaseChangeLogLock[] listLocks() throws JDBCException, IOException, LockException {
        checkDatabaseChangeLogTable();

        return LockHandler.getInstance(getDatabase()).listLocks();
    }

    public void reportLocks(PrintStream out) throws LockException, IOException, JDBCException {
        DatabaseChangeLogLock[] locks = listLocks();
        out.println("Database change log locks for " + getDatabase().getConnectionUsername() + "@" + getDatabase().getConnectionURL());
        if (locks.length == 0) {
            out.println(" - No locks");
        }
        for (DatabaseChangeLogLock lock : locks) {
            out.println(" - " + lock.getLockedBy() + " at " + DateFormat.getDateTimeInstance().format(lock.getLockGranted()));
        }

    }

    public void forceReleaseLocks() throws LockException, IOException, JDBCException {
        checkDatabaseChangeLogTable();

        LockHandler.getInstance(getDatabase()).forceReleaseLock();
    }

    public List<ChangeSet> listUnrunChangeSets(String contexts) throws LiquibaseException {
        LockHandler lockHandler = LockHandler.getInstance(database);
        lockHandler.waitForLock();

        try {
            database.checkDatabaseChangeLogTable();

            DatabaseChangeLog changeLog = new ChangeLogParser().parse(changeLogFile, fileOpener);
            changeLog.validate(database);

            ChangeLogIterator logIterator = new ChangeLogIterator(changeLog,
                    new ShouldRunChangeSetFilter(database),
                    new ContextChangeSetFilter(contexts),
                    new DbmsChangeSetFilter(database));

            ListVisitor visitor = new ListVisitor();
            logIterator.run(visitor);
            return visitor.getSeenChangeSets();
        } finally {
            lockHandler.releaseLock();
        }
    }

    public void reportStatus(boolean verbose, String contexts, Writer out) throws LiquibaseException {
        try {
            List<ChangeSet> unrunChangeSets = listUnrunChangeSets(contexts);
            out.append(String.valueOf(unrunChangeSets.size()));
            out.append(" change sets have not been applied to ");
            out.append(getDatabase().getConnectionUsername());
            out.append("@");
            out.append(getDatabase().getConnectionURL());
            out.append(StreamUtil.getLineSeparator());
            if (verbose) {
                for (ChangeSet changeSet : unrunChangeSets) {
                    out.append("     ").append(changeSet.toString(false)).append(StreamUtil.getLineSeparator());
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
        LockHandler lockHandler = LockHandler.getInstance(database);
        lockHandler.waitForLock();

        try {
            database.checkDatabaseChangeLogTable();

            UpdateStatement updateStatement = new UpdateStatement(getDatabase().getDefaultSchemaName(), getDatabase().getDatabaseChangeLogTableName());
            updateStatement.addNewColumnValue("MD5SUM", null);
            getDatabase().getJdbcTemplate().execute(updateStatement);
            getDatabase().commit();
        } finally {
            lockHandler.releaseLock();
        }
    }

    public void generateDocumentation(String outputDirectory) throws LiquibaseException {
        log.info("Generating Database Documentation");
        LockHandler lockHandler = LockHandler.getInstance(database);
        lockHandler.waitForLock();

        try {
            database.checkDatabaseChangeLogTable();

            DatabaseChangeLog changeLog = new ChangeLogParser().parse(changeLogFile, fileOpener);
            changeLog.validate(database);

            ChangeLogIterator logIterator = new ChangeLogIterator(changeLog,
                    new DbmsChangeSetFilter(database));

            DBDocVisitor visitor = new DBDocVisitor(database);
            logIterator.run(visitor);

            visitor.writeHTML(new File(outputDirectory), fileOpener);
        } catch (IOException e) {
            throw new LiquibaseException(e);
        } finally {
            lockHandler.releaseLock();
        }

//        try {
//            if (!LockHandler.getInstance(database).waitForLock()) {
//                return;
//            }
//
//            DBDocChangeLogHandler changeLogHandler = new DBDocChangeLogHandler(outputDirectory, this, changeLogFile,fileOpener);
//            runChangeLogs(changeLogHandler);
//
//            changeLogHandler.writeHTML(this);
//        } finally {
//            releaseLock();
//        }
    }

    /**
     * Checks changelogs for bad MD5Sums and preconditions before attempting a migration
     */
    public void validate() throws LiquibaseException {

        DatabaseChangeLog changeLog = new ChangeLogParser().parse(changeLogFile, fileOpener);
        changeLog.validate(database);
    }
}
