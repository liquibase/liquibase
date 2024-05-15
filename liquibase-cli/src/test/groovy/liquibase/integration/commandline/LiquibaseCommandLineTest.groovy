package liquibase.integration.commandline

import liquibase.Scope
import liquibase.command.CommandBuilder
import liquibase.configuration.ConfigurationDefinition
import liquibase.exception.LiquibaseException
import liquibase.logging.core.BufferedLogService
import liquibase.ui.ConsoleUIService
import liquibase.util.StringUtil
import spock.lang.Specification
import spock.lang.Unroll

import java.util.logging.Level

class LiquibaseCommandLineTest extends Specification {

    def expectedHelpOutput = """
Usage: liquibase [GLOBAL OPTIONS] [COMMAND] [COMMAND OPTIONS]
Command-specific help: "liquibase <command-name> --help"

Global Options
      --allow-duplicated-changeset-identifiers=PARAM
                             Allows duplicated changeset identifiers without
                               failing Liquibase execution.
                             DEFAULT: false
                             (defaults file: 'liquibase.
                               allowDuplicatedChangesetIdentifiers',
                               environment variable:
                               'LIQUIBASE_ALLOW_DUPLICATED_CHANGESET_IDENTIFIERS
                               ')

      --always-drop-instead-of-replace=PARAM
                             If true, drop and recreate a view instead of
                               replacing it.
                             DEFAULT: false
                             (defaults file: 'liquibase.
                               alwaysDropInsteadOfReplace', environment
                               variable:
                               'LIQUIBASE_ALWAYS_DROP_INSTEAD_OF_REPLACE')

      --always-override-stored-logic-schema=PARAM
                             When generating SQL for createProcedure, should
                               the procedure schema be forced to the default
                               schema if no schemaName attribute is set?
                             DEFAULT: false
                             (defaults file: 'liquibase.
                               alwaysOverrideStoredLogicSchema', environment
                               variable:
                               'LIQUIBASE_ALWAYS_OVERRIDE_STORED_LOGIC_SCHEMA')

      --auto-reorg=PARAM     Should Liquibase automatically include REORG TABLE
                               commands when needed?
                             DEFAULT: true
                             (defaults file: 'liquibase.autoReorg', environment
                               variable: 'LIQUIBASE_AUTO_REORG')

      --changelog-lock-poll-rate=PARAM
                             Number of seconds wait between checks to the
                               changelog lock when it is locked
                             DEFAULT: 10
                             (defaults file: 'liquibase.changelogLockPollRate',
                               environment variable:
                               'LIQUIBASE_CHANGELOG_LOCK_POLL_RATE')

      --changelog-lock-wait-time-in-minutes=PARAM
                             Number of minutes to wait for the changelog lock
                               to be available before giving up
                             DEFAULT: 5
                             (defaults file: 'liquibase.
                               changelogLockWaitTimeInMinutes', environment
                               variable:
                               'LIQUIBASE_CHANGELOG_LOCK_WAIT_TIME_IN_MINUTES')

      --changelog-parse-mode=PARAM
                             Configures how to handle unknown fields in
                               changelog files. Possible values: STRICT which
                               causes parsing to fail, and LAX which continues
                               with the parsing.
                             DEFAULT: STRICT
                             (defaults file: 'liquibase.changelogParseMode',
                               environment variable:
                               'LIQUIBASE_CHANGELOG_PARSE_MODE')

      --classpath=PARAM      Additional classpath entries to use
                             (defaults file: 'liquibase.classpath', environment
                               variable: 'LIQUIBASE_CLASSPATH')

      --convert-data-types=PARAM
                             Should Liquibase convert to/from STANDARD data
                               types. Applies to both snapshot and update
                               commands.
                             DEFAULT: true
                             (defaults file: 'liquibase.convertDataTypes',
                               environment variable:
                               'LIQUIBASE_CONVERT_DATA_TYPES')

      --database-changelog-lock-table-name=PARAM
                             Name of table to use for tracking concurrent
                               Liquibase usage
                             DEFAULT: DATABASECHANGELOGLOCK
                             (defaults file: 'liquibase.
                               databaseChangelogLockTableName', environment
                               variable:
                               'LIQUIBASE_DATABASE_CHANGELOG_LOCK_TABLE_NAME')

      --database-changelog-table-name=PARAM
                             Name of table to use for tracking change history
                             DEFAULT: DATABASECHANGELOG
                             (defaults file: 'liquibase.
                               databaseChangelogTableName', environment
                               variable:
                               'LIQUIBASE_DATABASE_CHANGELOG_TABLE_NAME')

      --database-class=PARAM Class to use for Database implementation
                             (defaults file: 'liquibase.databaseClass',
                               environment variable: 'LIQUIBASE_DATABASE_CLASS')

      --ddl-lock-timeout=PARAM
                             The DDL_LOCK_TIMEOUT parameter indicates the
                               number of seconds a DDL command should wait for
                               the locks to become available before throwing
                               the resource busy error message. This applies
                               only to Oracle databases.
                             (defaults file: 'liquibase.ddlLockTimeout',
                               environment variable:
                               'LIQUIBASE_DDL_LOCK_TIMEOUT')

      --defaults-file=PARAM  File with default Liquibase properties
                             DEFAULT: liquibase.properties
                             (defaults file: 'liquibase.defaultsFile',
                               environment variable: 'LIQUIBASE_DEFAULTS_FILE')

      --diff-column-order=PARAM
                             Should Liquibase compare column order in diff
                               operation?
                             DEFAULT: true
                             (defaults file: 'liquibase.diffColumnOrder',
                               environment variable:
                               'LIQUIBASE_DIFF_COLUMN_ORDER')

      --driver=PARAM         Database driver class
                             (defaults file: 'liquibase.driver', environment
                               variable: 'LIQUIBASE_DRIVER')

      --driver-properties-file=PARAM
                             Driver-specific properties
                             (defaults file: 'liquibase.driverPropertiesFile',
                               environment variable:
                               'LIQUIBASE_DRIVER_PROPERTIES_FILE')

      --duplicate-file-mode=PARAM
                             How to handle multiple files being found in the
                               search path that have duplicate paths. Options
                               are WARN (log warning and choose one at random)
                               or ERROR (fail current operation)
                             DEFAULT: ERROR
                             (defaults file: 'liquibase.duplicateFileMode',
                               environment variable:
                               'LIQUIBASE_DUPLICATE_FILE_MODE')

      --error-on-circular-include-all=PARAM
                             Throw an error if Liquibase detects that an
                               includeAll will cause a circular reference (and
                               thus a changelog parse error).
                             DEFAULT: true
                             (defaults file: 'liquibase.
                               errorOnCircularIncludeAll', environment
                               variable:
                               'LIQUIBASE_ERROR_ON_CIRCULAR_INCLUDE_ALL')

      --file-encoding=PARAM  Encoding to use when reading files. Valid values
                               include: UTF-8, UTF-16, UTF-16BE, UTF-16LE,
                               US-ASCII, or OS to use the system configured
                               encoding.
                             DEFAULT: UTF-8
                             (defaults file: 'liquibase.fileEncoding',
                               environment variable: 'LIQUIBASE_FILE_ENCODING')

      --filter-log-messages=PARAM
                             DEPRECATED: No longer used
                             (defaults file: 'liquibase.filterLogMessages',
                               environment variable:
                               'LIQUIBASE_FILTER_LOG_MESSAGES')

      --generate-changeset-created-values=PARAM
                             Should Liquibase include a 'created' attribute in
                               diff/generateChangelog changesets with the
                               current datetime
                             DEFAULT: false
                             (defaults file: 'liquibase.
                               generateChangesetCreatedValues', environment
                               variable:
                               'LIQUIBASE_GENERATE_CHANGESET_CREATED_VALUES')

      --generated-changeset-ids-contains-description=PARAM
                             Should Liquibase include the change description in
                               the id when generating changesets?
                             DEFAULT: false
                             (defaults file: 'liquibase.
                               generatedChangesetIdsContainsDescription',
                               environment variable:
                               'LIQUIBASE_GENERATED_CHANGESET_IDS_CONTAINS_DESCR
                               IPTION')

  -h, --help                 Show this help message and exit

      --headless=PARAM       Force liquibase to think it has no access to a
                               keyboard
                             DEFAULT: false
                             (defaults file: 'liquibase.headless', environment
                               variable: 'LIQUIBASE_HEADLESS')

      --include-catalog-in-specification=PARAM
                             Should Liquibase include the catalog name when
                               determining equality?
                             DEFAULT: false
                             (defaults file: 'liquibase.
                               includeCatalogInSpecification', environment
                               variable:
                               'LIQUIBASE_INCLUDE_CATALOG_IN_SPECIFICATION')

      --include-relations-for-computed-columns=PARAM
                             If true, the parent relationship for computed
                               columns is preserved in snapshot-dependent
                               commands: snapshot and diff
                             DEFAULT: false
                             (defaults file: 'liquibase.
                               includeRelationsForComputedColumns', environment
                               variable:
                               'LIQUIBASE_INCLUDE_RELATIONS_FOR_COMPUTED_COLUMNS
                               ')

      --include-system-classpath=PARAM
                             Include the system classpath when resolving
                               classes at runtime
                             DEFAULT: true
                             (defaults file: 'liquibase.
                               includeSystemClasspath', environment variable:
                               'LIQUIBASE_INCLUDE_SYSTEM_CLASSPATH')

      --liquibase-catalog-name=PARAM
                             Catalog to use for Liquibase objects
                             (defaults file: 'liquibase.liquibaseCatalogName',
                               environment variable:
                               'LIQUIBASE_LIQUIBASE_CATALOG_NAME')

      --liquibase-schema-name=PARAM
                             Schema to use for Liquibase objects
                             (defaults file: 'liquibase.liquibaseSchemaName',
                               environment variable:
                               'LIQUIBASE_LIQUIBASE_SCHEMA_NAME')

      --liquibase-tablespace-name=PARAM
                             Tablespace to use for Liquibase objects
                             (defaults file: 'liquibase.
                               liquibaseTablespaceName', environment variable:
                               'LIQUIBASE_LIQUIBASE_TABLESPACE_NAME')

      --log-channels=PARAM   DEFAULT: Controls which log channels have their
                               level set by the liquibase.logLevel setting.
                               Comma separate multiple values. To set the level
                               of all channels, use 'all'. Example: liquibase,
                               org.mariadb.jdbc
                             (defaults file: 'liquibase.logChannels',
                               environment variable: 'LIQUIBASE_LOG_CHANNELS')

      --log-file=PARAM       (defaults file: 'liquibase.logFile', environment
                               variable: 'LIQUIBASE_LOG_FILE')

      --log-format=PARAM     Sets the format of log output to console or log
                               files. Open Source users default to unstructured
                               "TEXT" logs to the console or output log files.
                               Pro users have the option to set value as "JSON"
                               or "JSON_PRETTY" to enable json-structured log
                               files to the console or output log files.
                             DEFAULT: TEXT
                             (defaults file: 'liquibase.logFormat', environment
                               variable: 'LIQUIBASE_LOG_FORMAT')

      --log-level=PARAM      DEFAULT: Controls which logs get set to stderr AND
                               to any log file. The CLI defaults, if log file
                               set, to SEVERE. Others vary by integration. The
                               official log levels are: OFF, SEVERE, WARNING,
                               INFO, FINE
                             (defaults file: 'liquibase.logLevel', environment
                               variable: 'LIQUIBASE_LOG_LEVEL')

      --mirror-console-messages-to-log=PARAM
                             When set to true, the console messages are
                               mirrored to the logs as [liquibase.ui] to
                               provide a more complete picture of liquibase
                               operations to log analysis tools. Set to false
                               to change this behavior.
                             DEFAULT: true
                             (defaults file: 'liquibase.
                               mirrorConsoleMessagesToLog', environment
                               variable:
                               'LIQUIBASE_MIRROR_CONSOLE_MESSAGES_TO_LOG')

      --missing-property-mode=PARAM
                             How to handle changelog property expressions where
                               a value is not set. For example, a string 'null'
                               when no 'address' property was defined. Values
                               can be: 'preserve' which leaves the string
                               as-is, 'empty' which replaces it with an empty
                               string, or 'error' which stops processing with
                               an error.
                             DEFAULT: PRESERVE
                             (defaults file: 'liquibase.missingPropertyMode',
                               environment variable:
                               'LIQUIBASE_MISSING_PROPERTY_MODE')

      --monitor-performance=PARAM
                             Enable performance tracking. Set to 'false' to
                               disable. If set to 'true', data is stored to a
                               `liquibase-TIMESTAMP.jfr` file in your working
                               directory. Any other value will enable tracking
                               and be used as the name of the file to write the
                               data to.
                             DEFAULT: false
                             (defaults file: 'liquibase.monitorPerformance',
                               environment variable:
                               'LIQUIBASE_MONITOR_PERFORMANCE')

      --on-missing-include-changelog=PARAM
                             If set to WARN, then liquibase will not throw
                               exception on missing changelog file, instead
                               will show a warning message.
                             DEFAULT: FAIL
                             (defaults file: 'liquibase.
                               onMissingIncludeChangelog', environment
                               variable:
                               'LIQUIBASE_ON_MISSING_INCLUDE_CHANGELOG')

      --output-file=PARAM    (defaults file: 'liquibase.outputFile',
                               environment variable: 'LIQUIBASE_OUTPUT_FILE')

      --output-file-encoding=PARAM
                             Encoding to use when writing files
                             DEFAULT: UTF-8
                             (defaults file: 'liquibase.outputFileEncoding',
                               environment variable:
                               'LIQUIBASE_OUTPUT_FILE_ENCODING')

      --output-line-separator=PARAM
                             Line separator for output
                             DEFAULT: Line separator(LF or CRLF) for output.
                               Defaults to OS default
                             (defaults file: 'liquibase.outputLineSeparator',
                               environment variable:
                               'LIQUIBASE_OUTPUT_LINE_SEPARATOR')

      --preserve-schema-case=PARAM
                             Should liquibase treat schema and catalog names as
                               case sensitive?
                             DEFAULT: false
                             (defaults file: 'liquibase.preserveSchemaCase',
                               environment variable:
                               'LIQUIBASE_PRESERVE_SCHEMA_CASE')

      --prompt-for-non-local-database=PARAM
                             Should Liquibase prompt if a non-local database is
                               being accessed
                             (defaults file: 'liquibase.
                               promptForNonLocalDatabase', environment
                               variable:
                               'LIQUIBASE_PROMPT_FOR_NON_LOCAL_DATABASE')

      --property-provider-class=PARAM
                             Implementation of Properties class to provide
                               additional driver properties
                             (defaults file: 'liquibase.propertyProviderClass',
                               environment variable:
                               'LIQUIBASE_PROPERTY_PROVIDER_CLASS')

      --search-path=PARAM    Complete list of Location(s) to search for files
                               such as changelog files in. Multiple paths can
                               be specified by separating them with commas.
                             (defaults file: 'liquibase.searchPath',
                               environment variable: 'LIQUIBASE_SEARCH_PATH')

      --secure-parsing=PARAM If true, remove functionality from file parsers
                               which could be used insecurely. Examples include
                               (but not limited to) disabling remote XML entity
                               support.
                             DEFAULT: true
                             (defaults file: 'liquibase.secureParsing',
                               environment variable: 'LIQUIBASE_SECURE_PARSING')

      --should-run=PARAM     Should Liquibase commands execute
                             DEFAULT: true
                             (defaults file: 'liquibase.shouldRun', environment
                               variable: 'LIQUIBASE_SHOULD_RUN')

      --should-snapshot-data=PARAM
                             Should Liquibase snapshot data by default?
                             DEFAULT: false
                             (defaults file: 'liquibase.shouldSnapshotData',
                               environment variable:
                               'LIQUIBASE_SHOULD_SNAPSHOT_DATA')

      --show-banner=PARAM    If true, show a Liquibase banner on startup.
                             DEFAULT: true
                             (defaults file: 'liquibase.showBanner',
                               environment variable: 'LIQUIBASE_SHOW_BANNER')

      --sql-log-level=PARAM  Level to log SQL statements to
                             DEFAULT: FINE
                             (defaults file: 'liquibase.sql.logLevel',
                               environment variable: 'LIQUIBASE_SQL_LOG_LEVEL')

      --sql-show-sql-warnings=PARAM
                             Show SQLWarning messages
                             DEFAULT: true
                             (defaults file: 'liquibase.sql.showSqlWarnings',
                               environment variable:
                               'LIQUIBASE_SQL_SHOW_SQL_WARNINGS')

      --strict=PARAM         Be stricter on allowed Liquibase configuration and
                               setup?
                             DEFAULT: false
                             (defaults file: 'liquibase.strict', environment
                               variable: 'LIQUIBASE_STRICT')

      --support-property-escaping=PARAM
                             Support escaping changelog parameters using a
                               colon. Example: null
                             DEFAULT: false
                             (defaults file: 'liquibase.
                               supportPropertyEscaping', environment variable:
                               'LIQUIBASE_SUPPORT_PROPERTY_ESCAPING')

      --supports-method-validation-level=PARAM
                             Controls the level of validation performed on the
                               supports method of Change classes. Options are
                               OFF, WARN, FAIL.
                             DEFAULT: WARN
                             (defaults file: 'liquibase.
                               supportsMethodValidationLevel', environment
                               variable:
                               'LIQUIBASE_SUPPORTS_METHOD_VALIDATION_LEVEL')

      --ui-service=PARAM     Changes the default UI Service Logger used by
                               Liquibase. Options are CONSOLE or LOGGER.
                             DEFAULT: CONSOLE
                             (defaults file: 'liquibase.uiService', environment
                               variable: 'LIQUIBASE_UI_SERVICE')

      --use-procedure-schema=PARAM
                             If set to true (default value), createProcedure
                               tags with a set schemaName will modify the
                               procedure body with the given schema name.
                             DEFAULT: true
                             (defaults file: 'liquibase.useProcedureSchema',
                               environment variable:
                               'LIQUIBASE_USE_PROCEDURE_SCHEMA')

  -v, --version              Print version information and exit

      --validate-xml-changelog-files=PARAM
                             Will perform xsd validation of XML changelog
                               files. When many XML changelog files are
                               included this validation may impact Liquibase
                               performance. Defaults to true.
                             DEFAULT: true
                             (defaults file: 'liquibase.
                               validateXmlChangelogFiles', environment
                               variable:
                               'LIQUIBASE_VALIDATE_XML_CHANGELOG_FILES')


Commands
  calculate-checksum            Calculates and prints a checksum for the
                                  changeset

  changelog-sync                Marks all changes as executed in the database

  changelog-sync-sql            Output the raw SQL used by Liquibase when
                                  running changelogSync

  changelog-sync-to-tag         Marks all undeployed changesets as executed, up
                                  to a tag

  changelog-sync-to-tag-sql     Output the raw SQL used by Liquibase when
                                  running changelogSyncToTag

  clear-checksums               Clears all checksums

  db-doc                        Generates JavaDoc documentation for the
                                  existing database and changelogs

  diff                          Outputs a description of differences.  If you
                                  have a Liquibase Pro key, you can output the
                                  differences as JSON using the --format=JSON
                                  option

  diff-changelog                Compare two databases to produce changesets and
                                  write them to a changelog file

  drop-all                      Drop all database objects owned by the user

  execute-sql                   Execute a SQL string or file

  future-rollback-count-sql     Generates SQL to sequentially revert <count>
                                  number of changes

  future-rollback-from-tag-sql  Generates SQL to revert future changes up to
                                  the specified tag

  future-rollback-sql           Generate the raw SQL needed to rollback
                                  undeployed changes

  generate-changelog            Generate a changelog

  history                       List all deployed changesets and their
                                  deployment ID

  init                          < Init commands >

  list-locks                    List the hostname, IP address, and timestamp of
                                  the Liquibase lock record

  mark-next-changeset-ran       Marks the next change you apply as executed in
                                  your database

  mark-next-changeset-ran-sql   Writes the SQL used to mark the next change you
                                  apply as executed in your database

  release-locks                 Remove the Liquibase lock record from the
                                  DATABASECHANGELOG table

  rollback                      Rollback changes made to the database based on
                                  the specific tag

  rollback-count                Rollback the specified number of changes made
                                  to the database

  rollback-count-sql            Generate the SQL to rollback the specified
                                  number of changes

  rollback-sql                  Generate the SQL to rollback changes made to
                                  the database based on the specific tag

  rollback-to-date              Rollback changes made to the database based on
                                  the specific date

  rollback-to-date-sql          Generate the SQL to rollback changes made to
                                  the database based on the specific date

  snapshot                      Capture the current state of the database

  snapshot-reference            Capture the current state of the reference
                                  database

  status                        Generate a list of pending changesets

  tag                           Mark the current database state with the
                                  specified tag

  tag-exists                    Verify the existence of the specified tag

  unexpected-changesets         Generate a list of changesets that have been
                                  executed but are not in the current changelog

  update                        Deploy any changes in the changelog file that
                                  have not been deployed

  update-count                  Deploy the specified number of changes from the
                                  changelog file

  update-count-sql              Generate the SQL to deploy the specified number
                                  of changes

  update-sql                    Generate the SQL to deploy changes in the
                                  changelog which have not been deployed

  update-testing-rollback       Updates database, then rolls back changes
                                  before updating again. Useful for testing
                                  rollback support

  update-to-tag                 Deploy changes from the changelog file to the
                                  specified tag

  update-to-tag-sql             Generate the SQL to deploy changes up to the tag

  validate                      Validate the changelog for errors


Each argument contains the corresponding 'configuration key' in parentheses. As
an alternative to passing values on the command line, these keys can be used as
a basis for configuration settings in other locations.

Available configuration locations, in order of priority:
- Command line arguments (argument name in --help)
- Java system properties (configuration key listed above)
- Environment values (env variable listed above)
- Defaults file (configuration key OR argument name)

Full documentation is available at
https://docs.liquibase.com
"""

    @Unroll
    def "toArgNames for command arguments"() {
        expect:
        LiquibaseCommandLine.toArgNames(new CommandBuilder(["argTest"] as String[][]).argument(argName, String).build()).join(", ") == expected

        where:
        argName          | expected
        "test"           | "--test"
        "twoWords"       | "--two-words, --twoWords"
        "threeWordsHere" | "--three-words-here, --threeWordsHere"
    }

    @Unroll
    def "toArgNames for configuration arguments"() {
        expect:
        LiquibaseCommandLine.toArgNames(new ConfigurationDefinition.Builder(prefix).define(argName, String).buildTemporary()).join(", ") == expected

        where:
        prefix          | argName          | expected
        "liquibase"     | "test"           | "--test, --liquibase-test, --liquibasetest"
        "liquibase"     | "twoWords"       | "--two-words, --liquibase-two-words, --twoWords, --liquibasetwoWords"
        "liquibase"     | "threeWordsHere" | "--three-words-here, --liquibase-three-words-here, --threeWordsHere, --liquibasethreeWordsHere"
        "liquibase.pro" | "test"           | "--pro-test, --liquibase-pro-test, --protest, --liquibaseprotest"
        "other"         | "twoWords"       | "--other-two-words, --othertwoWords"
    }

    @Unroll
    def "toArgNames for configuration arguments and aliases"() {
        expect:
        LiquibaseCommandLine.toArgNames(new ConfigurationDefinition.Builder(prefix).define(argName, String).addAliasKey(alias).buildTemporary()).join(", ") == expected

        where:
        prefix      | argName | alias       | expected
        "liquibase" | "test"  | "testAlias" | "--test, --liquibase-test, --liquibasetest, --test-alias, --testAlias"
    }

    @Unroll
    def "toArgNames for command arguments and aliases"() {
        expect:
        LiquibaseCommandLine.toArgNames(new CommandBuilder([["argCommand"]] as String[][]).argument(argName, String).addAlias(alias).build()).join(", ") == expected

        where:
        prefix          | argName          | alias                 | expected
        "liquibase"     | "test"           | "testAlias"           | "--test, --test-alias, --testAlias"
    }

    @Unroll
    def "adjustLegacyArgs"() {
        expect:
        new LiquibaseCommandLine().adjustLegacyArgs(input as String[]).toArrayString() == (expected as String[]).toArrayString()

        where:
        input                                                                                                                                                                                       | expected
        ["--arg", "update", "--help"]                                                                                                                                                               | ["--arg", "update", "--help"]
        ["tag", "--help"]                                                                                                                                                                           | ["tag", "--help"]
        ["tag", "my-tag"]                                                                                                                                                                           | ["tag", "--tag", "my-tag"]
        ["rollback", "my-tag"]                                                                                                                                                                      | ["rollback", "--tag", "my-tag"]
        ["rollbackToDate", "1/2/3"]                                                                                                                                                                 | ["rollbackToDate", "--date", "1/2/3"]
        ["rollback-to-date", "1/2/3"]                                                                                                                                                               | ["rollback-to-date", "--date", "1/2/3"]
        ["rollback-to-date", "1/2/3", "3:15:21"]                                                                                                                                                    | ["rollback-to-date", "--date", "1/2/3 3:15:21"]
        ["rollback-count", "5"]                                                                                                                                                                     | ["rollback-count", "--count", "5"]
        ["future-rollback-count-sql", "5"]                                                                                                                                                          | ["future-rollback-count-sql", "--count", "5"]
        ["future-rollback-from-tag-sql", "my-tag"]                                                                                                                                                  | ["future-rollback-from-tag-sql", "--tag", "my-tag"]

        ["--log-level", "DEBUG", "--log-file", "06V21.txt", "--defaultsFile=liquibase.h2-mem.properties", "update", "--changelog-file", "postgres_lbpro_master_changelog.xml", "--labels", "setup"] | ["--log-level", "DEBUG", "--log-file", "06V21.txt", "--defaultsFile=liquibase.h2-mem.properties", "update", "--changelog-file", "postgres_lbpro_master_changelog.xml", "--labels", "setup"]
    }

    def "accepts -D subcommand arguments for changelog parameters"() {
        when:
        def subcommands = new LiquibaseCommandLine().commandLine.getSubcommands()

        then:
        subcommands["update"].commandSpec.findOption("-D") != null
        subcommands["snapshot"].commandSpec.findOption("-D") == null
    }

    @Unroll
    def "cleanExceptionMessage"() {
        expect:
        new LiquibaseCommandLine().cleanExceptionMessage(input) == expected

        where:
        input                                                                | expected
        null                                                                 | null
        ""                                                                   | ""
        "random string"                                                      | "random string"
        "Unexpected error running Liquibase: message here"                   | "message here"
        "java.lang.RuntimeException: message here"                           | "message here"
        "java.lang.ParseError: message here"                                 | "message here"
        "java.io.RuntimeException: java.lang.RuntimeException: message here" | "message here"
    }

    @Unroll
    def "handleException should show WARNING if specified"(def level, def expected) {
        when:
        BufferedLogService logService = new BufferedLogService()
        Map<String, Object> scopeValues = new HashMap<>()
        scopeValues.put(Scope.Attr.logService.name(), logService)
        Scope.child(scopeValues, new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                LiquibaseException le = null
                if (level != null) {
                    le = new LiquibaseException("Test exception", level)
                } else {
                    le = new LiquibaseException("Test exception")
                }
                new LiquibaseCommandLine().handleException(le)
            }
        })

        then:
        String logString = logService.getLogAsString(level)
        assert logString != null
        assert logString.contains(expected)

        where:
        level                                                                | expected
        null                                                                 | "SEVERE Test exception"
        Level.SEVERE                                                         | "SEVERE Test exception"
        Level.WARNING                                                        | "WARNING Test exception"
    }

    def "help output" () {
        when:
        def oldOut = System.out
        def bytes = new ByteArrayOutputStream()
        def newOut = new PrintStream(bytes)
        System.setOut(newOut)
        new LiquibaseCommandLine().execute("--help")

        then:
        StringUtil.standardizeLineEndings(bytes.toString().trim()) == StringUtil.standardizeLineEndings(expectedHelpOutput.trim())

        cleanup:
        System.setOut(oldOut)
    }

    static class TestConsoleUIService extends ConsoleUIService {
        private List<String> messages = new ArrayList<>()

        @Override
        void sendMessage(String message) {
            messages.add(message)
        }

        List<String> getMessages() {
            return messages
        }
    }
}
