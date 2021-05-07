package liquibase.integration.commandline


import liquibase.command.CommandBuilder
import liquibase.configuration.ConfigurationDefinition
import spock.lang.Specification
import spock.lang.Unroll

class LiquibaseCommandLineTest extends Specification {

    @Unroll
    def "toArgNames for command arguments"() {
        expect:
        LiquibaseCommandLine.toArgNames(new CommandBuilder("argTest").argument(argName, String).build()).join(", ") == expected

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
        prefix      | argName          | expected
        "liquibase" | "test"           | "--test, --liquibase-test"
        "liquibase" | "test"           | "--test, --liquibase-test"
        "liquibase" | "twoWords"       | "--two-words, --liquibase-two-words, --twoWords"
        "liquibase" | "threeWordsHere" | "--three-words-here, --liquibase-three-words-here, --threeWordsHere"
        "other"     | "twoWords"       | "--other-two-words, --othertwoWords"
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
        ["rollbackDate", "1/2/3"]              | ["rollbackDate", "--date", "1/2/3"]
        ["rollback-date", "1/2/3"]             | ["rollback-date", "--date", "1/2/3"]
        ["rollback-count", "5"]                | ["rollback-count", "--count", "5"]
        ["future-rollback-count", "5"]         | ["future-rollback-count", "--count", "5"]
        ["future-rollback-from-tag", "my-tag"] | ["future-rollback-from-tag", "--tag", "my-tag"]

        ["--url", "jdbc:url", "update"]        | ["update", "--url", "jdbc:url"]
        ["--url=jdbc:url", "update"]           | ["update", "--url=jdbc:url"]
        ["--logLevel", "debug", "update"]      | ["--logLevel", "debug", "update"]
        ["update", "--logLevel", "debug"]      | ["--logLevel", "debug", "update"]
        ["--url", "jdbc:url", "--REFERENCE-URL=myurl", "update", "--logLevel", "debug", "--LOG-FILE=x"]      | ["--logLevel", "debug", "--LOG-FILE=x", "update", "--url", "jdbc:url", "--REFERENCE-URL=myurl"]
    }
}
