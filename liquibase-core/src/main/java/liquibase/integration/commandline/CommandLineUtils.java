package liquibase.integration.commandline;

import liquibase.CatalogAndSchema;
import liquibase.command.CommandExecutionException;
import liquibase.command.DiffCommand;
import liquibase.command.DiffToChangeLogCommand;
import liquibase.command.GenerateChangeLogCommand;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.diff.DiffStatusListener;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.exception.*;
import liquibase.logging.LogFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.snapshot.InvalidExampleException;
import liquibase.util.StringUtils;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

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
                                                String propertyProviderClass,
                                                String liquibaseCatalogName,
                                                String liquibaseSchemaName) throws DatabaseException {
        try {
            liquibaseCatalogName = StringUtils.trimToNull(liquibaseCatalogName);
            liquibaseSchemaName = StringUtils.trimToNull(liquibaseSchemaName);
            defaultCatalogName = StringUtils.trimToNull(defaultCatalogName);
            defaultSchemaName = StringUtils.trimToNull(defaultSchemaName);

            Database database = DatabaseFactory.getInstance().openDatabase(url, username, password, driver, databaseClass, driverPropertiesFile, propertyProviderClass, new ClassLoaderResourceAccessor(classLoader));

            if (!database.supportsSchemas()) {
                if (defaultSchemaName != null && defaultCatalogName == null) {
                    defaultCatalogName = defaultSchemaName;
                }
                if (liquibaseSchemaName != null && liquibaseCatalogName == null) {
                    liquibaseCatalogName = liquibaseSchemaName;
                }
            }

            database.setDefaultCatalogName(defaultCatalogName);
            database.setDefaultSchemaName(defaultSchemaName);
            database.setOutputDefaultCatalog(outputDefaultCatalog);
            database.setOutputDefaultSchema(outputDefaultSchema);
            database.setLiquibaseCatalogName(liquibaseCatalogName);
            database.setLiquibaseSchemaName(liquibaseSchemaName);
            return database;
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public static void doDiff(Database referenceDatabase, Database targetDatabase, String snapshotTypes) throws LiquibaseException {
        doDiff(referenceDatabase, targetDatabase, snapshotTypes, null);
    }

    public static void doDiff(Database referenceDatabase, Database targetDatabase, String snapshotTypes, CompareControl.SchemaComparison[] schemaComparisons) throws LiquibaseException {
        DiffCommand diffCommand = new DiffCommand()
                .setReferenceDatabase(referenceDatabase)
                .setTargetDatabase(targetDatabase)
                .setCompareControl(new CompareControl(schemaComparisons, snapshotTypes))
                .setSnapshotTypes(snapshotTypes)
                .setOutputStream(System.out);

        System.out.println("");
        System.out.println("Diff Results:");
        try {
            diffCommand.execute();
        } catch (CommandExecutionException e) {
            throw new LiquibaseException(e);
        }
    }

    public static void doDiffToChangeLog(String changeLogFile,
                                         Database referenceDatabase,
                                         Database targetDatabase,
                                         DiffOutputControl diffOutputControl,
                                         String snapshotTypes)
            throws LiquibaseException, IOException, ParserConfigurationException {
        doDiffToChangeLog(changeLogFile, referenceDatabase, targetDatabase, diffOutputControl, snapshotTypes, null);
    }

        public static void doDiffToChangeLog(String changeLogFile,
                                         Database referenceDatabase,
                                         Database targetDatabase,
                                         DiffOutputControl diffOutputControl,
                                         String snapshotTypes,
                                         CompareControl.SchemaComparison[] schemaComparisons)
            throws LiquibaseException, IOException, ParserConfigurationException {

        DiffToChangeLogCommand command = new DiffToChangeLogCommand();
        command.setReferenceDatabase(referenceDatabase)
                .setTargetDatabase(targetDatabase)
                .setSnapshotTypes(snapshotTypes)
                .setCompareControl(new CompareControl(schemaComparisons, snapshotTypes))
                .setOutputStream(System.out);
        command.setChangeLogFile(changeLogFile)
                .setDiffOutputControl(diffOutputControl);

        try {
            command.execute();
        } catch (CommandExecutionException e) {
            throw new LiquibaseException(e);
        }

    }

    public static void doGenerateChangeLog(String changeLogFile, Database originalDatabase, String catalogName, String schemaName, String snapshotTypes, String author, String context, String dataDir, DiffOutputControl diffOutputControl) throws DatabaseException, IOException, ParserConfigurationException, InvalidExampleException, LiquibaseException {
        doGenerateChangeLog(changeLogFile, originalDatabase, new CatalogAndSchema[] {new CatalogAndSchema(catalogName, schemaName)}, snapshotTypes, author, context, dataDir, diffOutputControl);
    }

    public static void doGenerateChangeLog(String changeLogFile, Database originalDatabase, CatalogAndSchema[] schemas, String snapshotTypes, String author, String context, String dataDir, DiffOutputControl diffOutputControl) throws DatabaseException, IOException, ParserConfigurationException, InvalidExampleException, LiquibaseException {
        CompareControl.SchemaComparison[] comparisons = new CompareControl.SchemaComparison[schemas.length];
        int i=0;
        for (CatalogAndSchema schema : schemas) {
            comparisons[i++] = new CompareControl.SchemaComparison(schema, schema);
        }
        CompareControl compareControl = new CompareControl(comparisons, snapshotTypes);
        diffOutputControl.setDataDir(dataDir);

        GenerateChangeLogCommand command = new GenerateChangeLogCommand();

        command.setReferenceDatabase(originalDatabase)
                .setSnapshotTypes(snapshotTypes)
                .setOutputStream(System.out)
                .setCompareControl(compareControl);
        command.setChangeLogFile(changeLogFile)
                .setDiffOutputControl(diffOutputControl);
        command.setAuthor(author)
                .setContext(context);

        try {
            command.execute();
        } catch (CommandExecutionException e) {
            throw new LiquibaseException(e);
        }

    }

    private static class OutDiffStatusListener implements DiffStatusListener {

        @Override
        public void statusUpdate(String message) {
            LogFactory.getLogger().info(message);

        }

    }

}
