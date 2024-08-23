package liquibase.command

import spock.lang.Specification
import spock.lang.Unroll

class TestAbstractCliWrapperCommandStepTest extends Specification {

    @Unroll
    def "collectArguments"() {
        when:
        def commandScope = new CommandScope(commandName)
        if (passedArguments != null) {
            passedArguments.forEach({ key, value ->
                commandScope.addArgumentValue(key, value)
            })
        }


        then:
        new TestCliWrapperCommandStep().collectArguments(commandScope, legacyCommandArguments, positionalArgument).join(", ") == expected

        where:
        commandName | passedArguments                                     | legacyCommandArguments   | positionalArgument | expected
        "update"    | null                                                | null                     | null               | "--showSummary, SUMMARY, --showSummaryOutput, ALL, update"
        "update"    | ["changelogFile": "x"]                              | null                     | null               | "--changelogFile, x, --showSummary, SUMMARY, --showSummaryOutput, ALL, update"
        "update"    | ["changelogFile": "x"]                              | ["changelogFile"]        | null               | "--showSummary, SUMMARY, --showSummaryOutput, ALL, update, --changelogFile, x"
        "update"    | ["changelogFile": "x", "url": "y"]                  | null                     | null               | "--changelogFile, x, --showSummary, SUMMARY, --showSummaryOutput, ALL, --url, y, update"
        "update"    | ["changelogFile": "x", "url": "y"]                  | ["changelogFile"]        | null               | "--showSummary, SUMMARY, --showSummaryOutput, ALL, --url, y, update, --changelogFile, x"
        "update"    | ["changelogFile": "x", "url": "y"]                  | ["changelogFile", "url"] | null               | "--showSummary, SUMMARY, --showSummaryOutput, ALL, update, --changelogFile, x, --url, y"
        "update"    | ["changelogFile": "x", "url": "y"]                  | null                     | "url"              | "--changelogFile, x, --showSummary, SUMMARY, --showSummaryOutput, ALL, update, y"
        "update"    | ["changelogFile": "x", "url": "y", "password": "z"] | ["password"]             | "url"              | "--changelogFile, x, --showSummary, SUMMARY, --showSummaryOutput, ALL, update, --password, z, y"
    }

    @Unroll
    def "removeArgumentValues"() {
        expect:
        new TestCliWrapperCommandStep().removeArgumentValues(allArgs as String[], noValueArgs as String[]).join(", ") == expected

        where:
        allArgs                         | noValueArgs | expected
        []                              | []          | ""
        ["--x", "a", "--y", "b"]        | []          | "--x, a, --y, b"
        ["--x", "a", "--y", "b"]        | ["x"]       | "--x, --y, b"
        ["--x", "a", "--y", "b"]        | ["y"]       | "--x, a, --y"
        ["--x", "a", "--y", "b", "--z"] | ["z"]       | "--x, a, --y, b, --z"
    }


    private static class TestCliWrapperCommandStep extends AbstractCliWrapperCommandStep {
        @Override
        String[][] defineCommandNames() {
            return "test"
        }

        @Override
        protected String[] collectArguments(CommandScope commandScope) throws liquibase.exception.CommandExecutionException {
            return collectArguments(commandScope, null, null);
        }
    }
}
