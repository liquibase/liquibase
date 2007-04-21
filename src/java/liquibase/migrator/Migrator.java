package liquibase.migrator;

import liquibase.database.*;
import liquibase.migrator.commandline.cli.CommandLine;

import org.xml.sax.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Logger;
import java.net.URI;

public class Migrator {
    // These modes tell the program whether to execute the statements against the database
    // Or to output them in some file to be ran later manually
    public static final String EXECUTE_MODE = "execute";
    public static final String OUTPUT_SQL_MODE = "save";
    public static final String OUTPUT_CHANGELOG_ONLY_SQL_MODE = "changelog_save";

    public static final String SHOULD_RUN_SYSTEM_PROPERTY = "database.migrator.should.run";

    public static final String DEFAULT_LOG_NAME = "database.migrator";

    private static boolean outputtedHeader = false;

    private XMLReader xmlReader;

    private String migrationFile;
    private FileOpener fileOpener;
    public String mode;
    private Writer outputSQLWriter;

    private boolean shouldDropDatabaseObjectsFirst;
    private AbstractDatabase database;
    private Logger log;
    private Set<String> contexts;

    private boolean hasChangeLogLock = false;
    private long changeLogLockWaitTime = 1000 * 60 * 5;  //default to 5 mins

    protected Migrator(String migrationFile, FileOpener fileOpener, boolean alreadyHasChangeLogLock) throws SQLException, MigrationFailedException {
        log = Logger.getLogger(Migrator.DEFAULT_LOG_NAME);

        this.migrationFile = migrationFile;
        this.fileOpener = fileOpener;
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        saxParserFactory.setValidating(true);
        saxParserFactory.setNamespaceAware(true);
        try {
            SAXParser parser = saxParserFactory.newSAXParser();
            parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", "http://www.w3.org/2001/XMLSchema");
            parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource", getClass().getClassLoader().getResource("liquibase/dbchangelog-1.0.xsd").toURI().toString());
            this.xmlReader = parser.getXMLReader();
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
            xmlReader.setContentHandler(new ChangeLogHandler(this));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        setMode(EXECUTE_MODE);
        this.hasChangeLogLock = alreadyHasChangeLogLock;
    }

    public Migrator(String migrationFile, FileOpener fileOpener) throws SQLException, MigrationFailedException {
        this(migrationFile, fileOpener, false);
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

    public boolean shouldDropDatabaseObjectsFirst() {
        return shouldDropDatabaseObjectsFirst;
    }

    public void setShouldDropDatabaseObjectsFirst(boolean shouldDropDatabaseObjectsFirst) {
        this.shouldDropDatabaseObjectsFirst = shouldDropDatabaseObjectsFirst;
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

    public String getMigrationFile() {
        return migrationFile;
    }

    public FileOpener getFileOpener() {
        return fileOpener;
    }

    public void migrate() throws MigrationFailedException {
        try {
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
                return;
            }
            if (shouldDropDatabaseObjectsFirst()) {
                log.info("Dropping Database Objects in " + getDatabase().getSchemaName());
                getDatabase().dropDatabaseObjects();
                log.finest("Objects dropped successfully");
            }

            Writer outputSQLWriter = getOutputSQLWriter();

            if (outputSQLWriter == null) {
                log.info("Reading changelog " + getMigrationFile());
            } else {
                if (!outputtedHeader) {
                    outputSQLWriter.write("--------------------------------------------------------------------------------------\n");
                    outputSQLWriter.write("-- Migration file: " + getMigrationFile() + "\n");
                    outputSQLWriter.write("-- Run at: " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date()) + "\n");
                    outputSQLWriter.write("-- Against: " + getDatabase().getConnectionUsername() + "@" + getDatabase().getConnectionURL() + "\n");
                    outputSQLWriter.write("--------------------------------------------------------------------------------------\n\n\n");
                    outputtedHeader = true;
                }
            }

//            digester.push(this);
//            digester.push(getMigrationFile());
//            digester.push(outputSQLWriter);
//            digester.push(getMode());
//            digester.push(getDatabase());
            processMigration();
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
        getDatabase().releaseLock(this);
    }

    /**
     * Releases whatever locks are on the database change log table
     *
     * @throws MigrationFailedException
     */
    public void forceReleaseLock() throws MigrationFailedException, SQLException, IOException {
        checkDatabaseChangeLogTable();

        getDatabase().releaseLock(this);
    }


    protected void checkDatabaseChangeLogTable() throws SQLException, IOException {
        getDatabase().checkDatabaseChangeLogTable(this);
        getDatabase().checkDatabaseChangeLogLockTable(this);
    }

    public void processMigration() throws MigrationFailedException {
//        setUpMigrationRules();
        try {
            InputStream inputStream = getFileOpener().getResourceAsStream(getMigrationFile());
            if (inputStream == null) {
                throw new MigrationFailedException(getMigrationFile() + " does not exist");
            }
//            StringBuffer inputBuffer = new StringBuffer();
//            String line;
//            while ((line = reader.readLine()) != null) {
//                inputBuffer.append(line).append("\n");
//            }
//            String input = inputBuffer.toString().replaceAll("\r\n", "\n").replaceAll("\r", "\n");

            xmlReader.parse(new InputSource(inputStream));
            inputStream.close();
        } catch (IOException e) {
            throw new MigrationFailedException("Error Reading Migration File: " + e.getMessage(), e);
        } catch (SAXException e) {
            Throwable parentCause = e.getException();
            while (parentCause != null) {
                if (parentCause instanceof MigrationFailedException) {
                    throw ((MigrationFailedException) parentCause);
                }
                parentCause = parentCause.getCause();
            }
            throw new MigrationFailedException("Invalid Migration File: " + e.getMessage(), e);
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

        return getDatabase().listLocks(this);
    }

    public long getChangeLogLockWaitTime() {
        return changeLogLockWaitTime;
    }

    public void setChangeLogLockWaitTime(long changeLogLockWaitTime) {
        this.changeLogLockWaitTime = changeLogLockWaitTime;
    }

    public void setContexts(String contexts) {
        this.contexts = new HashSet<String>();
        String[] strings = contexts.split(",");
        for (String string : strings) {
            this.contexts.add(string.trim().toLowerCase());
        }
    }
    public Set<String> getContexts() {
        return contexts;
    }
}