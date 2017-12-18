package liquibase.configuration;

/**
 * Configuration container for global properties.
 */
public class GlobalConfiguration extends AbstractConfigurationContainer {

    public static final String SHOULD_RUN = "shouldRun";
    public static final String DATABASECHANGELOG_TABLE_NAME = "databaseChangeLogTableName";
    public static final String DATABASECHANGELOGLOCK_TABLE_NAME = "databaseChangeLogLockTableName";
    public static final String LIQUIBASE_TABLESPACE_NAME = "tablespaceName";
    public static final String LIQUIBASE_CATALOG_NAME = "catalogName";
    public static final String LIQUIBASE_SCHEMA_NAME = "schemaName";
    public static final String OUTPUT_LINE_SEPARATOR = "outputLineSeparator";
    public static final String OUTPUT_ENCODING = "outputFileEncoding";
    public static final String CHANGELOGLOCK_WAIT_TIME = "changeLogLockWaitTimeInMinutes";
    public static final String CHANGELOGLOCK_POLL_RATE = "changeLogLockPollRate";
    public static final String CONVERT_DATA_TYPES = "convertDataTypes";
    public static final String GENERATE_CHANGESET_CREATED_VALUES = "generateChangeSetCreatedValues";
    public static final String AUTO_REORG = "autoReorg";
    public static final String DIFF_COLUMN_ORDER = "diffColumnOrder";
    public static final String ALWAYS_OVERRIDE_STORED_LOGIC_SCHEMA = "alwaysOverrideStoredLogicSchema";
    public static final String GENERATED_CHANGESET_IDS_INCLUDE_DESCRIPTION = "generatedChangeSetIdsContainsDescription";

    public GlobalConfiguration() {
        super("liquibase");

        getContainer().addProperty(SHOULD_RUN, Boolean.class)
            .setDescription("Should Liquibase commands execute")
                .setDefaultValue(true)
                .addAlias("should.run");

        getContainer().addProperty(DATABASECHANGELOG_TABLE_NAME, String.class)
                .setDescription("Name of table to use for tracking change history")
                .setDefaultValue("DATABASECHANGELOG");

        getContainer().addProperty(DATABASECHANGELOGLOCK_TABLE_NAME, String.class)
            .setDescription("Name of table to use for tracking concurrent Liquibase usage")
                .setDefaultValue("DATABASECHANGELOGLOCK");

        getContainer().addProperty(CHANGELOGLOCK_WAIT_TIME, Long.class)
                .setDescription("Number of minutes to wait for the changelog lock to be available before giving up")
                .setDefaultValue(5);

        getContainer().addProperty(CHANGELOGLOCK_POLL_RATE, Long.class)
                .setDescription("Number of seconds wait between checks to the changelog lock when it is locked")
                .setDefaultValue(10);

        getContainer().addProperty(LIQUIBASE_TABLESPACE_NAME, String.class)
            .setDescription("Tablespace to use for Liquibase objects");

        getContainer().addProperty(LIQUIBASE_CATALOG_NAME, String.class)
            .setDescription("Catalog to use for Liquibase objects");

        getContainer().addProperty(LIQUIBASE_SCHEMA_NAME, String.class)
            .setDescription("Schema to use for Liquibase objects");

        getContainer().addProperty(OUTPUT_LINE_SEPARATOR, String.class)
                .setDescription("Line separator for output. Defaults to OS default")
                .setDefaultValue(System.getProperty("line.separator"));

        getContainer().addProperty(OUTPUT_ENCODING, String.class)
                .setDescription("Encoding to output text in. Defaults to file.encoding system property or UTF-8")
                .setDefaultValue("UTF-8")
                .addAlias("file.encoding");

        getContainer().addProperty(CONVERT_DATA_TYPES, Boolean.class)
            .setDescription("Should Liquibase convert to/from STANDARD data types. Applies to both snapshot and " +
                "update commands.")
                .setDefaultValue(true);

        getContainer().addProperty(GENERATE_CHANGESET_CREATED_VALUES, Boolean.class)
            .setDescription("Should Liquibase include a 'created' attribute in diff/generateChangeLog changeSets with" +
                " the current datetime")
                .setDefaultValue(false);

        getContainer().addProperty(AUTO_REORG, Boolean.class)
            .setDescription("Should Liquibase automatically include REORG TABLE commands when needed?")
                .setDefaultValue(true);

        getContainer().addProperty(DIFF_COLUMN_ORDER, Boolean.class)
            .setDescription("Should Liquibase compare column order in diff operation?")
                .setDefaultValue(true);

        getContainer().addProperty(ALWAYS_OVERRIDE_STORED_LOGIC_SCHEMA, Boolean.class)
                .setDescription("When generating SQL for createProcedure, should the procedure schema be forced to the default schema if no schemaName attribute is set?")
                .setDefaultValue(false);


        getContainer().addProperty(GENERATED_CHANGESET_IDS_INCLUDE_DESCRIPTION, Boolean.class)
            .setDescription("Should Liquibase include the change description in the id when generating changeSets?")
                .setDefaultValue(false);
    }

    /**
     * Should Liquibase execute
     */
    public boolean getShouldRun() {
        return getContainer().getValue(SHOULD_RUN, Boolean.class);
    }

    public GlobalConfiguration setShouldRun(boolean shouldRun) {
        getContainer().setValue(SHOULD_RUN, shouldRun);
        return this;
    }

    /**
     * Table name to use for DATABASECHANGELOG
     */
    public String getDatabaseChangeLogTableName() {
        return getContainer().getValue(DATABASECHANGELOG_TABLE_NAME, String.class);
    }

    public GlobalConfiguration setDatabaseChangeLogTableName(String name) {
        getContainer().setValue(DATABASECHANGELOG_TABLE_NAME, name);
        return this;
    }

    /**
     * Table name to use for DATABASECHANGELOGLOCK
     */
    public String getDatabaseChangeLogLockTableName() {
        return getContainer().getValue(DATABASECHANGELOGLOCK_TABLE_NAME, String.class);
    }

    public GlobalConfiguration setDatabaseChangeLogLockTableName(String name) {
        getContainer().setValue(DATABASECHANGELOGLOCK_TABLE_NAME, name);
        return this;
    }

    /**
     * Wait time (in minutes) to wait to receive the changelog lock before giving up.
     */
    public Long getDatabaseChangeLogLockWaitTime() {
        return getContainer().getValue(CHANGELOGLOCK_WAIT_TIME, Long.class);
    }

    public GlobalConfiguration setDatabaseChangeLogLockWaitTime(Long minutes) {
        getContainer().setValue(CHANGELOGLOCK_WAIT_TIME, minutes);
        return this;
    }

    /**
     * Wait time (in seconds) between polling requests to the changelog lock system.
     */
    public Long getDatabaseChangeLogLockPollRate() {
        return getContainer().getValue(CHANGELOGLOCK_POLL_RATE, Long.class);
    }

    public GlobalConfiguration setDatabaseChangeLogLockPollRate(Long seconds) {
        getContainer().setValue(CHANGELOGLOCK_POLL_RATE, seconds);
        return this;
    }

    /**
     * Name of the tablespace to use for liquibase database objects
     */
    public String getLiquibaseTablespaceName() {
        return getContainer().getValue(LIQUIBASE_TABLESPACE_NAME, String.class);
    }

    public GlobalConfiguration setLiquibaseTablespaceName(String name) {
        getContainer().setValue(LIQUIBASE_TABLESPACE_NAME, name);
        return this;
    }

    /**
     * Name of the catalog to use for liquibase database objects
     */
    public String getLiquibaseCatalogName() {
        return getContainer().getValue(LIQUIBASE_CATALOG_NAME, String.class);
    }

    public GlobalConfiguration setLiquibaseCatalogName(String name) {
        getContainer().setValue(LIQUIBASE_CATALOG_NAME, name);
        return this;
    }

    /**
     * Name of the schema to use for liquibase database objects
     */
    public String getLiquibaseSchemaName() {
        return getContainer().getValue(LIQUIBASE_SCHEMA_NAME, String.class);
    }

    public GlobalConfiguration setLiquibaseSchemaName(String name) {
        getContainer().setValue(LIQUIBASE_SCHEMA_NAME, name);
        return this;
    }

    /**
     * Line separator to use in output
     */
    public String getOutputLineSeparator() {
        return getContainer().getValue(OUTPUT_LINE_SEPARATOR, String.class);
    }

    public GlobalConfiguration setOutputLineSeparator(String name) {
        getContainer().setValue(OUTPUT_LINE_SEPARATOR, name);
        return this;
    }

    /**
     * String encoding to use in output.
     */
    public String getOutputEncoding() {
        return getContainer().getValue(OUTPUT_ENCODING, String.class);
    }

    public GlobalConfiguration setOutputEncoding(String name) {
        getContainer().setValue(OUTPUT_ENCODING, name);
        return this;
    }

    public Boolean getDiffColumnOrder() {
        return getContainer().getValue(DIFF_COLUMN_ORDER, Boolean.class);
    }

    public GlobalConfiguration setDiffColumnOrder(boolean diff) {
        getContainer().setValue(DIFF_COLUMN_ORDER, diff);
        return this;
    }


    public Boolean getAlwaysOverrideStoredLogicSchema() {
        return getContainer().getValue(ALWAYS_OVERRIDE_STORED_LOGIC_SCHEMA, Boolean.class);
    }

    public GlobalConfiguration setAlwaysOverrideStoredLogicSchema(boolean override) {
        getContainer().setValue(ALWAYS_OVERRIDE_STORED_LOGIC_SCHEMA, override);
        return this;
    }


    public Boolean getGeneratedChangeSetIdsContainDescription() {
        return getContainer().getValue(GENERATED_CHANGESET_IDS_INCLUDE_DESCRIPTION, Boolean.class);
    }

    public GlobalConfiguration setGeneratedChangeSetIdsContainDescription(boolean containDescription) {
        getContainer().setValue(GENERATED_CHANGESET_IDS_INCLUDE_DESCRIPTION, containDescription);
        return this;
    }
}
