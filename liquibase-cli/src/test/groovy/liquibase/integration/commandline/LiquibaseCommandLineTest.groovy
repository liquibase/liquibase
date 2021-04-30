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
        "other" | "twoWords"       | "--other-two-words, --othertwoWords"
    }
}
