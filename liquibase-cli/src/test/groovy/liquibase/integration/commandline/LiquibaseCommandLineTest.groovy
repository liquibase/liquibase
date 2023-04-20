package liquibase.integration.commandline

import liquibase.Scope
import liquibase.command.CommandBuilder
import liquibase.configuration.ConfigurationDefinition
import liquibase.exception.LiquibaseException
import liquibase.logging.core.BufferedLogService
import spock.lang.Specification
import spock.lang.Unroll

import java.util.logging.Level

class LiquibaseCommandLineTest extends Specification {

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
        "liquibase"     | "test"           | "testAlias"           | "--test-alias, --testAlias, --test"
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
}
