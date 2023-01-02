package liquibase.command.core;

import liquibase.CatalogAndSchema;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.command.*;
import liquibase.configuration.ConfigurationValueObfuscator;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.exception.DatabaseException;
import liquibase.integration.commandline.CommandLineUtils;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.SnapshotSerializerFactory;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.util.StringUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.*;

import static java.util.ResourceBundle.getBundle;

public class SnapshotCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"snapshot"};
    private static final ResourceBundle coreBundle = getBundle("liquibase/i18n/liquibase-core");

    public static final CommandArgumentDefinition<String> USERNAME_ARG;
    public static final CommandArgumentDefinition<String> PASSWORD_ARG;
    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> SCHEMAS_ARG;
    public static final CommandArgumentDefinition<String> DEFAULT_SCHEMA_NAME_ARG;
    public static final CommandArgumentDefinition<String> DEFAULT_CATALOG_NAME_ARG;
    public static final CommandArgumentDefinition<String> SNAPSHOT_FORMAT_ARG;
    public static final CommandArgumentDefinition<String> DRIVER_ARG;
    public static final CommandArgumentDefinition<String> DRIVER_PROPERTIES_FILE_ARG;
    public static final CommandArgumentDefinition<Database> DATABASE_ARG;
    public static final CommandArgumentDefinition<SnapshotControl> SNAPSHOT_CONTROL_ARG;

    static {
        CommandBuilder builder = new CommandBuilder(COMMAND_NAME);
        URL_ARG = builder.argument(CommonArgumentNames.URL, String.class).required()
                .description("The JDBC database connection URL").build();
        SCHEMAS_ARG = builder.argument("schemas", String.class)
                .description("The schemas to snapshot").build();
        DEFAULT_SCHEMA_NAME_ARG = builder.argument("defaultSchemaName", String.class)
                .description("The default schema name to use for the database connection").build();
        DEFAULT_CATALOG_NAME_ARG = builder.argument("defaultCatalogName", String.class)
                .description("The default catalog name to use for the database connection").build();
        DRIVER_ARG = builder.argument("driver", String.class)
                .description("The JDBC driver class").build();
        DRIVER_PROPERTIES_FILE_ARG = builder.argument("driverPropertiesFile", String.class)
                .description("The JDBC driver properties file").build();
        USERNAME_ARG = builder.argument(CommonArgumentNames.USERNAME, String.class)
                .description("Username to use to connect to the database").build();
        PASSWORD_ARG = builder.argument(CommonArgumentNames.PASSWORD, String.class)
                .description("Password to use to connect to the database")
                .setValueObfuscator(ConfigurationValueObfuscator.STANDARD)
                .build();
        SNAPSHOT_FORMAT_ARG = builder.argument("snapshotFormat", String.class)
                .description("Output format to use (JSON, YAML, or TXT)").build();
        DATABASE_ARG = builder.argument("database", Database.class).hidden().build();
        SNAPSHOT_CONTROL_ARG = builder.argument("snapshotControl", SnapshotControl.class).hidden().build();
    }

    private Database database;

    private Map<String, Object> snapshotMetadata;

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Capture the current state of the database");
    }

    private CatalogAndSchema[] parseSchemas(Database database, String... schemas) {
        if ((schemas == null) || (schemas.length == 0) || (schemas[0] == null)) {
            return null;
        }

        schemas = StringUtil.join(schemas, ",").split("\\s*,\\s*");
        List<CatalogAndSchema> finalList = new ArrayList<>();
        for (String schema : schemas) {
            finalList.add(new CatalogAndSchema(null, schema).customize(database));
        }

        return finalList.toArray(new CatalogAndSchema[0]);
    }

    public Map<String, Object> getSnapshotMetadata() {
        return snapshotMetadata;
    }

    public void setSnapshotMetadata(Map<String, Object> snapshotMetadata) {
        this.snapshotMetadata = snapshotMetadata;
    }

    @Override
    public void run(CommandResultsBuilder resultsBuilder) throws Exception {
        CommandScope commandScope = resultsBuilder.getCommandScope();

        if (commandScope.getArgumentValue(DATABASE_ARG) == null) {
            String url = commandScope.getArgumentValue(SnapshotCommandStep.URL_ARG);
            String username = commandScope.getArgumentValue(SnapshotCommandStep.USERNAME_ARG);
            String password = commandScope.getArgumentValue(SnapshotCommandStep.PASSWORD_ARG);
            String defaultSchemaName = commandScope.getArgumentValue(SnapshotCommandStep.DEFAULT_SCHEMA_NAME_ARG);
            String defaultCatalogName = commandScope.getArgumentValue(SnapshotCommandStep.DEFAULT_CATALOG_NAME_ARG);
            String driver = commandScope.getArgumentValue(SnapshotCommandStep.DRIVER_ARG);
            String driverPropertiesFile = commandScope.getArgumentValue(SnapshotCommandStep.DRIVER_PROPERTIES_FILE_ARG);
            createDatabaseObject(url, username, password, defaultSchemaName, defaultCatalogName, driver, driverPropertiesFile);
        } else {
            database = commandScope.getArgumentValue(DATABASE_ARG);
        }

        CatalogAndSchema[] schemas = parseSchemas(database, commandScope.getArgumentValue(SCHEMAS_ARG));

        InternalSnapshotCommandStep.logUnsupportedDatabase(database, this.getClass());
        SnapshotControl snapshotControl;
        if (commandScope.getArgumentValue(SNAPSHOT_CONTROL_ARG) == null) {
            snapshotControl = new SnapshotControl(database);
        } else {
            snapshotControl = commandScope.getArgumentValue(SnapshotCommandStep.SNAPSHOT_CONTROL_ARG);
        }

        if (schemas == null) {
            schemas = new CatalogAndSchema[]{database.getDefaultSchema()};
        }

        ObjectQuotingStrategy originalQuotingStrategy = database.getObjectQuotingStrategy();

        database.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);

        try {
            DatabaseSnapshot snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(schemas, database, snapshotControl);

            snapshot.setMetadata(this.getSnapshotMetadata());

            resultsBuilder.addResult("snapshot", snapshot);
            resultsBuilder.addResult("statusCode", 0);

            OutputStream outputStream = resultsBuilder.getOutputStream();
            if (outputStream != null) {
                String result = printSnapshot(commandScope, snapshot);
                Writer outputWriter = getOutputWriter(outputStream);
                outputWriter.write(result);
                outputWriter.flush();
            }
        } finally {
            //
            // Reset the quoting strategy
            //
            database.setObjectQuotingStrategy(originalQuotingStrategy);

            //
            // Need to clean up here since we created the Database
            //
            if (commandScope.getArgumentValue(DATABASE_ARG) == null) {
                closeDatabase();
            }
        }
    }

    private Writer getOutputWriter(final OutputStream outputStream) throws IOException {
        String charsetName = GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue();

        return new OutputStreamWriter(outputStream, charsetName);
    }

    /**
     *
     * Method to create a Database object given these parameters
     *
     * @param  url                       URL to connect to
     * @param  username                  Username credential
     * @param  password                  Password credential
     * @param  defaultSchemaName         Default schema for connection
     * @param  defaultCatalogName        Default catalog for connection
     * @param  driver                    Driver class
     * @param  driverPropertiesFile      Additional driver properties
     * @throws DatabaseException         Thrown when there is a connection error
     *
     */
    private void createDatabaseObject(String url,
                                      String username,
                                      String password,
                                      String defaultSchemaName,
                                      String defaultCatalogName,
                                      String driver,
                                      String driverPropertiesFile)
            throws DatabaseException {
        ResourceAccessor resourceAccessor = Scope.getCurrentScope().getResourceAccessor();
        String databaseClassName = null;
        Class databaseClass = LiquibaseCommandLineConfiguration.DATABASE_CLASS.getCurrentValue();
        if (databaseClass != null) {
            databaseClassName = databaseClass.getCanonicalName();
        }
        String propertyProviderClass = null;
        Class clazz = LiquibaseCommandLineConfiguration.PROPERTY_PROVIDER_CLASS.getCurrentValue();
        if (clazz != null) {
            propertyProviderClass = clazz.getName();
        }
        String liquibaseCatalogName = GlobalConfiguration.LIQUIBASE_CATALOG_NAME.getCurrentValue();
        String liquibaseSchemaName = GlobalConfiguration.LIQUIBASE_SCHEMA_NAME.getCurrentValue();
        String databaseChangeLogTablespaceName = GlobalConfiguration.LIQUIBASE_TABLESPACE_NAME.getCurrentValue();
        String databaseChangeLogLockTableName = GlobalConfiguration.DATABASECHANGELOGLOCK_TABLE_NAME.getCurrentValue();
        String databaseChangeLogTableName = GlobalConfiguration.DATABASECHANGELOG_TABLE_NAME.getCurrentValue();
        database =
                CommandLineUtils.createDatabaseObject(resourceAccessor,
                        url,
                        username,
                        password,
                        driver,
                        defaultCatalogName,
                        defaultSchemaName,
                        true,
                        true,
                        databaseClassName,
                        driverPropertiesFile,
                        propertyProviderClass,
                        liquibaseCatalogName, liquibaseSchemaName,
                        databaseChangeLogTableName,
                        databaseChangeLogLockTableName);
        database.setLiquibaseTablespaceName(databaseChangeLogTablespaceName);
    }

    private void closeDatabase() {
        try {
            if (database != null) {
                database.rollback();
                database.close();
            }
        } catch (Exception e) {
            Scope.getCurrentScope().getLog(getClass()).warning(
                    coreBundle.getString("problem.closing.connection"), e);
        }
    }

    private String printSnapshot(CommandScope commandScope, DatabaseSnapshot snapshot) {
        String format = commandScope.getArgumentValue(SNAPSHOT_FORMAT_ARG);
        if (format == null) {
            format = "txt";
        }

        return SnapshotSerializerFactory.getInstance()
                                        .getSerializer(format.toLowerCase(Locale.US))
                                        .serialize(snapshot, true);
    }

}
