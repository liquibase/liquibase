package liquibase.migrator;

import liquibase.database.*;
import liquibase.migrator.exception.DatabaseHistoryException;
import liquibase.migrator.exception.JDBCException;
import liquibase.migrator.exception.MigrationFailedException;
import liquibase.migrator.exception.ValidationFailedException;
import liquibase.migrator.parser.*;
import liquibase.util.StreamUtil;
import org.xml.sax.*;

import javax.swing.*;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.sql.*;
import java.text.DateFormat;
import java.util.*;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Core class of the LiquiBase migrator.
 * Although there are several ways of executing LiquiBase (Ant, command line, etc.) they are all wrappers around this class.
 * <p/>
 * <b>Using Migrator directly</b>
 * <ol>
 * <li>Construct an instance of Migrator passing in the changelog file and file opener.</li>
 * <li>Call migrator.init(connection)</li>
 * <li>Set any contexts with the setContexts() method</li>
 * <li>Set the execution mode with setMode()</li>
 * <li>Call migrate()</li>
 * </ol>
 */
public class Migrator {

    // These modes tell the program whether to execute the statements against the database
    // Or to output them in some file to be ran later manually

    public enum Mode {
        EXECUTE_MODE,
        EXECUTE_ROLLBACK_MODE,
        OUTPUT_SQL_MODE,
        OUTPUT_ROLLBACK_SQL_MODE,
        OUTPUT_FUTURE_ROLLBACK_SQL_MODE,
        OUTPUT_CHANGELOG_ONLY_SQL_MODE,
        FIND_UNRUN_CHANGESETS_MODE,
    }

    public static final String SHOULD_RUN_SYSTEM_PROPERTY = "database.migrator.should.run";

    public static final String DEFAULT_LOG_NAME = "database.migrator";

    private static boolean outputtedHeader = false;

    private ChangeFactory changeFactory = new ChangeFactory();

    private XMLReader xmlReader;

    private String changeLogFile;
    private FileOpener fileOpener;
    private Mode mode;
    private Writer outputSQLWriter;
    private Date rollbackToDate;
    private String rollbackToTag;
    private Integer rollbackCount;

    private Database database;
    private Logger log;
    private Set<String> contexts = new HashSet<String>();

    private boolean hasChangeLogLock = false;
    private long changeLogLockWaitTime = 1000 * 60 * 5;  //default to 5 mins

    private List<RanChangeSet> ranChangeSetList;
    private String buildVersion;

    private boolean wasValidationRan = false;

    public Migrator(String changeLogFile, FileOpener fileOpener) {
        this(changeLogFile, fileOpener, false);
    }

    protected Migrator(String changeLogFile, FileOpener fileOpener, boolean alreadyHasChangeLogLock) {
        log = Logger.getLogger(Migrator.DEFAULT_LOG_NAME);

        this.changeLogFile = changeLogFile;
        this.fileOpener = fileOpener;
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        if (System.getProperty("java.vm.version").startsWith("1.4")) {
            saxParserFactory.setValidating(false);
            saxParserFactory.setNamespaceAware(false);
        } else {
            saxParserFactory.setValidating(true);
            saxParserFactory.setNamespaceAware(true);
        }
        try {
            SAXParser parser = saxParserFactory.newSAXParser();
            try {
                parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
            } catch (SAXNotRecognizedException e) {
                ; //ok, parser must not support it
            } catch (SAXNotSupportedException e) {
                ; //ok, parser must not support it
            }
            xmlReader = parser.getXMLReader();
            xmlReader.setEntityResolver(new MigratorSchemaResolver());
            xmlReader.setErrorHandler(new ErrorHandler() {
                public void warning(SAXParseException exception) throws SAXException {
                    Logger.getLogger(Migrator.DEFAULT_LOG_NAME).warning(exception.getMessage());
                    throw exception;
                }

                public void error(SAXParseException exception) throws SAXException {
                    Logger.getLogger(Migrator.DEFAULT_LOG_NAME).severe(exception.getMessage());
                    throw exception;
                }

                public void fatalError(SAXParseException exception) throws SAXException {
                    Logger.getLogger(Migrator.DEFAULT_LOG_NAME).severe(exception.getMessage());
                    throw exception;
                }
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        setMode(Mode.EXECUTE_MODE);
        this.hasChangeLogLock = alreadyHasChangeLogLock;

        this.buildVersion = findVersion();
    }

    private String findVersion() {
        Properties buildInfo = new Properties();
        URL buildInfoFile = Thread.currentThread().getContextClassLoader().getResource("buildinfo.properties");
        try {
            if (buildInfoFile == null) {
                return "UNKNOWN";
            } else {
                InputStream in = buildInfoFile.openStream();

                buildInfo.load(in);
                String o = (String) buildInfo.get("build.version");
                if (o == null) {
                    return "UNKNOWN";
                } else {
                    return o;
                }
            }
        } catch (IOException e) {
            return "UNKNOWN";
        }
    }

    /**
     * Initializes the Migrator with the given connection.  Needs to be called before actually using the Migrator.
     */
    public void init(Connection connection) throws JDBCException {
        // Array Of all the implemented databases
        database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(connection);
        database.setConnection(connection);
        try {
            database.getConnection().setAutoCommit(false);
        } catch (SQLException e) {
            //must not be able to set auto-commit, or is already set
        }
    }

    /**
     * Returns the ChangeFactory for converting tag strings to Change implementations.
     */
    public ChangeFactory getChangeFactory() {
        return changeFactory;
    }

    public String getBuildVersion() {
        return buildVersion;
    }

    public Database getDatabase() {
        return database;
    }

    /**
     * Sets the mode of opereration for the Migrator.
     */
    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }


    /**
     * The Writer to append SQL if not executing directly against the database.
     */
    public Writer getOutputSQLWriter() {
        return outputSQLWriter;
    }

    public void setOutputSQLWriter(Writer outputSQLWriter) {
        this.outputSQLWriter = outputSQLWriter;
    }

    /**
     * Date to rollback to if executing in rollback mode.
     */
    public Date getRollbackToDate() {
        if (rollbackToDate == null) {
            return null;
        }
        return (Date) rollbackToDate.clone();

    }

    public void setRollbackToDate(Date rollbackToDate) {
        if (rollbackToDate != null) {
            this.rollbackToDate = new Date(rollbackToDate.getTime());
        }
    }

    /**
     * Tag to rollback to if executing in rollback mode.
     */
    public String getRollbackToTag() {
        return rollbackToTag;
    }

    public void setRollbackToTag(String rollbackToTag) {
        this.rollbackToTag = rollbackToTag;
    }

    /**
     * Number of statements to rollback to if executing in rollback mode.
     */
    public Integer getRollbackCount() {
        return rollbackCount;
    }

    public void setRollbackCount(Integer rollbackCount) {
        this.rollbackCount = rollbackCount;
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

    /**
     * Returns the ChangeSets that have been run against the current database.
     */
    public List<RanChangeSet> getRanChangeSetList() throws JDBCException {
        try {
            String databaseChangeLogTableName = getDatabase().getDatabaseChangeLogTableName();
            if (ranChangeSetList == null) {
                ranChangeSetList = new ArrayList<RanChangeSet>();
                if (getDatabase().doesChangeLogTableExist()) {
                    log.info("Reading from " + databaseChangeLogTableName);
                    String sql = "SELECT * FROM " + databaseChangeLogTableName + " ORDER BY dateExecuted asc".toUpperCase();
                    Statement statement = getDatabase().getConnection().createStatement();
                    ResultSet rs = statement.executeQuery(sql);
                    while (rs.next()) {
                        String fileName = rs.getString("filename");
                        String author = rs.getString("author");
                        String id = rs.getString("id");
                        String md5sum = rs.getString("md5sum");
                        Date dateExecuted = rs.getTimestamp("dateExecuted");
                        String tag = rs.getString("tag");
                        RanChangeSet ranChangeSet = new RanChangeSet(fileName, id, author, md5sum, dateExecuted, tag);
                        ranChangeSetList.add(ranChangeSet);
                    }
                    rs.close();
                    statement.close();
                }
            }
            return ranChangeSetList;
        } catch (SQLException e) {
            throw new JDBCException(e);
        }
    }




    /**
     * Checks changelogs for bad MD5Sums and preconditions before attempting a migration
     */
    public void validate() throws MigrationFailedException, IOException, JDBCException {
        try {
            if (!waitForLock()) {
                return;
            }

            ValidateChangeLogHandler validateChangeLogHandler = new ValidateChangeLogHandler(this, changeLogFile);
            runChangeLogs(validateChangeLogHandler);
            if (!validateChangeLogHandler.validationPassed()) {
                throw new ValidationFailedException(validateChangeLogHandler.getInvalidMD5Sums(), validateChangeLogHandler.getFailedPreconditions(), validateChangeLogHandler.getDuplicateChangeSets());
            }
        } finally {
            releaseLock();
        }
    }

    /**
     * The primary method to call on Migrator to actually do work.
     * To use the Migrator, initialize it with the init(Connection) method, set the mode, outputSQLWriter, etc. as need be,
     * then call the migrate() method.
     */
    public final void migrate() throws MigrationFailedException {
        try {
            if (!wasValidationRan()) {
                validate();
                wasValidationRan = true;
            }

            if (!waitForLock()) {
                return;
            }

            Writer outputSQLWriter = getOutputSQLWriter();

            if (outputSQLWriter == null) {
                log.info("Reading changelog " + changeLogFile);
            } else {
                if (!outputtedHeader) {
                    outputSQLWriter.write("--------------------------------------------------------------------------------------" + StreamUtil.getLineSeparator());
                    if (mode.equals(Mode.OUTPUT_SQL_MODE)) {
                        outputSQLWriter.write("-- SQL to update database to newest version" + StreamUtil.getLineSeparator());
                    } else if (mode.equals(Mode.OUTPUT_CHANGELOG_ONLY_SQL_MODE)) {
                        outputSQLWriter.write("-- SQL to add all changesets to database history table" + StreamUtil.getLineSeparator());
                    } else if (mode.equals(Mode.OUTPUT_ROLLBACK_SQL_MODE)) {
                        String stateDescription;
                        if (getRollbackToTag() != null) {
                            stateDescription = getRollbackToTag();
                        } else if (getRollbackToDate() != null) {
                            stateDescription = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(getRollbackToDate());
                        } else if (getRollbackCount() != null) {
                            stateDescription = getRollbackCount() + " change set ago";
                        } else {
                            throw new RuntimeException("Unknown rollback type");
                        }
                        outputSQLWriter.write("-- SQL to roll-back database to the state it was at " + stateDescription + StreamUtil.getLineSeparator());
                    } else if (mode.equals(Mode.OUTPUT_FUTURE_ROLLBACK_SQL_MODE)) {
                        outputSQLWriter.write("-- SQL to roll-back database from an updated buildVersion back to current version" + StreamUtil.getLineSeparator());
                    } else {
                        throw new MigrationFailedException("Unexpected output mode: " + mode);
                    }
                    outputSQLWriter.write("-- Change Log: " + changeLogFile + StreamUtil.getLineSeparator());
                    outputSQLWriter.write("-- Ran at: " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date()) + StreamUtil.getLineSeparator());
                    outputSQLWriter.write("-- Against: " + getDatabase().getConnectionUsername() + "@" + getDatabase().getConnectionURL() + StreamUtil.getLineSeparator());
                    outputSQLWriter.write("--------------------------------------------------------------------------------------" + StreamUtil.getLineSeparator() + StreamUtil.getLineSeparator() + StreamUtil.getLineSeparator());
                    outputtedHeader = true;
                }
            }

            if (mode.equals(Mode.EXECUTE_MODE) || mode.equals(Mode.OUTPUT_SQL_MODE) || mode.equals(Mode.OUTPUT_CHANGELOG_ONLY_SQL_MODE)) {
                runChangeLogs(new UpdateDatabaseChangeLogHandler(this, changeLogFile));
            } else if (mode.equals(Mode.EXECUTE_ROLLBACK_MODE) || mode.equals(Mode.OUTPUT_ROLLBACK_SQL_MODE)) {
                RollbackDatabaseChangeLogHandler rollbackHandler;
                if (getRollbackToDate() != null) {
                    rollbackHandler = new RollbackDatabaseChangeLogHandler(this, changeLogFile, getRollbackToDate());
                } else if (getRollbackToTag() != null) {
                    if (!getDatabase().doesTagExist(getRollbackToTag())) {
                        throw new MigrationFailedException("'" + getRollbackToTag() + "' is not tag that exists in the database");
                    }

                    rollbackHandler = new RollbackDatabaseChangeLogHandler(this, changeLogFile, getRollbackToTag());
                } else if (getRollbackCount() != null) {
                    rollbackHandler = new RollbackDatabaseChangeLogHandler(this, changeLogFile, getRollbackCount());
                } else {
                    throw new RuntimeException("Don't know what to rollback to");
                }
                runChangeLogs(rollbackHandler);
                ChangeSet unrollbackableChangeSet = rollbackHandler.getUnRollBackableChangeSet();
                if (unrollbackableChangeSet == null) {
                    rollbackHandler.doRollback();
                } else {
                    throw new MigrationFailedException("Cannot roll back changelog to selected date due to change set " + unrollbackableChangeSet);
                }
            } else if (mode.equals(Mode.OUTPUT_FUTURE_ROLLBACK_SQL_MODE)) {
                RollbackFutureDatabaseChangeLogHandler rollbackHandler = new RollbackFutureDatabaseChangeLogHandler(this, changeLogFile);
                runChangeLogs(rollbackHandler);
                ChangeSet unrollbackableChangeSet = rollbackHandler.getUnRollBackableChangeSet();
                if (unrollbackableChangeSet == null) {
                    rollbackHandler.doRollback();
                } else {
                    throw new MigrationFailedException("Will not be able to rollback changes due to change set " + unrollbackableChangeSet);
                }
            } else if (mode.equals(Mode.FIND_UNRUN_CHANGESETS_MODE)) {
                runChangeLogs(new FindChangeSetsHandler(this, changeLogFile));

//                List<ChangeSet> unrunChangeSets = FindChangeSetsHandler.getUnrunChangeSets();
//                System.out.println(unrunChangeSets.size()+" change sets have not been applied to "+getDatabase().getConnectionUsername() + "@" + getDatabase().getConnectionURL());
            } else {
                throw new MigrationFailedException("Unknown mode: " + getMode());
            }
            if (outputSQLWriter != null) {
                outputSQLWriter.flush();
            }
        } catch (MigrationFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new MigrationFailedException(e);
        } finally {
            releaseLock();
        }
    }

    protected ValidateChangeLogHandler getValidatChangeLogHandler() {
        return new ValidateChangeLogHandler(this, changeLogFile);
    }

    private boolean waitForLock() throws JDBCException, MigrationFailedException, IOException {
        if (!hasChangeLogLock) {
            checkDatabaseChangeLogTable();
        }

        boolean locked = false;
        long timeToGiveUp = new Date().getTime() + changeLogLockWaitTime;
        while (!locked && new Date().getTime() < timeToGiveUp) {
            locked = acquireLock();
            if (!locked) {
                log.info("Waiting for changelog lock....");
                try {
                    Thread.sleep(1000 * 10);
                } catch (InterruptedException e) {
                    ;
                }
            }
        }

        if (!locked) {
            DatabaseChangeLogLock[] locks = listLocks();
            String lockedBy;
            if (locks.length > 0) {
                DatabaseChangeLogLock lock = locks[0];
                lockedBy = lock.getLockedBy() + " since " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(lock.getLockGranted());
            } else {
                lockedBy = "UNKNOWN";
            }
            log.severe("Could not acquire change log lock.  Currently locked by " + lockedBy);
            return false;
        }

        return true;

    }


    protected boolean wasValidationRan() {
        return wasValidationRan;
    }

    /**
     * Drops all database objects owned by the current user.
     */
    public final void dropAll() throws MigrationFailedException {
        try {
            if (!waitForLock()) {
                return;
            }

            log.info("Dropping Database Objects in " + getDatabase().getSchemaName());
            getDatabase().dropDatabaseObjects();
            checkDatabaseChangeLogTable();
            log.finest("Objects dropped successfully");
        } catch (MigrationFailedException e) {
            throw e;
        } catch (Exception e) {
            throw new MigrationFailedException(e);
        } finally {
            releaseLock();
        }
    }

    public boolean acquireLock() throws MigrationFailedException {
        return hasChangeLogLock || getDatabase().acquireLock(this);
    }

    public void releaseLock() throws MigrationFailedException {
        getDatabase().releaseLock();
    }

    /**
     * Releases whatever locks are on the database change log table
     *
     * @throws MigrationFailedException
     */
    public void forceReleaseLock() throws MigrationFailedException, JDBCException, IOException {
        checkDatabaseChangeLogTable();

        getDatabase().releaseLock();
    }

    /**
     * 'Tags' the database for future rollback
     */
    public void tag(String tagString) throws MigrationFailedException {
        getDatabase().tag(tagString);
    }


    protected void checkDatabaseChangeLogTable() throws JDBCException, IOException {
        getDatabase().checkDatabaseChangeLogTable(this);
        getDatabase().checkDatabaseChangeLogLockTable(this);
    }

    private void runChangeLogs(ContentHandler contentHandler) throws MigrationFailedException {
        try {
            InputStream inputStream = getFileOpener().getResourceAsStream(changeLogFile);
            if (inputStream == null) {
                throw new MigrationFailedException(changeLogFile + " does not exist");
            }

            xmlReader.setContentHandler(contentHandler);
            xmlReader.parse(new InputSource(inputStream));
            inputStream.close();
        } catch (IOException e) {
            throw new MigrationFailedException("Error Reading Migration File: " + e.getMessage(), e);
        } catch (SAXParseException e) {
            throw new MigrationFailedException("Error parsing line " + e.getLineNumber() + " column " + e.getColumnNumber() + ": " + e.getMessage());
        } catch (SAXException e) {
            Throwable parentCause = e.getException();
            while (parentCause != null) {
                if (parentCause instanceof MigrationFailedException) {
                    throw ((MigrationFailedException) parentCause);
                }
                parentCause = parentCause.getCause();
            }
            String reason = e.getMessage();
            if (reason == null) {
                reason = "Unknown Reason";
            }

            throw new MigrationFailedException("Invalid Migration File: " + reason, e);
        }
    }

    /**
     * Returns true if it is "save" to migrate the database.
     * Currently, "safe" is defined as running in an output-sql mode or against a database on localhost.
     * It is fine to run the migrator against a "non-safe" database, the method is mainly used to determine if the user
     * should be prompted before continuing.
     */
    public boolean isSafeToRunMigration() throws JDBCException {
        if (Mode.OUTPUT_SQL_MODE.equals(getMode()) || Mode.OUTPUT_CHANGELOG_ONLY_SQL_MODE.equals(getMode())) {
            return true;
        }
        return getDatabase().getConnectionURL().indexOf("localhost") >= 0;
    }

    /**
     * Display change log lock information.
     */
    public DatabaseChangeLogLock[] listLocks() throws MigrationFailedException, JDBCException, IOException {
        checkDatabaseChangeLogTable();

        return getDatabase().listLocks();
    }

    /**
     * Set the contexts to execute.  If more than once, comma separate them.
     */
    public void setContexts(String contexts) {
        if (contexts != null) {
            String[] strings = contexts.split(",");
            for (String string : strings) {
                this.contexts.add(string.trim().toLowerCase());
            }
        }
    }

    public Set<String> getContexts() {
        return contexts;
    }

    /**
     * Returns the run status for the given ChangeSet
     */
    public ChangeSet.RunStatus getRunStatus(ChangeSet changeSet) throws JDBCException, DatabaseHistoryException {
        if (!getDatabase().doesChangeLogTableExist()) {
            return ChangeSet.RunStatus.NOT_RAN;
        }

        RanChangeSet foundRan = null;
        for (RanChangeSet ranChange : getRanChangeSetList()) {
            if (ranChange.isSameAs(changeSet)) {
                foundRan = ranChange;
                break;
            }
        }

        if (foundRan == null) {
            return ChangeSet.RunStatus.NOT_RAN;
        } else {
            if (foundRan.getMd5sum() == null) {
                try {
                    log.info("Updating NULL md5sum for " + changeSet.toString());
                    Migrator migrator = changeSet.getDatabaseChangeLog().getMigrator();
                    Connection connection = migrator.getDatabase().getConnection();
                    PreparedStatement updatePstmt = connection.prepareStatement("update DatabaseChangeLog set md5sum=? where id=? AND author=? AND filename=?".toUpperCase());
                    updatePstmt.setString(1, changeSet.getMd5sum());
                    updatePstmt.setString(2, changeSet.getId());
                    updatePstmt.setString(3, changeSet.getAuthor());
                    updatePstmt.setString(4, changeSet.getDatabaseChangeLog().getFilePath());

                    updatePstmt.executeUpdate();
                    updatePstmt.close();
                    connection.commit();
                } catch (SQLException e) {
                    throw new JDBCException(e);
                }

                return ChangeSet.RunStatus.ALREADY_RAN;
            } else {
                if (foundRan.getMd5sum().equals(changeSet.getMd5sum())) {
                    return ChangeSet.RunStatus.ALREADY_RAN;
                } else {
                    if (changeSet.shouldRunOnChange()) {
                        return ChangeSet.RunStatus.RUN_AGAIN;
                    } else {
                        return ChangeSet.RunStatus.INVALID_MD5SUM;
//                        throw new DatabaseHistoryException("MD5 Check for " + changeSet.toString() + " failed");
                    }
                }
            }
        }
    }

    /**
     * Displays swing-based dialog about running against a non-localhost database.
     * Returns true if the user selected that they are OK with that.
     */
    public boolean swingPromptForNonLocalDatabase() throws JDBCException {
        return JOptionPane.showConfirmDialog(null, "You are running a database migration against a non-local database." + StreamUtil.getLineSeparator() +
                "Database URL is: " + this.getDatabase().getConnectionURL() + StreamUtil.getLineSeparator() +
                "Username is: " + this.getDatabase().getConnectionUsername() + StreamUtil.getLineSeparator() + StreamUtil.getLineSeparator() +
                "Area you sure you want to do this?",
                "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION;
    }

    public boolean contextMatches(ChangeSet changeSet) {
        Set<String> requiredContexts = getContexts();
        String changeSetContext = changeSet.getContext();
        return changeSetContext == null || requiredContexts.size() == 0 || requiredContexts.contains(changeSetContext);
    }

    public List<ChangeSet> listUnrunChangeSets() throws JDBCException {
        setMode(Mode.FIND_UNRUN_CHANGESETS_MODE);
        try {
            migrate();
        } catch (MigrationFailedException e) {
            throw new JDBCException(e);
        }

        return FindChangeSetsHandler.getUnrunChangeSets();
    }


    /**
     * Sets checksums to null so they will be repopulated next run
     */
    public void clearCheckSums() throws JDBCException {
        Connection connection = getDatabase().getConnection();
        try {
            Statement statement = connection.createStatement();
            statement.execute("update databasechangelog set md5sum=null".toUpperCase());
            connection.commit();
        } catch (SQLException e) {
            throw new JDBCException(e);
        }
    }
}
