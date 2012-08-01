package liquibase.integration.commandline;

import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.database.structure.Schema;
import liquibase.diff.DiffControl;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.DiffStatusListener;
import liquibase.diff.output.DiffToChangeLog;
import liquibase.diff.output.DiffToPrintStream;
import liquibase.exception.*;
import liquibase.logging.LogFactory;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.DatabaseSnapshotGeneratorFactory;
import liquibase.util.StringUtils;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.*;
import java.util.*;

/**
 * Common Utilitiy methods used in the CommandLine application and the Maven plugin.
 * These methods were orignally moved from {@link Main} so they could be shared.
 *
 * @author Peter Murray
 */
public class CommandLineUtils {

    public static Database createDatabaseObject(ClassLoader classLoader,
                                                String url,
                                                String username,
                                                String password,
                                                String driver,
                                                String defaultSchemaName,
                                                String databaseClass,
                                                String driverPropertiesFile) throws DatabaseException {
        if (driver == null) {
            driver = DatabaseFactory.getInstance().findDefaultDriver(url);
        }

        try {
            Driver driverObject;
            DatabaseFactory databaseFactory = DatabaseFactory.getInstance();
            if (databaseClass != null) {
                databaseFactory.clearRegistry();
                databaseFactory.register((Database) Class.forName(databaseClass, true, classLoader).newInstance());
            }

            try {
                if (driver == null) {
                    driver = databaseFactory.findDefaultDriver(url);
                }

                if (driver == null) {
                    throw new RuntimeException("Driver class was not specified and could not be determined from the url (" + url + ")");
                }

                driverObject = (Driver) Class.forName(driver, true, classLoader).newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Cannot find database driver: " + e.getMessage());
            }


            Properties driverProperties = new Properties();

            if (username != null) {
                driverProperties.put("user", username);
            }
            if (password != null) {
                driverProperties.put("password", password);
            }
            if (null != driverPropertiesFile) {
                File propertiesFile = new File(driverPropertiesFile);
                if (propertiesFile.exists()) {
//                    System.out.println("Loading properties from the file:'" + driverPropertiesFile + "'");
                    driverProperties.load(new FileInputStream(propertiesFile));
                } else {
                  throw new RuntimeException("Can't open JDBC Driver specific properties from the file: '"
                      + driverPropertiesFile + "'");
                }
            }


//            System.out.println("Properties:");
//            for (Map.Entry entry : driverProperties.entrySet()) {
//                System.out.println("Key:'"+entry.getKey().toString()+"' Value:'"+entry.getValue().toString()+"'");
//            }
            

//            System.out.println("Connecting to the URL:'"+url+"' using driver:'"+driverObject.getClass().getName()+"'");
            Connection connection = driverObject.connect(url, driverProperties);
//            System.out.println("Connection has been created");
            if (connection == null) {
                throw new DatabaseException("Connection could not be created to " + url + " with driver " + driverObject.getClass().getName() + ".  Possibly the wrong driver for the given database URL");
            }

            Database database = databaseFactory.findCorrectDatabaseImplementation(new JdbcConnection(connection));
            database.setDefaultSchemaName(StringUtils.trimToNull(defaultSchemaName));
            return database;
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public static void doDiff(Database referenceDatabase, Database targetDatabase) throws DatabaseException {
        DiffControl diffControl = new DiffControl();
        diffControl.addStatusListener(new OutDiffStatusListener());
        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(referenceDatabase, targetDatabase, diffControl);

        System.out.println("");
        System.out.println("Diff Results:");
        new DiffToPrintStream(diffResult, System.out).print();
    }

    public static void doDiffToChangeLog(String changeLogFile,
                                         Database referenceDatabase,
                                         Database targetDatabase)
            throws DatabaseException, IOException, ParserConfigurationException {
        DiffControl diffControl = new DiffControl();
        diffControl.addStatusListener(new OutDiffStatusListener());

        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(referenceDatabase, targetDatabase, diffControl);

        if (changeLogFile == null) {
            new DiffToChangeLog(diffResult).print(System.out);
        } else {
            new DiffToChangeLog(diffResult).print(changeLogFile);
        }
    }

    public static void doGenerateChangeLog(String changeLogFile, Database originalDatabase, String catalogName, String schemaName, String diffTypes, String author, String context, String dataDir) throws DatabaseException, IOException, ParserConfigurationException {
        DiffControl diffControl = new DiffControl(new Schema(catalogName, schemaName), diffTypes);
        diffControl.setDataDir(dataDir);
        diffControl.addStatusListener(new OutDiffStatusListener());

        DatabaseSnapshot originalDatabaseSnapshot = DatabaseSnapshotGeneratorFactory.getInstance().createSnapshot(originalDatabase, diffControl, DiffControl.DatabaseRole.REFERENCE);
        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(originalDatabaseSnapshot, new DatabaseSnapshot(null, diffControl.getSchemas(DiffControl.DatabaseRole.REFERENCE)), diffControl);

        DiffToChangeLog changeLogWriter = new DiffToChangeLog(diffResult);

        changeLogWriter.setChangeSetAuthor(author);
        changeLogWriter.setChangeSetContext(context);

        if (StringUtils.trimToNull(changeLogFile) != null) {
            changeLogWriter.print(changeLogFile);
        } else {
            PrintStream outputStream = System.out;
            changeLogWriter.print(outputStream);
        }
    }

    private static class OutDiffStatusListener implements DiffStatusListener {

        public void statusUpdate(String message) {
            LogFactory.getLogger().info(message);

        }

    }

}
