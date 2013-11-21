package liquibase.integration.commandline;

import liquibase.CatalogAndSchema;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.diff.DiffGeneratorFactory;
import liquibase.diff.DiffResult;
import liquibase.diff.DiffStatusListener;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.changelog.DiffToChangeLog;
import liquibase.diff.output.report.DiffToReport;
import liquibase.exception.*;
import liquibase.logging.LogFactory;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.InvalidExampleException;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
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
                                                String defaultCatalogName,
                                                String defaultSchemaName,
                                                boolean outputDefaultCatalog,
                                                boolean outputDefaultSchema,
                                                String databaseClass,
                                                String driverPropertiesFile,
                                                String liquibaseCatalogName,
                                                String liquibaseSchemaName) throws DatabaseException {
        driver = StringUtils.trimToNull(driver);
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
                    FileInputStream inputStream = new FileInputStream(propertiesFile);
                    try {
                        driverProperties.load(inputStream);
                    } finally {
                        inputStream.close();
                    }
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
            database.setDefaultCatalogName(StringUtils.trimToNull(defaultCatalogName));
            database.setDefaultSchemaName(StringUtils.trimToNull(defaultSchemaName));
            database.setOutputDefaultCatalog(outputDefaultCatalog);
            database.setOutputDefaultSchema(outputDefaultSchema);
            database.setLiquibaseCatalogName(StringUtils.trimToNull(liquibaseCatalogName));
            database.setLiquibaseSchemaName(StringUtils.trimToNull(liquibaseSchemaName));
            return database;
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public static void doDiff(Database referenceDatabase, Database targetDatabase, String snapshotTypes) throws LiquibaseException {
//        compareControl.addStatusListener(new OutDiffStatusListener());
        DatabaseSnapshot referenceSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(referenceDatabase.getDefaultSchema(), referenceDatabase, new SnapshotControl(referenceDatabase, snapshotTypes));
        DatabaseSnapshot targetSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(targetDatabase.getDefaultSchema(), targetDatabase, new SnapshotControl(targetDatabase));

        CompareControl compareControl = new CompareControl(referenceSnapshot.getSnapshotControl().getTypesToInclude());
        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(referenceSnapshot, targetSnapshot, compareControl);

        System.out.println("");
        System.out.println("Diff Results:");
        new DiffToReport(diffResult, System.out).print();
    }

    public static void doDiffToChangeLog(String changeLogFile,
                                         Database referenceDatabase,
                                         Database targetDatabase,
                                         DiffOutputControl diffOutputControl,
                                         String snapshotTypes)
            throws LiquibaseException, IOException, ParserConfigurationException {
        DatabaseSnapshot referenceSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(referenceDatabase.getDefaultSchema(), referenceDatabase, new SnapshotControl(referenceDatabase, snapshotTypes));
        DatabaseSnapshot targetSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(targetDatabase.getDefaultSchema(), targetDatabase, new SnapshotControl(targetDatabase));

        CompareControl compareControl = new CompareControl(referenceSnapshot.getSnapshotControl().getTypesToInclude());
//        compareControl.addStatusListener(new OutDiffStatusListener());

        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(referenceSnapshot, targetSnapshot, compareControl);

        if (changeLogFile == null) {
            new DiffToChangeLog(diffResult, diffOutputControl).print(System.out);
        } else {
            new DiffToChangeLog(diffResult, diffOutputControl).print(changeLogFile);
        }
    }

    public static void doGenerateChangeLog(String changeLogFile, Database originalDatabase, String catalogName, String schemaName, String snapshotTypes, String author, String context, String dataDir, DiffOutputControl diffOutputControl) throws DatabaseException, IOException, ParserConfigurationException, InvalidExampleException {
        SnapshotControl snapshotControl = new SnapshotControl(originalDatabase, snapshotTypes);
        CompareControl compareControl = new CompareControl(new CompareControl.SchemaComparison[] {new CompareControl.SchemaComparison(new CatalogAndSchema(catalogName, schemaName), new CatalogAndSchema(catalogName, schemaName))}, snapshotTypes);
//        compareControl.addStatusListener(new OutDiffStatusListener());

        diffOutputControl.setDataDir(dataDir);

        DatabaseSnapshot originalDatabaseSnapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(compareControl.getSchemas(CompareControl.DatabaseRole.REFERENCE), originalDatabase, snapshotControl);
        DiffResult diffResult = DiffGeneratorFactory.getInstance().compare(originalDatabaseSnapshot, SnapshotGeneratorFactory.getInstance().createSnapshot(compareControl.getSchemas(CompareControl.DatabaseRole.REFERENCE), null, snapshotControl), compareControl);

        DiffToChangeLog changeLogWriter = new DiffToChangeLog(diffResult, diffOutputControl);

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

        @Override
        public void statusUpdate(String message) {
            LogFactory.getLogger().info(message);

        }

    }

}
