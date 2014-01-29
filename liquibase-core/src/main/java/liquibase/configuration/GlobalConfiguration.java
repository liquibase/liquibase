package liquibase.configuration;

public class GlobalConfiguration extends AbstractConfiguration {

    public static final String SHOULD_RUN = "shouldRun";
    public static final String DATABASECHANGELOG_TABLE_NAME = "databaseChangeLogTableName";
    public static final String DATABASECHANGELOGLOCK_TABLE_NAME = "databaseChangeLogLockTableName";
    public static final String LIQUIBASE_TABLESPACE_NAME = "tablespaceName";
    public static final String LIQUIBASE_CATALOG_NAME = "catalogName";
    public static final String LIQUIBASE_SCHEMA_NAME = "schemaName";
    public static final String OUTPUT_LINE_SEPARATOR = "outputLineSeparator";
    public static final String OUTPUT_ENCODING = "outputFileEncoding";

    public GlobalConfiguration() {
        super("liquibase");

        getContainer().addProperty(SHOULD_RUN, Boolean.class)
                .setDescription("Should Liquibase commands execute")
                .setDefaultValue(true)
                .addAlias("liquibase.should.run");

        getContainer().addProperty(DATABASECHANGELOG_TABLE_NAME, String.class)
                .setDescription("Name of table to use for tracking change history")
                .setDefaultValue("DATABASECHANGELOG");

        getContainer().addProperty(DATABASECHANGELOGLOCK_TABLE_NAME, String.class)
                .setDescription("Name of table to use for tracking concurrent liquibase usage")
                .setDefaultValue("DATABASECHANGELOGLOCK");

        getContainer().addProperty(LIQUIBASE_TABLESPACE_NAME, String.class)
                .setDescription("Tablespace to use for liquibase objects");

        getContainer().addProperty(LIQUIBASE_CATALOG_NAME, String.class)
                .setDescription("Catalog to use for liquibase objects");

        getContainer().addProperty(LIQUIBASE_SCHEMA_NAME, String.class)
                .setDescription("Schema to use for liquibase objects");

        getContainer().addProperty(OUTPUT_LINE_SEPARATOR, String.class)
                .setDescription("Line separator for output. Defaults to OS default")
                .setDefaultValue(System.getProperty("line.separator"));

        getContainer().addProperty(OUTPUT_ENCODING, String.class)
                .setDescription("Encoding to output text in. Defaults to file.encoding system property or UTF-8")
                .setDefaultValue(System.getProperty("file.encoding") == null ? "UTF-8" : System.getProperty("file.encoding"))
                .addAlias("file.encoding");
    }

    public boolean getShouldRun() {
        return getContainer().getValue(SHOULD_RUN, Boolean.class);
    }

    public GlobalConfiguration setShouldRun(boolean shouldRun) {
        getContainer().setValue(SHOULD_RUN, shouldRun);
        return this;
    }

    public String getDatabaseChangeLogTableName() {
        return getContainer().getValue(DATABASECHANGELOG_TABLE_NAME, String.class);
    }

    public GlobalConfiguration setDatabaseChangeLogTableName(String name) {
        getContainer().setValue(DATABASECHANGELOG_TABLE_NAME, name);
        return this;
    }

    public String getDatabaseChangeLogLockTableName() {
        return getContainer().getValue(DATABASECHANGELOGLOCK_TABLE_NAME, String.class);
    }

    public GlobalConfiguration setDatabaseChangeLogLockTableName(String name) {
        getContainer().setValue(DATABASECHANGELOGLOCK_TABLE_NAME, name);
        return this;
    }

    public String getLiquibaseTablespaceName() {
        return getContainer().getValue(LIQUIBASE_TABLESPACE_NAME, String.class);
    }

    public GlobalConfiguration setLiquibaseTablespaceName(String name) {
        getContainer().setValue(LIQUIBASE_TABLESPACE_NAME, name);
        return this;
    }

    public String getLiquibaseCatalogName() {
        return getContainer().getValue(LIQUIBASE_CATALOG_NAME, String.class);
    }

    public GlobalConfiguration setLiquibaseCatalogName(String name) {
        getContainer().setValue(LIQUIBASE_CATALOG_NAME, name);
        return this;
    }

    public String getLiquibaseSchemaName() {
        return getContainer().getValue(LIQUIBASE_SCHEMA_NAME, String.class);
    }

    public GlobalConfiguration setLiquibaseSchemaName(String name) {
        getContainer().setValue(LIQUIBASE_SCHEMA_NAME, name);
        return this;
    }

    public String getOutputLineSeparator() {
        return getContainer().getValue(OUTPUT_LINE_SEPARATOR, String.class);
    }

    public GlobalConfiguration setOutputLineSeparator(String name) {
        getContainer().setValue(OUTPUT_LINE_SEPARATOR, name);
        return this;
    }

    public String getOutputEncoding() {
        return getContainer().getValue(OUTPUT_ENCODING, String.class);
    }

    public GlobalConfiguration setOutputEncoding(String name) {
        getContainer().setValue(OUTPUT_ENCODING, name);
        return this;
    }
}
