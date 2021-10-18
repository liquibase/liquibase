package liquibase.integration.commandline;

import liquibase.CatalogAndSchema;
import liquibase.Scope;
import liquibase.command.CommandScope;
import liquibase.command.core.InternalDiffCommandStep;
import liquibase.command.core.InternalDiffChangelogCommandStep;
import liquibase.command.core.InternalGenerateChangelogCommandStep;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.DatabaseUtils;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.ObjectChangeFilter;
import liquibase.exception.CommandExecutionException;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StringUtil;

import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;

/**
 * Common Utility methods used in the CommandLine application and the Maven plugin.
 * These methods were originally moved from {@link Main} so they could be shared.
 *
 * @author Peter Murray
 */
public class CommandLineUtils {
    private static ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");

    /**
     * @deprecated Use ResourceAccessor version
     */
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
                                                String liquibaseSchemaName,
                                                String databaseChangeLogTableName,
                                                String databaseChangeLogLockTableName) throws DatabaseException {

        return createDatabaseObject(new ClassLoaderResourceAccessor(classLoader), url, username, password, driver,
                defaultCatalogName, defaultSchemaName, outputDefaultCatalog, outputDefaultSchema, databaseClass,
                driverPropertiesFile, propertyProviderClass, liquibaseCatalogName, liquibaseSchemaName,
                databaseChangeLogTableName, databaseChangeLogLockTableName);
    }

    public static Database createDatabaseObject(ResourceAccessor resourceAccessor,
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
                                                String liquibaseSchemaName,
                                                String databaseChangeLogTableName,
                                                String databaseChangeLogLockTableName) throws DatabaseException {
        try {
            liquibaseCatalogName = StringUtil.trimToNull(liquibaseCatalogName);
            liquibaseSchemaName = StringUtil.trimToNull(liquibaseSchemaName);
            defaultCatalogName = StringUtil.trimToNull(defaultCatalogName);
            defaultSchemaName = StringUtil.trimToNull(defaultSchemaName);
            databaseChangeLogTableName = StringUtil.trimToNull(databaseChangeLogTableName);
            databaseChangeLogLockTableName = StringUtil.trimToNull(databaseChangeLogLockTableName);

            Database database = DatabaseFactory.getInstance().openDatabase(url, username, password, driver,
                    databaseClass, driverPropertiesFile, propertyProviderClass, resourceAccessor);

            if (!database.supportsSchemas()) {
                if ((defaultSchemaName != null) && (defaultCatalogName == null)) {
                    defaultCatalogName = defaultSchemaName;
                }
                if ((liquibaseSchemaName != null) && (liquibaseCatalogName == null)) {
                    liquibaseCatalogName = liquibaseSchemaName;
                }
            }

            defaultCatalogName = StringUtil.trimToNull(defaultCatalogName);
            defaultSchemaName = StringUtil.trimToNull(defaultSchemaName);

            database.setDefaultCatalogName(defaultCatalogName);
            database.setDefaultSchemaName(defaultSchemaName);
            database.setOutputDefaultCatalog(outputDefaultCatalog);
            database.setOutputDefaultSchema(outputDefaultSchema);
            database.setLiquibaseCatalogName(liquibaseCatalogName);
            database.setLiquibaseSchemaName(liquibaseSchemaName);
            if (databaseChangeLogTableName != null) {
                database.setDatabaseChangeLogTableName(databaseChangeLogTableName);
                if (databaseChangeLogLockTableName != null) {
                    database.setDatabaseChangeLogLockTableName(databaseChangeLogLockTableName);
                } else {
                    database.setDatabaseChangeLogLockTableName(databaseChangeLogTableName + "LOCK");
                }
            }

            //Todo: move to database object methods in 4.0
            DatabaseUtils.initializeDatabase(defaultCatalogName, defaultSchemaName, database);

            return database;
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    public static void doDiff(Database referenceDatabase, Database targetDatabase, String snapshotTypes) throws
            LiquibaseException {
        doDiff(referenceDatabase, targetDatabase, snapshotTypes, null);
    }

    public static void doDiff(Database referenceDatabase, Database targetDatabase, String snapshotTypes,
                              CompareControl.SchemaComparison[] schemaComparisons) throws LiquibaseException {
        doDiff(referenceDatabase, targetDatabase, snapshotTypes, schemaComparisons, System.out);
    }

    public static void doDiff(Database referenceDatabase, Database targetDatabase, String snapshotTypes,
                              CompareControl.SchemaComparison[] schemaComparisons, PrintStream output) throws LiquibaseException {
        doDiff(referenceDatabase, targetDatabase, snapshotTypes, schemaComparisons, null, output);
    }

    public static CommandScope createDiffCommand(Database referenceDatabase, Database targetDatabase, String snapshotTypes,
                                                CompareControl.SchemaComparison[] schemaComparisons, ObjectChangeFilter objectChangeFilter, PrintStream output) throws CommandExecutionException {
        CommandScope diffCommand = new CommandScope("internalDiff");

        diffCommand
                .addArgumentValue(InternalDiffCommandStep.REFERENCE_DATABASE_ARG, referenceDatabase)
                .addArgumentValue(InternalDiffCommandStep.TARGET_DATABASE_ARG, targetDatabase)
                .addArgumentValue(InternalDiffCommandStep.COMPARE_CONTROL_ARG, new CompareControl(schemaComparisons, snapshotTypes))
                .addArgumentValue(InternalDiffCommandStep.OBJECT_CHANGE_FILTER_ARG, objectChangeFilter)
                .addArgumentValue(InternalDiffCommandStep.SNAPSHOT_TYPES_ARG, InternalDiffCommandStep.parseSnapshotTypes(snapshotTypes))
        ;

        diffCommand.setOutput(output);

        return diffCommand;
    }

    public static void doDiff(Database referenceDatabase, Database targetDatabase, String snapshotTypes,
                              CompareControl.SchemaComparison[] schemaComparisons, ObjectChangeFilter objectChangeFilter, PrintStream output) throws LiquibaseException {
        CommandScope diffCommand = createDiffCommand(referenceDatabase, targetDatabase, snapshotTypes, schemaComparisons, objectChangeFilter, output);

        Scope.getCurrentScope().getUI().sendMessage("");
        Scope.getCurrentScope().getUI().sendMessage(coreBundle.getString("diff.results"));
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
                                         ObjectChangeFilter objectChangeFilter,
                                         String snapshotTypes)
            throws LiquibaseException, IOException, ParserConfigurationException {
        doDiffToChangeLog(changeLogFile, referenceDatabase, targetDatabase, diffOutputControl, objectChangeFilter,
                snapshotTypes, null);
    }

    public static void doDiffToChangeLog(String changeLogFile,
                                         Database referenceDatabase,
                                         Database targetDatabase,
                                         DiffOutputControl diffOutputControl,
                                         ObjectChangeFilter objectChangeFilter,
                                         String snapshotTypes,
                                         CompareControl.SchemaComparison[] schemaComparisons)
            throws LiquibaseException, IOException, ParserConfigurationException {

        CommandScope command = new CommandScope("internalDiffChangeLog");
        command
                .addArgumentValue(InternalDiffChangelogCommandStep.REFERENCE_DATABASE_ARG, referenceDatabase)
                .addArgumentValue(InternalDiffChangelogCommandStep.TARGET_DATABASE_ARG, targetDatabase)
                .addArgumentValue(InternalDiffChangelogCommandStep.SNAPSHOT_TYPES_ARG, InternalDiffChangelogCommandStep.parseSnapshotTypes(snapshotTypes))
                .addArgumentValue(InternalDiffChangelogCommandStep.COMPARE_CONTROL_ARG, new CompareControl(schemaComparisons, snapshotTypes))
                .addArgumentValue(InternalDiffChangelogCommandStep.OBJECT_CHANGE_FILTER_ARG, objectChangeFilter)
                .addArgumentValue(InternalDiffChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(InternalDiffChangelogCommandStep.DIFF_OUTPUT_CONTROL_ARG, diffOutputControl);
        command.setOutput(System.out);
        try {
            command.execute();
        } catch (CommandExecutionException e) {
            throw new LiquibaseException(e);
        }

    }

    public static void doGenerateChangeLog(String changeLogFile, Database originalDatabase, String catalogName,
                                           String schemaName, String snapshotTypes, String author, String context,
                                           String dataDir, DiffOutputControl diffOutputControl) throws
            IOException, ParserConfigurationException, LiquibaseException {
        doGenerateChangeLog(changeLogFile, originalDatabase, new CatalogAndSchema[]{new CatalogAndSchema(catalogName,
                schemaName)}, snapshotTypes, author, context, dataDir, diffOutputControl);
    }

    public static void doGenerateChangeLog(String changeLogFile, Database originalDatabase, CatalogAndSchema[]
            schemas, String snapshotTypes, String author, String context, String dataDir, DiffOutputControl
                                                   diffOutputControl) throws IOException, ParserConfigurationException,
            LiquibaseException {
        CompareControl.SchemaComparison[] comparisons = new CompareControl.SchemaComparison[schemas.length];
        int i = 0;
        for (CatalogAndSchema schema : schemas) {
            comparisons[i++] = new CompareControl.SchemaComparison(schema, schema);
        }
        CompareControl compareControl = new CompareControl(comparisons, snapshotTypes);
        diffOutputControl.setDataDir(dataDir);

        CommandScope command = new CommandScope("internalGenerateChangeLog");
        command
                .addArgumentValue(InternalGenerateChangelogCommandStep.REFERENCE_DATABASE_ARG, originalDatabase)
                .addArgumentValue(InternalGenerateChangelogCommandStep.SNAPSHOT_TYPES_ARG, InternalGenerateChangelogCommandStep.parseSnapshotTypes(snapshotTypes))
                .addArgumentValue(InternalGenerateChangelogCommandStep.COMPARE_CONTROL_ARG, compareControl)
                .addArgumentValue(InternalGenerateChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(InternalGenerateChangelogCommandStep.DIFF_OUTPUT_CONTROL_ARG, diffOutputControl)
                .addArgumentValue(InternalGenerateChangelogCommandStep.AUTHOR_ARG, author)
                .addArgumentValue(InternalGenerateChangelogCommandStep.CONTEXT_ARG, context);

        command.setOutput(System.out);
        try {
            command.execute();
        } catch (CommandExecutionException e) {
            throw new LiquibaseException(e);
        }

    }

    public static String getBanner() {
        String myVersion = "";
        String buildTimeString = "";
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

        myVersion = LiquibaseUtil.getBuildVersionInfo();
        buildTimeString = LiquibaseUtil.getBuildTime();

        StringBuilder banner = new StringBuilder();

        // Banner is stored in liquibase/banner.txt in resources.
        Class commandLinUtilsClass = CommandLineUtils.class;
        InputStream inputStream = commandLinUtilsClass.getResourceAsStream("/liquibase/banner.txt");
        try {
            banner.append(readFromInputStream(inputStream));
        } catch (IOException e) {
            Scope.getCurrentScope().getLog(commandLinUtilsClass).fine("Unable to locate banner file.");
        }

        banner.append(String.format(
                coreBundle.getString("starting.liquibase.at.timestamp"), dateFormat.format(calendar.getTime())
        ));

        if (StringUtil.isNotEmpty(myVersion) && StringUtil.isNotEmpty(buildTimeString)) {
            myVersion = myVersion + " #" + LiquibaseUtil.getBuildNumber();
            banner.append(String.format(coreBundle.getString("liquibase.version.builddate"), myVersion, buildTimeString));
        }

        return banner.toString();
    }

    private static String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                resultStringBuilder.append(line + "\n");

            }
        }
        return resultStringBuilder.toString();
    }

}
