package liquibase.integration.commandline;

import liquibase.CatalogAndSchema;
import liquibase.command.CommandExecutionException;
import liquibase.command.DiffCommand;
import liquibase.command.DiffToChangeLogCommand;
import liquibase.command.GenerateChangeLogCommand;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.OfflineConnection;
import liquibase.database.core.*;
import liquibase.diff.DiffStatusListener;
import liquibase.diff.compare.CompareControl;
import liquibase.diff.output.DiffOutputControl;
import liquibase.exception.*;
import liquibase.executor.ExecutorService;
import liquibase.logging.LogFactory;
import liquibase.resource.ClassLoaderResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.snapshot.InvalidExampleException;
import liquibase.statement.core.RawSqlStatement;
import liquibase.structure.DatabaseObject;
import liquibase.structure.core.Schema;
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

            return createDatabaseObject(new ClassLoaderResourceAccessor(classLoader), url, username, password, driver, defaultCatalogName, defaultSchemaName, outputDefaultCatalog, outputDefaultSchema, databaseClass, driverPropertiesFile, propertyProviderClass, liquibaseCatalogName, liquibaseSchemaName, databaseChangeLogTableName, databaseChangeLogLockTableName);
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

            Database database = DatabaseFactory.getInstance().openDatabase(url, username, password, driver, databaseClass, driverPropertiesFile, propertyProviderClass, resourceAccessor);

            if (!database.supportsSchemas()) {
                if (defaultSchemaName != null && defaultCatalogName == null) {
                    defaultCatalogName = defaultSchemaName;
                }
                if (liquibaseSchemaName != null && liquibaseCatalogName == null) {
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
            if (databaseChangeLogTableName!=null) {
                database.setDatabaseChangeLogTableName(databaseChangeLogTableName);
                if (databaseChangeLogLockTableName!=null) {
                    database.setDatabaseChangeLogLockTableName(databaseChangeLogLockTableName);
                } else {
                    database.setDatabaseChangeLogLockTableName(databaseChangeLogTableName+"LOCK");
                }
            }
            
            //Todo: move to database object methods in 4.0
            if ((defaultCatalogName != null || defaultSchemaName != null) && !(database.getConnection() instanceof OfflineConnection)) {
                if (database instanceof OracleDatabase) {
                    String schema = defaultCatalogName;
                    if (schema == null) {
                        schema = defaultSchemaName;
                    }
                    ExecutorService.getInstance().getExecutor(database).execute(new RawSqlStatement("ALTER SESSION SET CURRENT_SCHEMA="+schema));
                } else if (database instanceof MSSQLDatabase && defaultSchemaName != null) {
                    ExecutorService.getInstance().getExecutor(database).execute(new RawSqlStatement("ALTER USER " + database.escapeObjectName(username, DatabaseObject.class) + " WITH DEFAULT_SCHEMA = " + database.escapeObjectName(defaultSchemaName, Schema.class)));
                } else if (database instanceof PostgresDatabase && defaultSchemaName != null) {
                    ExecutorService.getInstance().getExecutor(database).execute(new RawSqlStatement("SET SEARCH_PATH TO " + database.escapeObjectName(defaultSchemaName, Schema.class)));
                } else if (database instanceof DB2Database) {
                    String schema = defaultCatalogName;
                    if (schema == null) {
                        schema = defaultSchemaName;
                    }
                    ExecutorService.getInstance().getExecutor(database).execute(new RawSqlStatement("SET CURRENT SCHEMA "+schema));
                } else if (database instanceof MySQLDatabase) {
                    String schema = defaultCatalogName;
                    if (schema == null) {
                        schema = defaultSchemaName;
                    }
                    ExecutorService.getInstance().getExecutor(database).execute(new RawSqlStatement("USE "+schema));
                }

            }
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
