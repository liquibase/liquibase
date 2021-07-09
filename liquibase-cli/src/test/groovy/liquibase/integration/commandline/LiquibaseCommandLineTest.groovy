package liquibase.integration.commandline


import liquibase.command.CommandBuilder
import liquibase.configuration.ConfigurationDefinition
import picocli.CommandLine
import spock.lang.Specification
import spock.lang.Unroll

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
    def "adjustLegacyArgs"() {
        expect:
        new LiquibaseCommandLine().adjustLegacyArgs(input as String[]).toArrayString() == (expected as String[]).toArrayString()

        where:
        input                                  | expected
        ["--arg", "update", "--help"]          | ["--arg", "update", "--help"]
        ["tag", "--help"]                      | ["tag", "--help"]
        ["tag", "my-tag"]                      | ["tag", "--tag", "my-tag"]
        ["rollback", "my-tag"]                 | ["rollback", "--tag", "my-tag"]
        ["rollbackToDate", "1/2/3"]              | ["rollbackToDate", "--date", "1/2/3"]
        ["rollback-to-date", "1/2/3"]             | ["rollback-to-date", "--date", "1/2/3"]
        ["rollback-to-date", "1/2/3", "3:15:21"]             | ["rollback-to-date", "--date", "1/2/3 3:15:21"]
        ["rollback-count", "5"]                | ["rollback-count", "--count", "5"]
        ["future-rollback-count-sql", "5"]         | ["future-rollback-count-sql", "--count", "5"]
        ["future-rollback-from-tag-sql", "my-tag"] | ["future-rollback-from-tag-sql", "--tag", "my-tag"]

        ["--log-level","DEBUG","--log-file","06V21.txt","--defaultsFile=liquibase.h2-mem.properties","update","--changelog-file","postgres_lbpro_master_changelog.xml","--labels","setup"] | ["--log-level","DEBUG","--log-file","06V21.txt","--defaultsFile=liquibase.h2-mem.properties","update","--changelog-file","postgres_lbpro_master_changelog.xml","--labels","setup"]
    }

    def "accepts -D subcommand arguments for changelog parameters"() {
        when:
        def subcommands = new LiquibaseCommandLine().commandLine.getSubcommands()

        then:
        subcommands["update"].commandSpec.findOption("-D") != null
        subcommands["snapshot"].commandSpec.findOption("-D") == null
    }
}
