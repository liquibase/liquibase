package liquibase;

import liquibase.command.core.DiffCommandStep;
import liquibase.configuration.AutoloadedConfigurations;
import liquibase.configuration.ConfigurationDefinition;
import liquibase.ui.UIServiceEnum;

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
    public static final ConfigurationDefinition<Boolean> INCLUDE_SCHEMA_NAME_FOR_DEFAULT;
    public static final ConfigurationDefinition<Boolean> FAIL_ON_NULL_SNAPSHOT_ID;
    public static final ConfigurationDefinition<Boolean> PRESERVE_SCHEMA_CASE;
    public static final ConfigurationDefinition<Boolean> SHOW_BANNER;
    public static final ConfigurationDefinition<Boolean> ALWAYS_DROP_INSTEAD_OF_REPLACE;
    public static final ConfigurationDefinition<DuplicateFileMode> DUPLICATE_FILE_MODE;
    public static final ConfigurationDefinition<Boolean> ALLOW_DUPLICATED_CHANGESETS_IDENTIFIERS;

    public static final ConfigurationDefinition<Boolean> VALIDATE_XML_CHANGELOG_FILES;

    public static final ConfigurationDefinition<Boolean> TRIM_LOAD_DATA_FILE_HEADER;

    /**
     * @deprecated No longer used
     */
    @Deprecated
    public static final ConfigurationDefinition<Boolean> FILTER_LOG_MESSAGES;
    public static final ConfigurationDefinition<Boolean> HEADLESS;
    public static final ConfigurationDefinition<Boolean> STRICT;
    public static final ConfigurationDefinition<Integer> DDL_LOCK_TIMEOUT;
    public static final ConfigurationDefinition<Boolean> SECURE_PARSING;
    public static final ConfigurationDefinition<Boolean> ALLOW_CUSTOM_CHANGE;
    public static final ConfigurationDefinition<Boolean> ALLOW_EXECUTE_COMMAND;
    public static final ConfigurationDefinition<Boolean> ALLOW_EXTERNAL_CHANGELOG_PATHS;
    public static final ConfigurationDefinition<Boolean> ALLOW_INCLUDE_ALL_CLASSES;
    public static final ConfigurationDefinition<Boolean> ALLOW_PARENT_DIRECTORY_REFERENCES;
    public static final ConfigurationDefinition<Boolean> ALLOW_SQL_PRECONDITION;
    public static final ConfigurationDefinition<String> SEARCH_PATH;

    public static final ConfigurationDefinition<UIServiceEnum> UI_SERVICE;
    public static final ConfigurationDefinition<SupportsMethodValidationLevelsEnum> SUPPORTS_METHOD_VALIDATION_LEVEL;

    public static final ConfigurationDefinition<Boolean> PRESERVE_CLASSPATH_PREFIX_IN_NORMALIZED_PATHS;
    public static final ConfigurationDefinition<Boolean> ALLOW_INHERIT_LOGICAL_FILE_PATH;
    public static final ConfigurationDefinition<Boolean> DIFF_COLUMN_DEFAULT_VALUE_CONSTRAINT_NAME;

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
                .setDefaultValue(true)
                .build();

        GENERATE_CHANGESET_CREATED_VALUES = builder.define("generateChangesetCreatedValues", Boolean.class)
                .addAliasKey("liquibase.generateChangeSetCreatedValues")
                .setDescription("Should Liquibase include a 'created' attribute in diff/generateChangelog changesets with" +
                        " the current datetime")
                .setDefaultValue(false)
                .build();

        AUTO_REORG = builder.define("autoReorg", Boolean.class)
                .setDescription("Should Liquibase automatically include REORG TABLE commands when needed?")
                .setDefaultValue(true)
                .build();

        DIFF_COLUMN_ORDER = builder.define("diffColumnOrder", Boolean.class)
                .setDescription("Should Liquibase compare column order in diff operation?")
                .setDefaultValue(true)
                .build();

        ALWAYS_OVERRIDE_STORED_LOGIC_SCHEMA = builder.define("alwaysOverrideStoredLogicSchema", Boolean.class)
                .setDescription("When generating SQL for createProcedure, should the procedure schema be forced to the default schema if no schemaName attribute is set?")
                .setDefaultValue(false)
                .build();


        GENERATED_CHANGESET_IDS_INCLUDE_DESCRIPTION = builder.define("generatedChangesetIdsContainsDescription", Boolean.class)
                .addAliasKey("liquibase.generatedChangeSetIdsContainsDescription")
                .setDescription("Should Liquibase include the change description in the id when generating changesets?")
                .setDefaultValue(false)
                .build();

        INCLUDE_CATALOG_IN_SPECIFICATION = builder.define("includeCatalogInSpecification", Boolean.class)
                .setDescription("Should Liquibase include the catalog name when determining equality?")
                .setDefaultValue(false)
                .build();

        SHOULD_SNAPSHOT_DATA = builder.define("shouldSnapshotData", Boolean.class)
                .setDescription("Should Liquibase snapshot data by default?")
                .setDefaultValue(false)
                .build();

        INCLUDE_RELATIONS_FOR_COMPUTED_COLUMNS = builder.define("includeRelationsForComputedColumns", Boolean.class)
                .setDescription("If true, the parent relationship for computed columns is preserved in snapshot-dependent commands: snapshot and diff")
                .setDefaultValue(false)
                .build();

        INCLUDE_SCHEMA_NAME_FOR_DEFAULT = builder.define("includeSchemaNameForDefault", Boolean.class)
                .setDescription("If true, the schema name is included for the default schema when loading a snapshot")
                .setDefaultValue(false)
                .build();

        FAIL_ON_NULL_SNAPSHOT_ID = builder.define("failOnNullSnapshotId", Boolean.class)
                .setDescription("If true, referenced objects which do not have a snapshot ID will cause snapshot failure")
                .setDefaultValue(true)
                .build();

        FILTER_LOG_MESSAGES = builder.define("filterLogMessages", Boolean.class)
                .setDescription("DEPRECATED: No longer used")
                .setCommonlyUsed(false)
                .build();

        HEADLESS = builder.define("headless", Boolean.class)
                .setDescription("Force Liquibase to think it has no access to a keyboard")
                .setDefaultValue(false)
                .setCommonlyUsed(true)
                .build();

        STRICT = builder.define("strict", Boolean.class)
                .setDescription("If true, Liquibase enforces certain best practices and proactively looks for common errors")
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
                .setDefaultValue(true)
                .build();

        ALLOW_CUSTOM_CHANGE = builder.define("allowCustomChange", Boolean.class)
                .setDescription("If false, the customChange and customPrecondition changelog elements are " +
                        "rejected before the named class is loaded. Defaults to true to preserve the " +
                        "documented custom-Java features for the standard trust model (team-authored, " +
                        "team-reviewed changelogs). Set to false in environments that execute changelogs " +
                        "from less-trusted sources (multi-tenant SaaS running customer changelogs, " +
                        "downloaded change-packs, contributor PRs prior to review): both customChange " +
                        "and customPrecondition load an arbitrary JVM class by FQCN via " +
                        "Class.forName(initialize=true), which fires the class's static <clinit> " +
                        "initializer at load time — before any cast or marker-interface check could " +
                        "reject the load. Any class on the JVM classpath is reachable this way (CWE-470).")
                .setDefaultValue(true)
                .build();

        ALLOW_EXECUTE_COMMAND = builder.define("allowExecuteCommand", Boolean.class)
                .setDescription("If false, the executeCommand changelog change is rejected at validation time " +
                        "with a clear error instead of being allowed to invoke an OS shell command. " +
                        "Defaults to true to preserve the documented executeCommand feature for the standard " +
                        "trust model (team-authored, team-reviewed changelogs). Set to false in environments " +
                        "that execute changelogs from less-trusted sources (multi-tenant SaaS running customer " +
                        "changelogs, downloaded change-packs, contributor PRs prior to review) where arbitrary " +
                        "OS-shell execution via changelog is not an acceptable risk (CWE-78).")
                .setDefaultValue(true)
                .build();

        ALLOW_EXTERNAL_CHANGELOG_PATHS = builder.define("allowExternalChangelogPaths", Boolean.class)
                .setDescription("If false, the include / includeAll / sqlFile changelog directives reject " +
                        "paths that point outside the configured ResourceAccessor search-path scope — " +
                        "specifically: the 'classpath:' URI prefix, and absolute filesystem paths (leading " +
                        "'/', leading '\\\\' UNC, or Windows drive-letter '<L>:'). Defaults to true to " +
                        "preserve the documented behaviour for the standard trust model, including " +
                        "common deployments like Spring Boot apps that load 'classpath:db/changelog/...' " +
                        "from JAR resources. Set to false in environments that execute changelogs from " +
                        "less-trusted sources (multi-tenant SaaS, downloaded change-packs, contributor " +
                        "PRs prior to review): the audit observed that an attacker who can write a file " +
                        "anywhere on the ResourceAccessor search path (which by default in the CLI " +
                        "includes the current working directory) can then name it in a changelog " +
                        "include / sqlFile and get it parsed and executed. Restricting changelog paths " +
                        "to relative-only-within-search-path (this flag set to false) is a defence-" +
                        "in-depth mitigation; tightly-controlled search-path configuration alone also " +
                        "mitigates the issue. The flag's enforcement is bypassed for any include / " +
                        "includeAll / sqlFile that uses relativeToChangelogFile=true (the path is " +
                        "resolved relative to the parent changelog and cannot escape its directory) " +
                        "(CWE-22).")
                .setDefaultValue(true)
                .build();

        ALLOW_INCLUDE_ALL_CLASSES = builder.define("allowIncludeAllClasses", Boolean.class)
                .setDescription("If false, the includeAll changelog directive's resourceFilter and " +
                        "resourceComparator attributes are rejected when they reference a class name. " +
                        "Defaults to true to preserve the documented includeAll feature for the standard " +
                        "trust model (team-authored, team-reviewed changelogs). Set to false in environments " +
                        "that execute changelogs from less-trusted sources (multi-tenant SaaS running customer " +
                        "changelogs, downloaded change-packs, contributor PRs prior to review): both " +
                        "attributes load an arbitrary JVM class by FQCN via Class.forName(initialize=true), " +
                        "which fires the class's static <clinit> initializer at load time — before any cast " +
                        "or marker-interface check could reject the load. Any class on the JVM classpath is " +
                        "reachable this way (CWE-470). This flag governs the same unsafe-reflection surface " +
                        "as liquibase.allowCustomChange (which gates the <customChange> and " +
                        "<customPrecondition> changelog elements); operators wanting to fully lock down " +
                        "changelog-controlled class loading must set both flags to false.")
                .setDefaultValue(true)
                .build();

        ALLOW_PARENT_DIRECTORY_REFERENCES = builder.define("allowParentDirectoryReferences", Boolean.class)
                .setDescription("If true (the default), AbstractPathResourceAccessor allows path payloads " +
                        "containing '..' segments and symbolic links that resolve outside the configured " +
                        "root directory. This preserves the behaviour that existed before the CWE-22 " +
                        "path-containment fix landed, for legitimate multi-changelog layouts that depend on " +
                        "parent-directory traversal — for example a shared 'dbarepo' at the project root " +
                        "referenced as '../shared/foo.xml' from per-environment changelogs underneath it, " +
                        "or a custom-check SCRIPT_PATH like '../checks/policy.py'. Set to false to enforce " +
                        "strict containment: any '..' that resolves outside the accessor root, or any " +
                        "symbolic link whose canonical real path escapes the canonical root, is rejected " +
                        "with IOException. The default is true for one major release as a deprecation " +
                        "window; a future major release will flip the default to false, at which point " +
                        "callers depending on parent-directory traversal must either restructure their " +
                        "layout or explicitly opt in via this flag (CWE-22).")
                .setDefaultValue(true)
                .build();

        ALLOW_SQL_PRECONDITION = builder.define("allowSqlPrecondition", Boolean.class)
                .setDescription("If false, the sqlCheck changelog precondition is rejected at validation " +
                        "and check time without executing its SQL body. Defaults to true to preserve the " +
                        "documented sqlCheck feature for the standard trust model (team-authored, team-" +
                        "reviewed changelogs). Set to false in environments that execute changelogs from " +
                        "less-trusted sources (multi-tenant SaaS running customer changelogs, downloaded " +
                        "change-packs, contributor PRs prior to review): sqlCheck runs the literal SQL body " +
                        "from the changelog against the live JDBC connection during precondition " +
                        "evaluation, before the change body is reached. On drivers permitting multi-" +
                        "statement execution (e.g. MySQL/MariaDB with allowMultiQueries=true), additional " +
                        "DDL or DML can be batched into the sqlCheck body and run regardless of the " +
                        "expectedResult comparison; the SQL also executes through a less-reviewed code " +
                        "path that bypasses the change-execution audit trail, and an onFail=MARK_RAN " +
                        "precondition can hide the change body from being applied while the precondition " +
                        "SQL has already run (CWE-89).")
                .setDefaultValue(true)
                .build();

        PRESERVE_SCHEMA_CASE = builder.define("preserveSchemaCase", Boolean.class)
                .setDescription("If true, Liquibase treats schema and catalog names as case sensitive")
                .setDefaultValue(false)
                .build();

        SHOW_BANNER = builder.define("showBanner", Boolean.class)
                .setDescription("If true, show a Liquibase banner on startup.")
                .setDefaultValue(true)
                .build();

        DUPLICATE_FILE_MODE = builder.define("duplicateFileMode", DuplicateFileMode.class)
                .setDescription("How to handle multiple files being found in the search path that have duplicate paths. Options are SILENT (do not log and choose one at random), DEBUG, INFO, WARN (log at the given level and choose one at random), or ERROR (fail current operation).")
                .setDefaultValue(DuplicateFileMode.ERROR)
                .build();

        SEARCH_PATH = builder.define("searchPath", String.class)
                .setDescription("Complete list of Location(s) to search for files such as changelog files in. Multiple paths can be specified by separating them with commas.")
                .build();

        ALWAYS_DROP_INSTEAD_OF_REPLACE = builder.define("alwaysDropInsteadOfReplace", Boolean.class)
                .setDescription("If true, drop and recreate a view instead of replacing it.")
                .setDefaultValue(false)
                .build();

        ALLOW_DUPLICATED_CHANGESETS_IDENTIFIERS = builder.define("allowDuplicatedChangesetIdentifiers", Boolean.class)
                .setDescription("Allows duplicated changeset identifiers without failing Liquibase execution.")
                .setDefaultValue(false)
                .build();

        VALIDATE_XML_CHANGELOG_FILES = builder.define("validateXmlChangelogFiles", Boolean.class)
                .setDescription("Will perform XSD validation of XML changelog files. When many XML changelog files are included, this validation may impact Liquibase performance. Defaults to true.")
                .setDefaultValue(true)
                .build();

        UI_SERVICE = builder.define("uiService", UIServiceEnum.class)
                .setDescription("Changes the default UI Service Logger used by Liquibase. Options are CONSOLE or LOGGER.")
                .setDefaultValue(UIServiceEnum.CONSOLE)
                .build();

        SUPPORTS_METHOD_VALIDATION_LEVEL = builder.define("supportsMethodValidationLevel", SupportsMethodValidationLevelsEnum.class)
                .setDescription("Controls the level of validation performed on the supports method of Change classes. Options are OFF, WARN, FAIL.")
                .setDefaultValue(SupportsMethodValidationLevelsEnum.WARN)
                .build();
        TRIM_LOAD_DATA_FILE_HEADER = builder.define("trimLoadDataFileHeader", Boolean.class)
                .setDescription("If true column headers will be trimmed in case they were specified with spaces in the file.")
                .setDefaultValue(false)
                .build();

        PRESERVE_CLASSPATH_PREFIX_IN_NORMALIZED_PATHS = builder.define("preserveClasspathPrefixInNormalizedPaths", Boolean.class)
                .setDescription("If true 'classpath:' prefix will be preserved in normalized paths, allowing to resolve hierarchical resources under a classpath-based root.")
                .setDefaultValue(false)
                .build();

        ALLOW_INHERIT_LOGICAL_FILE_PATH = builder.define("allowInheritLogicalFilePath", Boolean.class)
                .setDescription("If true, included changelogs without an explicit logicalFilePath will inherit their parent changelog's logicalFilePath, and explicit logicalFilePath attributes on include statements are honored (Liquibase 4.31.0+ behavior). If false, included changelogs use their physical file paths, ignoring both implicit inheritance and explicit logicalFilePath attributes on include statements. Only logicalFilePath set directly on the changelog itself is respected. Defaults to true for backward compatibility.")
                .setDefaultValue(true)
                .build();

        DIFF_COLUMN_DEFAULT_VALUE_CONSTRAINT_NAME = builder.define("diffColumnDefaultValueConstraintName", Boolean.class)
                .setDescription("Should Liquibase compare column default value constraint name in diff operation?")
                .setDefaultValue(true)
                .build();
    }

    public enum DuplicateFileMode {
        WARN,
        ERROR,
        INFO,
        DEBUG,
        SILENT
    }

}
