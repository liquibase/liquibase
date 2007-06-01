package liquibase.migrator;

import liquibase.util.StreamUtil;
import liquibase.database.*;
import org.xml.sax.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.swing.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URL;
import java.net.URI;
import java.sql.*;
import java.text.DateFormat;
import java.util.*;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Core class of the LiquiBase migrator.
 * Although there are several ways of executing LiquiBase (Ant, command line, etc.) they are all wrappers around this class.
 * <p>
 * <b>Using Migrator directly</b>
 * <ol>
 *      <li>Construct an instance of Migrator passing in the changelog file and file opener.</li>
 *      <li>Call migrator.init(connection)</li>
 *      <li>Set any contexts with the setContexts() method</li>
 *      <li>Set the execution mode with setMode()</li>
 *      <li>Call migrate()</li>
 * </ol>
 *
 */
public class Migrator {
    // These modes tell the program whether to execute the statements against the database
    // Or to output them in some file to be ran later manually
    public static final String EXECUTE_MODE = "execute";
    public static final String EXECUTE_ROLLBACK_MODE = "execute_rollback";
    public static final String OUTPUT_SQL_MODE = "save";
    public static final String OUTPUT_ROLLBACK_SQL_MODE = "rollback_save";
    public static final String OUTPUT_FUTURE_ROLLBACK_SQL_MODE = "rollback_future_save";
    public static final String OUTPUT_CHANGELOG_ONLY_SQL_MODE = "changelog_save";

    public static final String SHOULD_RUN_SYSTEM_PROPERTY = "database.migrator.should.run";

    public static final String DEFAULT_LOG_NAME = "database.migrator";

    private static boolean outputtedHeader = false;

    private XMLReader xmlReader;

    private String changeLogFile;
    private FileOpener fileOpener;
    public String mode;
    private Writer outputSQLWriter;
    private Date rollbackToDate;
    private String rollbackToTag;
    private Integer rollbackCount;

    private AbstractDatabase database;
    private Logger log;
    private Set<String> contexts = new HashSet<String>();

    private boolean hasChangeLogLock = false;
    private long changeLogLockWaitTime = 1000 * 60 * 5;  //default to 5 mins

    private List<RanChangeSet> ranChangeSetList;
    private String buildVersion;

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
            try {
                parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", new URI(getClass().getClassLoader().getResource("liquibase/dbchangelog-1.0.xsd").toExternalForm()).toString());
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
        setMode(EXECUTE_MODE);
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

    public Migrator(String changeLogFile, FileOpener fileOpener) {
        this(changeLogFile, fileOpener, false);
    }

    public void init(Connection connection) throws SQLException, MigrationFailedException {
        // Array Of all the implemented databases
        AbstractDatabase[] implementedDatabases = getImplementedDatabases();

        boolean foundImplementation = false;
        for (int i = 0; i < implementedDatabases.length; i++) {
            database = implementedDatabases[i];
            if (database.isCorrectDatabaseImplementation(connection)) {
                database.setConnection(connection);
                database.getConnection().setAutoCommit(false);
                foundImplementation = true;
                break;
            }
        }
        if (!foundImplementation) {
            throw new MigrationFailedException("Unknown database: " + connection.getMetaData().getDatabaseProductName());
        }
    }

    protected AbstractDatabase[] getImplementedDatabases() {
        return new AbstractDatabase[]{
                new OracleDatabase(),
                new PostgresDatabase(),
                new MSSQLDatabase(),
                new MySQLDatabase(),
        };
    }

    public String getBuildVersion() {
        return buildVersion;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }


    public Writer getOutputSQLWriter() {
        return outputSQLWriter;
    }

    public void setOutputSQLWriter(Writer outputSQLWriter) {
        this.outputSQLWriter = outputSQLWriter;
    }

    public Date getRollbackToDate() {
        return rollbackToDate;
    }

    public void setRollbackToDate(Date rollbackToDate) {
        this.rollbackToDate = rollbackToDate;
    }

    public String getRollbackToTag() {
        return rollbackToTag;
    }

    public void setRollbackToTag(String rollbackToTag) {
        this.rollbackToTag = rollbackToTag;
    }

    public Integer getRollbackCount() {
        return rollbackCount;
    }

    public void setRollbackCount(Integer rollbackCount) {
        this.rollbackCount = rollbackCount;
    }

    public FileOpener getFileOpener() {
        return fileOpener;
    }

    public List<RanChangeSet> getRanChangeSetList() throws SQLException {
        String databaseChangeLogTableName = getDatabase().getDatabaseChangeLogTableName();
        if (ranChangeSetList == null) {
            ranChangeSetList = new ArrayList<RanChangeSet>();
            if (getDatabase().doesChangeLogTableExist()) {
                log.info("Reading from " + databaseChangeLogTableName);
                String sql = "SELECT * FROM " + databaseChangeLogTableName + " ORDER BY dateExecuted asc";
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
    }

    public final void migrate() throws MigrationFailedException {
        try {
            if (!waitForLock()) {
                return;
            }

            Writer outputSQLWriter = getOutputSQLWriter();

            if (outputSQLWriter == null) {
                log.info("Reading changelog " + changeLogFile);
            } else {
                if (!outputtedHeader) {
                    outputSQLWriter.write("--------------------------------------------------------------------------------------" + StreamUtil.getLineSeparator());
                    if (mode.equals(OUTPUT_SQL_MODE)) {
                        outputSQLWriter.write("-- SQL to update database to newest version" + StreamUtil.getLineSeparator());
                    } else if (mode.equals(OUTPUT_CHANGELOG_ONLY_SQL_MODE)) {
                        outputSQLWriter.write("-- SQL to add all changesets to database history table" + StreamUtil.getLineSeparator());
                    } else if (mode.equals(OUTPUT_ROLLBACK_SQL_MODE)) {
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
                    } else if (mode.equals(OUTPUT_FUTURE_ROLLBACK_SQL_MODE)) {
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

            if (mode.equals(EXECUTE_MODE) || mode.equals(OUTPUT_SQL_MODE) || mode.equals(OUTPUT_CHANGELOG_ONLY_SQL_MODE))
            {
                runChangeLogs(new UpdateDatabaseChangeLogHandler(this, changeLogFile));
            } else if (mode.equals(EXECUTE_ROLLBACK_MODE) || mode.equals(OUTPUT_ROLLBACK_SQL_MODE)) {
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
            } else if (mode.equals(OUTPUT_FUTURE_ROLLBACK_SQL_MODE)) {
                RollbackFutureDatabaseChangeLogHandler rollbackHandler = new RollbackFutureDatabaseChangeLogHandler(this, changeLogFile);
                runChangeLogs(rollbackHandler);
                ChangeSet unrollbackableChangeSet = rollbackHandler.getUnRollBackableChangeSet();
                if (unrollbackableChangeSet == null) {
                    rollbackHandler.doRollback();
                } else {
                    throw new MigrationFailedException("Will not be able to rollback changes due to change set " + unrollbackableChangeSet);
                }
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

    private boolean waitForLock() throws SQLException, MigrationFailedException, IOException {
        if (!hasChangeLogLock) {
            checkDatabaseChangeLogTable();
        }

        boolean locked = false;
        long timeToGiveUp = new Date().getTime() + changeLogLockWaitTime;
        while (!locked && new Date().getTime() < timeToGiveUp) {
            locked = aquireLock();
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
            log.severe("Could not aquire change log lock.  Currently locked by " + lockedBy);
            return false;
        }

        return true;

    }

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

    protected boolean aquireLock() throws MigrationFailedException {
        if (hasChangeLogLock) {
            return true;
        }

        return getDatabase().aquireLock(this);
    }

    protected void releaseLock() throws MigrationFailedException {
        getDatabase().releaseLock();
    }

    /**
     * Releases whatever locks are on the database change log table
     *
     * @throws MigrationFailedException
     */
    public void forceReleaseLock() throws MigrationFailedException, SQLException, IOException {
        checkDatabaseChangeLogTable();

        getDatabase().releaseLock();
    }

    /**
     * 'Tags' the database for future rollback
     */
    public void tag(String tagString) throws MigrationFailedException {
        getDatabase().tag(tagString);
    }


    protected void checkDatabaseChangeLogTable() throws SQLException, IOException {
        getDatabase().checkDatabaseChangeLogTable(this);
        getDatabase().checkDatabaseChangeLogLockTable(this);
    }

    public void runChangeLogs(ContentHandler contentHandler) throws MigrationFailedException {
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

    public AbstractDatabase getDatabase() {
        return database;
    }


    public boolean isSaveToRunMigration() throws SQLException {
        if (OUTPUT_SQL_MODE.equals(getMode()) || OUTPUT_CHANGELOG_ONLY_SQL_MODE.equals(getMode())) {
            return true;
        }
        return getDatabase().getConnectionURL().indexOf("localhost") >= 0;
    }

    /**
     * Display change log lock information
     */
    public DatabaseChangeLogLock[] listLocks() throws MigrationFailedException, SQLException, IOException {
        checkDatabaseChangeLogTable();

        return getDatabase().listLocks();
    }

    public long getChangeLogLockWaitTime() {
        return changeLogLockWaitTime;
    }

    public void setChangeLogLockWaitTime(long changeLogLockWaitTime) {
        this.changeLogLockWaitTime = changeLogLockWaitTime;
    }

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

    public ChangeSet.RunStatus getRunStatus(ChangeSet changeSet) throws SQLException, DatabaseHistoryException {
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

                return ChangeSet.RunStatus.ALREADY_RAN;
            } else {
                if (foundRan.getMd5sum().equals(changeSet.getMd5sum())) {
                    return ChangeSet.RunStatus.ALREADY_RAN;
                } else {
                    if (changeSet.shouldRunOnChange()) {
                        return ChangeSet.RunStatus.RUN_AGAIN;
                    } else {
                        throw new DatabaseHistoryException("MD5 Check for " + changeSet.toString() + " failed");
                    }
                }
            }
        }
    }

    public boolean swingPromptForNonLocalDatabase() throws SQLException {
        return JOptionPane.showConfirmDialog(null, "You are running a database refactoring against a non-local database." + StreamUtil.getLineSeparator() +
                "Database URL is: " + this.getDatabase().getConnectionURL() + StreamUtil.getLineSeparator() +
                "Username is: " + this.getDatabase().getConnectionUsername() + StreamUtil.getLineSeparator() + StreamUtil.getLineSeparator() +
                "Area you sure you want to do this?",
                "Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION;
    }

}