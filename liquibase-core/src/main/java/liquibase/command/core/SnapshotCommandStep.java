package liquibase.command.core;

import liquibase.CatalogAndSchema;
import liquibase.GlobalConfiguration;
import liquibase.Scope;
import liquibase.command.*;
import liquibase.configuration.ConfigurationValueObfuscator;
import liquibase.configuration.LiquibaseConfiguration;
import liquibase.database.Database;
import liquibase.database.ObjectQuotingStrategy;
import liquibase.database.core.*;
import liquibase.exception.LiquibaseException;
import liquibase.integration.commandline.CommandLineResourceAccessor;
import liquibase.integration.commandline.CommandLineUtils;
import liquibase.integration.commandline.LiquibaseCommandLineConfiguration;
import liquibase.integration.commandline.Main;
import liquibase.license.LicenseServiceUtils;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;
import liquibase.serializer.SnapshotSerializerFactory;
import liquibase.snapshot.DatabaseSnapshot;
import liquibase.snapshot.SnapshotControl;
import liquibase.snapshot.SnapshotGeneratorFactory;
import liquibase.snapshot.SnapshotListener;
import liquibase.util.StringUtil;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SnapshotCommandStep extends AbstractCommandStep {

    public static final String[] COMMAND_NAME = {"snapshot"};

    public static final CommandArgumentDefinition<String> USERNAME_ARG;
    public static final CommandArgumentDefinition<String> PASSWORD_ARG;
    public static final CommandArgumentDefinition<String> URL_ARG;
    public static final CommandArgumentDefinition<String> SCHEMAS_ARG;
    public static final CommandArgumentDefinition<String> DEFAULT_SCHEMA_NAME_ARG;
    public static final CommandArgumentDefinition<String> DEFAULT_CATALOG_NAME_ARG;
    public static final CommandArgumentDefinition<String> SNAPSHOT_FORMAT_ARG;
    public static final CommandArgumentDefinition<String> DRIVER_ARG;
    public static final CommandArgumentDefinition<String> DRIVER_PROPERTIES_FILE_ARG;
    public static final CommandArgumentDefinition<String> SERIALIZER_FORMAT_ARG;
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
                .description("Output format to use (JSON or YAML").build();
        SERIALIZER_FORMAT_ARG = builder.argument("serializerFormat", String.class).build();
    }

    private Map<String, Object> snapshotMetadata;

    public static final String SNAPSHOT_RESULTS_BUILDER = "snapshotResultsBuilder";

    @Override
    public String[][] defineCommandNames() {
        return new String[][] { COMMAND_NAME };
    }

    @Override
    public void adjustCommandDefinition(CommandDefinition commandDefinition) {
        commandDefinition.setShortDescription("Capture the current state of the database");
    }

    public static CatalogAndSchema[] parseSchemas(Database database, String... schemas) {
        if ((schemas == null) || (schemas.length == 0) || (schemas[0] == null)) {
            return null;
        }

        schemas = StringUtil.join(schemas, ",").split("\\s*,\\s*");
        List<CatalogAndSchema> finalList = new ArrayList<>();
        for (String schema : schemas) {
            finalList.add(new CatalogAndSchema(null, schema).customize(database));
        }

        return finalList.toArray(new CatalogAndSchema[finalList.size()]);
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
        final ResourceAccessor fileOpener = Scope.getCurrentScope().getResourceAccessor();
        String url = commandScope.getArgumentValue(URL_ARG);
        String username = commandScope.getArgumentValue(USERNAME_ARG);
        String password = commandScope.getArgumentValue(PASSWORD_ARG);
        String driver = commandScope.getArgumentValue(DRIVER_ARG);
        String defaultCatalogName = commandScope.getArgumentValue(DEFAULT_CATALOG_NAME_ARG);
        String defaultSchemaName = commandScope.getArgumentValue(DEFAULT_SCHEMA_NAME_ARG);
        String driverPropertiesFile = commandScope.getArgumentValue(DRIVER_PROPERTIES_FILE_ARG);
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
        Database database =
            CommandLineUtils.createDatabaseObject(fileOpener,
                                                  url,
                                                  username, password, driver, defaultCatalogName, defaultSchemaName,
                                                  false, false,
                                                  databaseClassName,
                                                  driverPropertiesFile,
                                                  propertyProviderClass,
                                                  liquibaseCatalogName, liquibaseSchemaName,
                                                  null,
                                                  null);
        database.setLiquibaseTablespaceName(databaseChangeLogTablespaceName);

        CatalogAndSchema[] schemas = parseSchemas(database, commandScope.getArgumentValue(SCHEMAS_ARG));

        logUnsupportedDatabase(database, this.getClass());
        SnapshotControl snapshotControl = new SnapshotControl(database);

        if (schemas == null) {
            schemas = new CatalogAndSchema[]{database.getDefaultSchema()};
        }

        ObjectQuotingStrategy originalQuotingStrategy = database.getObjectQuotingStrategy();

        database.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS);
        DatabaseSnapshot snapshot;
        try {
            snapshot = SnapshotGeneratorFactory.getInstance().createSnapshot(schemas, database, snapshotControl);
        } finally {
            database.setObjectQuotingStrategy(originalQuotingStrategy);
        }

        snapshot.setMetadata(this.getSnapshotMetadata());

        resultsBuilder.addResult("snapshot", snapshot);

        OutputStream outputStream = resultsBuilder.getOutputStream();
        if (outputStream != null) {
            String result = printSnapshot(resultsBuilder.getCommandScope(), resultsBuilder.build());
            Writer outputWriter = getOutputWriter(outputStream);
            outputWriter.write(result);
            outputWriter.flush();
        }
    }

    private Writer getOutputWriter(final OutputStream outputStream) throws IOException {
        String charsetName = GlobalConfiguration.OUTPUT_FILE_ENCODING.getCurrentValue();

        return new OutputStreamWriter(outputStream, charsetName);
    }

    public static String printSnapshot(CommandScope commandScope, CommandResults snapshotResults) throws LiquibaseException {
        String format = commandScope.getArgumentValue(SERIALIZER_FORMAT_ARG);
        if (format == null) {
            format = "txt";
        }

        return SnapshotSerializerFactory.getInstance().getSerializer(format.toLowerCase(Locale.US)).serialize((DatabaseSnapshot) snapshotResults.getResult("snapshot"), true);
    }

    public static void logUnsupportedDatabase(Database database, Class callingClass) {
        if (LicenseServiceUtils.isProLicenseValid()) {
            if (!(database instanceof MSSQLDatabase
                || database instanceof OracleDatabase
                || database instanceof MySQLDatabase
                || database instanceof DB2Database
                || database instanceof PostgresDatabase)) {
                Scope.getCurrentScope().getUI().sendMessage("INFO This command might not yet capture Liquibase Pro additional object types on " + database.getShortName());
            }
        }
    }

}
