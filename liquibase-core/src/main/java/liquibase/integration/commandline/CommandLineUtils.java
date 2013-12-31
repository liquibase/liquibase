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
import liquibase.resource.ClassLoaderResourceAccessor;
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
        try {
            Database database = DatabaseFactory.getInstance().openDatabase(url, username, password, driver, databaseClass, driverPropertiesFile, new ClassLoaderResourceAccessor(classLoader));
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
