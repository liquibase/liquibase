package liquibase.integration.commandline;

import liquibase.CatalogAndSchema;
import liquibase.command.CommandExecutionException;
import liquibase.command.CommandFactory;
import liquibase.command.core.DiffCommand;
import liquibase.command.core.DiffToChangeLogCommand;
import liquibase.command.core.GenerateChangeLogCommand;
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
 * Common Utility methods used in the CommandLine application and the Maven plugin.
 * These methods were originally moved from {@link Main} so they could be shared.
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
            initializeDatabase(username, defaultCatalogName, defaultSchemaName, database);

//            ValidationErrors errors = database.validate();
//            if (errors.hasErrors()) {
//                throw new DatabaseException("Database validation failed: "+errors.toString());
//            } else {
//                for (String warning : errors.getWarningMessages()) {
//                    LogFactory.getInstance().getLog().warning(warning);
//                }
//            }
            return database;
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * Executes RawSqlStatements particular to each database engine to set the default schema for the given Database
     *  
     * @param username            The username used for the connection. Used with MSSQL databases
     * @param defaultCatalogName  Catalog name and schema name are similar concepts. Used if defaultCatalogName is null. 
     * @param defaultSchemaName   Catalog name and schema name are similar concepts. Catalog is used with Oracle, DB2 and MySQL, and takes
     *                             precedence over the schema name.
     * @param database            Which Database object is affected by the initialization.
     * @throws DatabaseException
     */
    public static void initializeDatabase(String username, String defaultCatalogName, String defaultSchemaName, Database database) throws DatabaseException {
        if ((defaultCatalogName != null || defaultSchemaName != null) && !(database.getConnection() instanceof OfflineConnection)) {
            if (database instanceof OracleDatabase) {
                String schema = defaultCatalogName;
                if (schema == null) {
                    schema = defaultSchemaName;
                }
                ExecutorService.getInstance().getExecutor(database).execute(new RawSqlStatement("ALTER SESSION SET CURRENT_SCHEMA="+database.escapeObjectName(schema, Schema.class)));
            } else if (database instanceof MSSQLDatabase && defaultSchemaName != null) {
boolean sql2005OrLater = true;
                    try {
                        sql2005OrLater = database.getDatabaseMajorVersion() >= 9;
                    } catch (DatabaseException e) {
                        // Assume SQL Server 2005 or later
                    }
                    if (sql2005OrLater && username != null) {
                        ExecutorService.getInstance().getExecutor(database).execute(new RawSqlStatement(
                                "IF USER_NAME() <> N'dbo'\r\n" +
                                "BEGIN\r\n" +
                                "	DECLARE @sql [nvarchar](MAX)\r\n" +
                                "	SELECT @sql = N'ALTER USER ' + QUOTENAME(USER_NAME()) + N' WITH DEFAULT_SCHEMA = " + database.escapeStringForDatabase(database.escapeObjectName(username, DatabaseObject.class)) + "'\r\n" +
                                "	EXEC sp_executesql @sql\r\n" +
                                "END"));
                    }            } else if (database instanceof PostgresDatabase && defaultSchemaName != null) {
                    ExecutorService.getInstance().getExecutor(database).execute(new RawSqlStatement("SET SEARCH_PATH TO " + database.escapeObjectName(defaultSchemaName, Schema.class)));
            } else if (database instanceof AbstractDb2Database) {
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
    }

    public static void doDiff(Database referenceDatabase, Database targetDatabase, String snapshotTypes) throws LiquibaseException {
        doDiff(referenceDatabase, targetDatabase, snapshotTypes, null);
    }

    public static void doDiff(Database referenceDatabase, Database targetDatabase, String snapshotTypes, CompareControl.SchemaComparison[] schemaComparisons) throws LiquibaseException {
        DiffCommand diffCommand = (DiffCommand) CommandFactory.getInstance().getCommand("diff");

        diffCommand
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

        DiffToChangeLogCommand command = (DiffToChangeLogCommand) CommandFactory.getInstance().getCommand("diffChangeLog");
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

        GenerateChangeLogCommand command = (GenerateChangeLogCommand) CommandFactory.getInstance().getCommand("generateChangeLog");

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
