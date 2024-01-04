package liquibase.integration.commandline;

import liquibase.CatalogAndSchema;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.command.CommandScope;
import liquibase.command.core.DiffChangelogCommandStep;
import liquibase.command.core.DiffCommandStep;
import liquibase.command.core.GenerateChangelogCommandStep;
import liquibase.command.core.helpers.*;
import liquibase.configuration.ConfiguredValue;
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
import liquibase.util.StringUtil;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ResourceBundle;

import static java.util.ResourceBundle.getBundle;

/**
 * Common Utility methods used in the CommandLine application and the Maven plugin.
 * These methods were originally moved from {@link Main} so they could be shared.
 *
 * @author Peter Murray
 */
public class CommandLineUtils {
    private static final ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");

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

    @SuppressWarnings("java:S2095")
    @Deprecated
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
                //
                // Get values from the configuration object if they aren't already set
                //
                if (liquibaseCatalogName == null) {
                    ConfiguredValue<String> configuredValue  = GlobalConfiguration.LIQUIBASE_CATALOG_NAME.getCurrentConfiguredValue();
                    liquibaseCatalogName = configuredValue.getValue();
                }
                if (liquibaseSchemaName == null) {
                    ConfiguredValue<String> configuredValue  = GlobalConfiguration.LIQUIBASE_SCHEMA_NAME.getCurrentConfiguredValue();
                    liquibaseSchemaName = configuredValue.getValue();
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
                .addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DATABASE_ARG, referenceDatabase)
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, targetDatabase)
                .addArgumentValue(PreCompareCommandStep.COMPARE_CONTROL_ARG, new CompareControl(schemaComparisons, snapshotTypes))
                .addArgumentValue(PreCompareCommandStep.OBJECT_CHANGE_FILTER_ARG, objectChangeFilter)
                .addArgumentValue(PreCompareCommandStep.SNAPSHOT_TYPES_ARG, DiffCommandStep.parseSnapshotTypes(snapshotTypes))
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

    /**
     * @deprecated Use version with  String runOnChangeTypes, String replaceIfExistsTypes instead - {@link #doDiffToChangeLog(String, Database, Database, String, DiffOutputControl, ObjectChangeFilter, String, String, String)}
     */
    @Deprecated
    public static void doDiffToChangeLog(String changeLogFile,
                                         Database referenceDatabase,
                                         Database targetDatabase,
                                         String author,
                                         DiffOutputControl diffOutputControl,
                                         ObjectChangeFilter objectChangeFilter,
                                         String snapshotTypes)
            throws LiquibaseException, IOException, ParserConfigurationException {
        doDiffToChangeLog(changeLogFile, referenceDatabase, targetDatabase, author, diffOutputControl, objectChangeFilter,
                snapshotTypes, AbstractChangelogCommandStep.RUN_ON_CHANGE_TYPES_ARG.getDefaultValue(), AbstractChangelogCommandStep.REPLACE_IF_EXISTS_TYPES_ARG.getDefaultValue());
    }

    public static void doDiffToChangeLog(String changeLogFile,
                                         Database referenceDatabase,
                                         Database targetDatabase,
                                         String author,
                                         DiffOutputControl diffOutputControl,
                                         ObjectChangeFilter objectChangeFilter,
                                         String snapshotTypes,
                                         String runOnChangeTypes,
                                         String replaceIfExistsTypes)
            throws LiquibaseException, IOException, ParserConfigurationException {
        doDiffToChangeLog(changeLogFile, referenceDatabase, targetDatabase, author, diffOutputControl, objectChangeFilter,
                snapshotTypes, null, runOnChangeTypes, replaceIfExistsTypes);
    }

    /**
     * @deprecated Use version with  String runOnChangeTypes, String replaceIfExistsTypes instead - {@link #doDiffToChangeLog(String, Database, Database, String, DiffOutputControl, ObjectChangeFilter, String, CompareControl.SchemaComparison[], String, String)}
     */
    @Deprecated
    public static void doDiffToChangeLog(String changeLogFile,
                                         Database referenceDatabase,
                                         Database targetDatabase,
                                         String author,
                                         DiffOutputControl diffOutputControl,
                                         ObjectChangeFilter objectChangeFilter,
                                         String snapshotTypes,
                                         CompareControl.SchemaComparison[] schemaComparisons)
            throws LiquibaseException, IOException, ParserConfigurationException {
        doDiffToChangeLog(changeLogFile, referenceDatabase, targetDatabase, author, diffOutputControl, objectChangeFilter,
                snapshotTypes, schemaComparisons, AbstractChangelogCommandStep.RUN_ON_CHANGE_TYPES_ARG.getDefaultValue(), AbstractChangelogCommandStep.REPLACE_IF_EXISTS_TYPES_ARG.getDefaultValue());
    }

    public static void doDiffToChangeLog(String changeLogFile,
                                         Database referenceDatabase,
                                         Database targetDatabase,
                                         String author,
                                         DiffOutputControl diffOutputControl,
                                         ObjectChangeFilter objectChangeFilter,
                                         String snapshotTypes,
                                         CompareControl.SchemaComparison[] schemaComparisons,
                                         String runOnChangeTypes,
                                         String replaceIfExistsTypes)
            throws LiquibaseException, IOException, ParserConfigurationException {


        CommandScope command = new CommandScope("diffChangeLog");
        command
                .addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DATABASE_ARG, referenceDatabase)
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, targetDatabase)
                .addArgumentValue(PreCompareCommandStep.SNAPSHOT_TYPES_ARG, DiffCommandStep.parseSnapshotTypes(snapshotTypes))
                .addArgumentValue(PreCompareCommandStep.COMPARE_CONTROL_ARG, new CompareControl(schemaComparisons, snapshotTypes))
                .addArgumentValue(PreCompareCommandStep.OBJECT_CHANGE_FILTER_ARG, objectChangeFilter)
                .addArgumentValue(DiffChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DiffOutputControlCommandStep.INCLUDE_CATALOG_ARG, diffOutputControl.getIncludeCatalog())
                .addArgumentValue(DiffOutputControlCommandStep.INCLUDE_SCHEMA_ARG, diffOutputControl.getIncludeSchema())
                .addArgumentValue(DiffOutputControlCommandStep.INCLUDE_TABLESPACE_ARG, diffOutputControl.getIncludeTablespace())
                .addArgumentValue(DiffChangelogCommandStep.AUTHOR_ARG, author)
                .addArgumentValue(DiffChangelogCommandStep.RUN_ON_CHANGE_TYPES_ARG, runOnChangeTypes)
                .addArgumentValue(DiffChangelogCommandStep.REPLACE_IF_EXISTS_TYPES_ARG, replaceIfExistsTypes);
        if(diffOutputControl.isReplaceIfExistsSet()) {
            command.addArgumentValue(GenerateChangelogCommandStep.USE_OR_REPLACE_OPTION, true);
        }
        command.setOutput(System.out);
        try {
            command.execute();
        } catch (CommandExecutionException e) {
            throw new LiquibaseException(e);
        }
    }

    /**
     * @deprecated Use version with  String runOnChangeTypes, String replaceIfExistsTypes instead - {@link #doGenerateChangeLog(String, Database, String, String, String, String, String, String, DiffOutputControl, String, String)}
     */
    @Deprecated
    public static void doGenerateChangeLog(String changeLogFile, Database originalDatabase, String catalogName,
                                           String schemaName, String snapshotTypes, String author, String context,
                                           String dataDir, DiffOutputControl diffOutputControl) throws
            IOException, ParserConfigurationException, LiquibaseException {
        doGenerateChangeLog(changeLogFile, originalDatabase, new CatalogAndSchema[]{new CatalogAndSchema(catalogName,
                schemaName)}, snapshotTypes, author, context, dataDir, diffOutputControl,
                AbstractChangelogCommandStep.RUN_ON_CHANGE_TYPES_ARG.getDefaultValue(), AbstractChangelogCommandStep.REPLACE_IF_EXISTS_TYPES_ARG.getDefaultValue());
    }

    public static void doGenerateChangeLog(String changeLogFile, Database originalDatabase, String catalogName,
                                           String schemaName, String snapshotTypes, String author, String context,
                                           String dataDir, DiffOutputControl diffOutputControl, String runOnChangeTypes, String replaceIfExistsTypes) throws
            IOException, ParserConfigurationException, LiquibaseException {
        doGenerateChangeLog(changeLogFile, originalDatabase, new CatalogAndSchema[]{new CatalogAndSchema(catalogName,
                schemaName)}, snapshotTypes, author, context, dataDir, diffOutputControl, runOnChangeTypes, replaceIfExistsTypes);
    }

    /**
     * @deprecated Use version with  String runOnChangeTypes, String replaceIfExistsTypes instead - {@link #doGenerateChangeLog(String, Database, String, String, String, String, String, String, DiffOutputControl, String, String)}
     */
    @Deprecated
    public static void doGenerateChangeLog(String changeLogFile, Database originalDatabase, CatalogAndSchema[]
            schemas, String snapshotTypes, String author, String context, String dataDir, DiffOutputControl diffOutputControl)
            throws IOException, ParserConfigurationException, LiquibaseException {
        doGenerateChangeLog(changeLogFile, originalDatabase, schemas, snapshotTypes, author, context, dataDir, diffOutputControl,
                AbstractChangelogCommandStep.RUN_ON_CHANGE_TYPES_ARG.getDefaultValue(), AbstractChangelogCommandStep.REPLACE_IF_EXISTS_TYPES_ARG.getDefaultValue());
    }


    public static void doGenerateChangeLog(String changeLogFile, Database originalDatabase, CatalogAndSchema[]
            schemas, String snapshotTypes, String author, String context, String dataDir, DiffOutputControl
                                                   diffOutputControl, String runOnChangeTypes, String replaceIfExistsTypes) throws IOException, ParserConfigurationException,
            LiquibaseException {
        doGenerateChangeLog(changeLogFile, originalDatabase, schemas, snapshotTypes, author, context, dataDir, diffOutputControl, false, runOnChangeTypes, replaceIfExistsTypes);
    }

    /**
     * @deprecated Use version with  String runOnChangeTypes, String replaceIfExistsTypes instead - {@link #doGenerateChangeLog(String, Database, CatalogAndSchema[], String, String, String, String, DiffOutputControl, String, String)}
     */
    @Deprecated

    public static void doGenerateChangeLog(String changeLogFile, Database originalDatabase, CatalogAndSchema[]
            schemas, String snapshotTypes, String author, String context, String dataDir, DiffOutputControl
                                                   diffOutputControl, boolean overwriteOutputFile) throws IOException, ParserConfigurationException, LiquibaseException {
        doGenerateChangeLog(changeLogFile, originalDatabase, schemas, snapshotTypes, author, context, dataDir, diffOutputControl, overwriteOutputFile,
                AbstractChangelogCommandStep.RUN_ON_CHANGE_TYPES_ARG.getDefaultValue(), AbstractChangelogCommandStep.REPLACE_IF_EXISTS_TYPES_ARG.getDefaultValue());
    }

    public static void doGenerateChangeLog(String changeLogFile, Database originalDatabase, CatalogAndSchema[]
            schemas, String snapshotTypes, String author, String context, String dataDir, DiffOutputControl
            diffOutputControl, boolean overwriteOutputFile, String runOnChangeTypes, String replaceIfExistsTypes) throws IOException, ParserConfigurationException,
            LiquibaseException {
        CompareControl.SchemaComparison[] comparisons = new CompareControl.SchemaComparison[schemas.length];
        int i = 0;
        for (CatalogAndSchema schema : schemas) {
            comparisons[i++] = new CompareControl.SchemaComparison(schema, schema);
        }
        CompareControl compareControl = new CompareControl(comparisons, snapshotTypes);
        diffOutputControl.setDataDir(dataDir);

        CommandScope command = new CommandScope("generateChangeLog");
        command
                .addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DATABASE_ARG, originalDatabase)
                .addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, originalDatabase)
                .addArgumentValue(PreCompareCommandStep.SNAPSHOT_TYPES_ARG, DiffCommandStep.parseSnapshotTypes(snapshotTypes))
                .addArgumentValue(PreCompareCommandStep.COMPARE_CONTROL_ARG, compareControl)
                .addArgumentValue(DiffChangelogCommandStep.CHANGELOG_FILE_ARG, changeLogFile)
                .addArgumentValue(DiffOutputControlCommandStep.INCLUDE_CATALOG_ARG, diffOutputControl.getIncludeCatalog())
                .addArgumentValue(DiffOutputControlCommandStep.INCLUDE_SCHEMA_ARG, diffOutputControl.getIncludeSchema())
                .addArgumentValue(DiffOutputControlCommandStep.INCLUDE_TABLESPACE_ARG, diffOutputControl.getIncludeTablespace())
                .addArgumentValue(GenerateChangelogCommandStep.AUTHOR_ARG, author)
                .addArgumentValue(GenerateChangelogCommandStep.CONTEXT_ARG, context)
                .addArgumentValue(GenerateChangelogCommandStep.OVERWRITE_OUTPUT_FILE_ARG, overwriteOutputFile)
                .addArgumentValue(GenerateChangelogCommandStep.RUN_ON_CHANGE_TYPES_ARG, runOnChangeTypes)
                .addArgumentValue(GenerateChangelogCommandStep.REPLACE_IF_EXISTS_TYPES_ARG, replaceIfExistsTypes);


        if(diffOutputControl.isReplaceIfExistsSet()) {
            command.addArgumentValue(GenerateChangelogCommandStep.USE_OR_REPLACE_OPTION, true);
        }
        command.setOutput(System.out);
        try {
            command.execute();
        } catch (CommandExecutionException e) {
            throw new LiquibaseException(e);
        }

    }

    public static String getBanner() {
        return new Banner().toString();
    }

}
