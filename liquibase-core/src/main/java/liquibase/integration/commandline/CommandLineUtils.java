package liquibase.integration.commandline;

import liquibase.CatalogAndSchema;
import liquibase.command.CommandExecutionException;
import liquibase.command.CommandFactory;
import liquibase.command.core.DiffCommand;
import liquibase.command.core.DiffToChangeLogCommand;
import liquibase.command.core.GenerateChangeLogCommand;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.core.DatabaseUtils;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.diff.output.ObjectChangeFilter;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.logging.LogService;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.util.LiquibaseUtil;
import liquibase.util.StringUtils;

import javax.xml.parsers.ParserConfigurationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
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
            liquibaseCatalogName = StringUtils.trimToNull(liquibaseCatalogName);
            liquibaseSchemaName = StringUtils.trimToNull(liquibaseSchemaName);
            defaultCatalogName = StringUtils.trimToNull(defaultCatalogName);
            defaultSchemaName = StringUtils.trimToNull(defaultSchemaName);
            databaseChangeLogTableName = StringUtils.trimToNull(databaseChangeLogTableName);
            databaseChangeLogLockTableName = StringUtils.trimToNull(databaseChangeLogLockTableName);

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

            defaultCatalogName = StringUtils.trimToNull(defaultCatalogName);
            defaultSchemaName = StringUtils.trimToNull(defaultSchemaName);

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

    public static DiffCommand createDiffCommand(Database referenceDatabase, Database targetDatabase, String snapshotTypes,
                              CompareControl.SchemaComparison[] schemaComparisons, ObjectChangeFilter objectChangeFilter, PrintStream output) {
        DiffCommand diffCommand = (DiffCommand) CommandFactory.getInstance().getCommand("diff");

        diffCommand
                .setReferenceDatabase(referenceDatabase)
                .setTargetDatabase(targetDatabase)
                .setCompareControl(new CompareControl(schemaComparisons, snapshotTypes))
                .setObjectChangeFilter(objectChangeFilter)
                .setSnapshotTypes(snapshotTypes)
                .setOutputStream(output);
        return diffCommand;
    }

    public static void doDiff(Database referenceDatabase, Database targetDatabase, String snapshotTypes,
            CompareControl.SchemaComparison[] schemaComparisons, ObjectChangeFilter objectChangeFilter, PrintStream output) throws LiquibaseException {
        DiffCommand diffCommand = createDiffCommand(referenceDatabase, targetDatabase, snapshotTypes, schemaComparisons, objectChangeFilter, output);

        System.out.println("");
        System.out.println(coreBundle.getString("diff.results"));
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

        DiffToChangeLogCommand command = (DiffToChangeLogCommand) CommandFactory.getInstance().getCommand
                ("diffChangeLog");
        command.setReferenceDatabase(referenceDatabase)
                .setTargetDatabase(targetDatabase)
                .setSnapshotTypes(snapshotTypes)
                .setCompareControl(new CompareControl(schemaComparisons, snapshotTypes))
                .setObjectChangeFilter(objectChangeFilter)
                .setOutputStream(System.out);
        command.setChangeLogFile(changeLogFile)
                .setDiffOutputControl(diffOutputControl);

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

        GenerateChangeLogCommand command = (GenerateChangeLogCommand) CommandFactory.getInstance().getCommand
                ("generateChangeLog");

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

    public static String getBanner() {
        String myVersion = "";
        String buildTimeString = "";
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");

        myVersion = LiquibaseUtil.getBuildVersion();
        buildTimeString = LiquibaseUtil.getBuildTime();

        StringBuffer banner = new StringBuffer();
        
        // Banner is stored in banner.txt in resources.
	    Class commandLinUtilsClass = CommandLineUtils.class;
	    InputStream inputStream = commandLinUtilsClass.getResourceAsStream("/banner.txt");
	    try {
			banner.append(readFromInputStream(inputStream));
		} catch (IOException e) {
			LogService.getLog(commandLinUtilsClass).debug("Unable to locate banner file.");
		}
    
        banner.append(String.format(
            coreBundle.getString("starting.liquibase.at.timestamp"), dateFormat.format(calendar.getTime())
        ));

        if (StringUtils.isNotEmpty(myVersion) && StringUtils.isNotEmpty(buildTimeString)) {
            myVersion = myVersion + " #"+ LiquibaseUtil.getBuildNumber();
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
