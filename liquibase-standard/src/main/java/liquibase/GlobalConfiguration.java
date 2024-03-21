package liquibase;

import liquibase.command.CommandArgumentDefinition;
import liquibase.configuration.AutoloadedConfigurations;
import liquibase.configuration.ConfigurationDefinition;
import liquibase.ui.UIServiceEnum;
import liquibase.util.ValueHandlerUtil;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Configuration container for global properties.
 */
public class GlobalConfiguration implements AutoloadedConfigurations {

    public static final ConfigurationDefinition<String> DATABASECHANGELOG_TABLE_NAME;
    public static final ConfigurationDefinition<String> DATABASECHANGELOGLOCK_TABLE_NAME;
    public static final ConfigurationDefinition<String> LIQUIBASE_TABLESPACE_NAME;
    public static final ConfigurationDefinition<String> LIQUIBASE_CATALOG_NAME;
    public static final ConfigurationDefinition<String> LIQUIBASE_SCHEMA_NAME;
    public static final ConfigurationDefinition<String> OUTPUT_LINE_SEPARATOR;
    public static final ConfigurationDefinition<String> OUTPUT_FILE_ENCODING;
    public static final ConfigurationDefinition<Charset> FILE_ENCODING;
    public static final ConfigurationDefinition<Long> CHANGELOGLOCK_WAIT_TIME;
    public static final ConfigurationDefinition<Long> CHANGELOGLOCK_POLL_RATE;
    public static final ConfigurationDefinition<Boolean> CONVERT_DATA_TYPES;
    public static final ConfigurationDefinition<Boolean> GENERATE_CHANGESET_CREATED_VALUES;
    public static final ConfigurationDefinition<Boolean> AUTO_REORG;
    public static final ConfigurationDefinition<Boolean> DIFF_COLUMN_ORDER;
    public static final ConfigurationDefinition<Boolean> ALWAYS_OVERRIDE_STORED_LOGIC_SCHEMA;
    public static final ConfigurationDefinition<Boolean> GENERATED_CHANGESET_IDS_INCLUDE_DESCRIPTION;
    public static final ConfigurationDefinition<Boolean> INCLUDE_CATALOG_IN_SPECIFICATION;
    public static final ConfigurationDefinition<Boolean> SHOULD_SNAPSHOT_DATA;
    public static final ConfigurationDefinition<Boolean> INCLUDE_RELATIONS_FOR_COMPUTED_COLUMNS;
    public static final ConfigurationDefinition<Boolean> PRESERVE_SCHEMA_CASE;
    public static final ConfigurationDefinition<Boolean> SHOW_BANNER;
    public static final ConfigurationDefinition<Boolean> ALWAYS_DROP_INSTEAD_OF_REPLACE;
    public static final ConfigurationDefinition<DuplicateFileMode> DUPLICATE_FILE_MODE;
    public static final ConfigurationDefinition<Boolean> ALLOW_DUPLICATED_CHANGESETS_IDENTIFIERS;

    public static final ConfigurationDefinition<Boolean> VALIDATE_XML_CHANGELOG_FILES;

    /**
     * @deprecated No longer used
     */
    @Deprecated
    public static final ConfigurationDefinition<Boolean> FILTER_LOG_MESSAGES;
    public static final ConfigurationDefinition<Boolean> HEADLESS;
    public static final ConfigurationDefinition<Boolean> STRICT;
    public static final ConfigurationDefinition<Integer> DDL_LOCK_TIMEOUT;
    public static final ConfigurationDefinition<Boolean> SECURE_PARSING;
    public static final ConfigurationDefinition<String> SEARCH_PATH;

    public static final ConfigurationDefinition<UIServiceEnum> UI_SERVICE;

    static {
        ConfigurationDefinition.Builder builder = new ConfigurationDefinition.Builder("liquibase");

        DATABASECHANGELOG_TABLE_NAME = builder.define("databaseChangelogTableName", String.class)
                .addAliasKey("liquibase.databaseChangeLogTableName")
                .setDescription("Name of table to use for tracking change history")
                .setDefaultValue("DATABASECHANGELOG")
                .build();

        DATABASECHANGELOGLOCK_TABLE_NAME = builder.define("databaseChangelogLockTableName", String.class)
                .addAliasKey("liquibase.databaseChangeLogLockTableName")
                .setDescription("Name of table to use for tracking concurrent Liquibase usage")
                .setDefaultValue("DATABASECHANGELOGLOCK")
                .build();

        CHANGELOGLOCK_WAIT_TIME = builder.define("changelogLockWaitTimeInMinutes", Long.class)
                .addAliasKey("liquibase.changeLogLockWaitTimeInMinutes")
                .setDescription("Number of minutes to wait for the changelog lock to be available before giving up")
                .setDefaultValue(5L)
                .build();

        CHANGELOGLOCK_POLL_RATE = builder.define("changelogLockPollRate", Long.class)
                .addAliasKey("liquibase.changeLogLockPollRate")
                .setDescription("Number of seconds wait between checks to the changelog lock when it is locked")
                .setDefaultValue(10L)
                .build();

        LIQUIBASE_TABLESPACE_NAME = builder.define("liquibaseTablespaceName", String.class)
                .addAliasKey("liquibase.liquibaseTableSpaceName")
                .addAliasKey("liquibase.databaseChangeLogTablespaceName")
                .setDescription("Tablespace to use for Liquibase objects")
                .build();

        LIQUIBASE_CATALOG_NAME = builder.define("liquibaseCatalogName", String.class)
                .addAliasKey("liquibase.catalogName")
                .setDescription("Catalog to use for Liquibase objects")
                .build();

        LIQUIBASE_SCHEMA_NAME = builder.define("liquibaseSchemaName", String.class)
                .addAliasKey("liquibase.schemaName")
                .setDescription("Schema to use for Liquibase objects")
                .build();

        OUTPUT_LINE_SEPARATOR = builder.define("outputLineSeparator", String.class)
                .setDescription("Line separator for output")
                .setDefaultValue(System.getProperty("line.separator"),"Line separator(LF or CRLF) for output. Defaults to OS default")
                .build();

        FILE_ENCODING = builder.define("fileEncoding", Charset.class)
                .setDescription("Encoding to use when reading files. Valid values include: UTF-8, UTF-16, UTF-16BE, UTF-16LE, US-ASCII, or OS to use the system configured encoding.")
                .setDefaultValue(StandardCharsets.UTF_8)
                .setValueHandler(value -> {
                    if (value == null) {
                        return StandardCharsets.UTF_8;
                    }
                    if (value instanceof Charset) {
                        return (Charset) value;
                    }
                    final String valueString = String.valueOf(value);
                    if (valueString.equalsIgnoreCase("os")) {
                        return Charset.defaultCharset();
                    } else {
                        return Charset.forName(valueString);
                    }
                })
                .setCommonlyUsed(true)
                .build();

        OUTPUT_FILE_ENCODING = builder.define("outputFileEncoding", String.class)
                .setDescription("Encoding to use when writing files")
                .setDefaultValue(StandardCharsets.UTF_8.name())
                .setCommonlyUsed(true)
                .build();

        CONVERT_DATA_TYPES = builder.define("convertDataTypes", Boolean.class)
                .setDescription("Should Liquibase convert to/from STANDARD data types. Applies to both snapshot and " +
                        "update commands.")
                .setValueHandler(ValueHandlerUtil::booleanValueHandler)
                .setDefaultValue(true)
                .build();

        GENERATE_CHANGESET_CREATED_VALUES = builder.define("generateChangesetCreatedValues", Boolean.class)
                .addAliasKey("liquibase.generateChangeSetCreatedValues")
                .setDescription("Should Liquibase include a 'created' attribute in diff/generateChangelog changesets with" +
                        " the current datetime")
                .setValueHandler(ValueHandlerUtil::booleanValueHandler)
                .setDefaultValue(false)
                .build();

        AUTO_REORG = builder.define("autoReorg", Boolean.class)
                .setDescription("Should Liquibase automatically include REORG TABLE commands when needed?")
                .setValueHandler(ValueHandlerUtil::booleanValueHandler)
                .setDefaultValue(true)
                .build();

        DIFF_COLUMN_ORDER = builder.define("diffColumnOrder", Boolean.class)
                .setDescription("Should Liquibase compare column order in diff operation?")
                .setValueHandler(ValueHandlerUtil::booleanValueHandler)
                .setDefaultValue(true)
                .build();

        ALWAYS_OVERRIDE_STORED_LOGIC_SCHEMA = builder.define("alwaysOverrideStoredLogicSchema", Boolean.class)
                .setDescription("When generating SQL for createProcedure, should the procedure schema be forced to the default schema if no schemaName attribute is set?")
                .setValueHandler(ValueHandlerUtil::booleanValueHandler)
                .setDefaultValue(false)
                .build();


        GENERATED_CHANGESET_IDS_INCLUDE_DESCRIPTION = builder.define("generatedChangesetIdsContainsDescription", Boolean.class)
                .addAliasKey("liquibase.generatedChangeSetIdsContainsDescription")
                .setDescription("Should Liquibase include the change description in the id when generating changesets?")
                .setValueHandler(ValueHandlerUtil::booleanValueHandler)
                .setDefaultValue(false)
                .build();

        INCLUDE_CATALOG_IN_SPECIFICATION = builder.define("includeCatalogInSpecification", Boolean.class)
                .setDescription("Should Liquibase include the catalog name when determining equality?")
                .setValueHandler(ValueHandlerUtil::booleanValueHandler)
                .setDefaultValue(false)
                .build();

        SHOULD_SNAPSHOT_DATA = builder.define("shouldSnapshotData", Boolean.class)
                .setDescription("Should Liquibase snapshot data by default?")
                .setValueHandler(ValueHandlerUtil::booleanValueHandler)
                .setDefaultValue(false)
                .build();

        INCLUDE_RELATIONS_FOR_COMPUTED_COLUMNS = builder.define("includeRelationsForComputedColumns", Boolean.class)
                .setDescription("If true, the parent relationship for computed columns is preserved in snapshot-dependent commands: snapshot and diff")
                .setValueHandler(ValueHandlerUtil::booleanValueHandler)
                .setDefaultValue(false)
                .build();

        FILTER_LOG_MESSAGES = builder.define("filterLogMessages", Boolean.class)
                .setDescription("DEPRECATED: No longer used")
                .setCommonlyUsed(false)
                .setValueHandler(ValueHandlerUtil::booleanValueHandler)
                .build();

        HEADLESS = builder.define("headless", Boolean.class)
                .setDescription("Force Liquibase to think it has no access to a keyboard")
                .setValueHandler(ValueHandlerUtil::booleanValueHandler)
                .setDefaultValue(false)
                .setCommonlyUsed(true)
                .build();

        STRICT = builder.define("strict", Boolean.class)
                .setDescription("If true, Liquibase enforces certain best practices and proactively looks for common errors")
                .setValueHandler(ValueHandlerUtil::booleanValueHandler)
                .setDefaultValue(false)
                .build();

        DDL_LOCK_TIMEOUT = builder.define("ddlLockTimeout", Integer.class)
                .addAliasKey("liquibase.ddlLockTimeout")
                .addAliasKey("ddl_lock_timeout")
                .addAliasKey("liquibase.ddl_lock_timeout")
                .setDescription("The DDL_LOCK_TIMEOUT parameter indicates the number of seconds a DDL command should wait for the locks to become available before throwing the resource busy error message. This applies only to Oracle databases.")
                .build();

        SECURE_PARSING = builder.define("secureParsing", Boolean.class)
                .setDescription("If true, remove functionality from file parsers which could be used insecurely. Examples include (but not limited to) disabling remote XML entity support.")
                .setValueHandler(ValueHandlerUtil::booleanValueHandler)
                .setDefaultValue(true)
                .build();

        PRESERVE_SCHEMA_CASE = builder.define("preserveSchemaCase", Boolean.class)
                .setDescription("If true, Liquibase treats schema and catalog names as case sensitive")
                .setValueHandler(ValueHandlerUtil::booleanValueHandler)
                .setDefaultValue(false)
                .build();

        SHOW_BANNER = builder.define("showBanner", Boolean.class)
                .setDescription("If true, show a Liquibase banner on startup.")
                .setValueHandler(ValueHandlerUtil::booleanValueHandler)
                .setDefaultValue(true)
                .build();

        DUPLICATE_FILE_MODE = builder.define("duplicateFileMode", DuplicateFileMode.class)
                .setDescription("How to handle multiple files being found in the search path that have duplicate paths. Options are WARN (log warning and choose one at random) or ERROR (fail current operation)")
                .setDefaultValue(DuplicateFileMode.ERROR)
                .build();

        SEARCH_PATH = builder.define("searchPath", String.class)
                .setDescription("Complete list of Location(s) to search for files such as changelog files in. Multiple paths can be specified by separating them with commas.")
                .build();

            ALWAYS_DROP_INSTEAD_OF_REPLACE = builder.define("alwaysDropInsteadOfReplace", Boolean.class)
                .setDescription("If true, drop and recreate a view instead of replacing it.")
                .setDefaultValue(false)
                .setValueHandler(ValueHandlerUtil::booleanValueHandler)
                .build();

        ALLOW_DUPLICATED_CHANGESETS_IDENTIFIERS = builder.define("allowDuplicatedChangesetIdentifiers", Boolean.class)
                .setDescription("Allows duplicated changeset identifiers without failing Liquibase execution.")
                .setValueHandler(ValueHandlerUtil::booleanValueHandler)
                .setDefaultValue(false)
                .build();

        VALIDATE_XML_CHANGELOG_FILES = builder.define("validateXmlChangelogFiles", Boolean.class)
                .setDescription("Will perform XSD validation of XML changelog files. When many XML changelog files are included, this validation may impact Liquibase performance. Defaults to true.")
                .setValueHandler(ValueHandlerUtil::booleanValueHandler)
                .setDefaultValue(true)
                .build();

        UI_SERVICE = builder.define("uiService", UIServiceEnum.class)
                .setDescription("Changes the default UI Service Logger used by Liquibase. Options are CONSOLE or LOGGER.")
                .setDefaultValue(UIServiceEnum.CONSOLE)
                .setValueHandler(o -> ValueHandlerUtil.getEnum(UIServiceEnum.class, o, "UiService"))
                .build();
    }

    public enum DuplicateFileMode {
        WARN,
        ERROR,
    }
}
