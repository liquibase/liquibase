package liquibase.command

import liquibase.Scope
import liquibase.command.core.MockCommandStep
import liquibase.exception.CommandValidationException
import spock.lang.Specification
import spock.lang.Unroll

class CommandScopeTest extends Specification {

    def setup() {
        MockCommandStep.reset()
    }

    @Unroll
    def "can get and set argument values"() {
        when:
        def scope = new CommandScope("mock")

        def arg = new CommandBuilder("mock command").argument(argumentName, String).defaultValue(defaultValue).build()

        def scopeId = Scope.enter([
                "liquibase.command.mock.argSetFromScope": "value from scope",
                "liquibase.command.argSetFromScope"     : "value from scope directly under command",
                "liquibase.command.otherArgInScope"     : "other value in scope",
        ])

        if (passedArg != null) {
            scope.addArgumentValue(passedArg, passedValue)
        }

        def argValue = scope.getConfiguredValue(arg)

        then:
        argValue.value == expectedValue
        argValue.getProvidedValue().getActualKey() == expectedActualKey
        argValue.getProvidedValue().getRequestedKey() == "liquibase.command.mock." + argumentName
        argValue.getProvidedValue().getSourceDescription() == expectedSource
        argValue.getValue() == scope.getArgumentValue(arg)

        Scope.exit(scopeId)

        where:
        argumentName        | defaultValue               | passedArg              | passedValue       | expectedValue          | expectedActualKey                        | expectedSource
        "arg1"              | null                       | "arg1"                 | "arg 1"           | "arg 1"                | "arg1"                                   | "Command argument"
        "unsetArg"          | null                       | null                   | null              | null                   | "liquibase.command.mock.unsetArg"        | "No configuration or default value found"
        "argWithDefault"    | "default value"            | null                   | null              | "default value"        | "liquibase.command.mock.argWithDefault"  | "Default value"
        "setArgWithDefault" | "overridden default value" | "setArgWithDefault"    | "set arg value"   | "set arg value"        | "setArgWithDefault"                      | "Command argument"
        "argSetFromScope"   | null                       | null                   | null              | "value from scope"     | "liquibase.command.mock.argSetFromScope" | "Scoped value"
        "otherArgInScope"   | null                       | null                   | null              | "other value in scope" | "liquibase.command.otherArgInScope"      | "Scoped value"
        "argSavedKabobCase" | null                       | "arg-saved-kabob-case" | "kabob value"     | "kabob value"          | "arg-saved-kabob-case"                   | "Command argument"
        "argSavedUpperCase" | null                       | "ARGSAVEDUPPERCASE"    | "uppercase value" | "uppercase value"      | "ARGSAVEDUPPERCASE"                      | "Command argument"
    }

    def "constructor fails for unknown commands"() {
        when:
        new CommandScope("invalid", "command")

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Unknown command 'invalid command'"
    }

    def "execute"() {
        when:
        def output = new ByteArrayOutputStream()
        def scope = new CommandScope("mock")
        scope.setOutput(output)

        MockCommandStep.logic = new MockCommandStep() {
            @Override
            void run(CommandResultsBuilder resultsBuilder) throws Exception {
                resultsBuilder.getOutputStream().write("Sent output".getBytes());
                resultsBuilder.addResult("result1", "result #1")
                resultsBuilder.addResult("result2", "result #2")
            }
        }

        def results = scope.execute()

        then:
        output.toString() == "Sent output"
        results.getResults() == [
                result1: "result #1",
                result2: "result #2"
        ]
    }

    def "execute with failing argument validation"() {
        when:
        def output = new ByteArrayOutputStream()

        new CommandBuilder("mock").argument("requiredArg", String).required().build()
        new CommandBuilder("mock").argument("optionalArg", String).optional().build()

        def scope = new CommandScope("mock")
        scope.setOutput(output)
        scope.execute()

        then:
        def e = thrown(CommandValidationException)
        e.message == "Invalid argument 'requiredArg': missing required argument"
    }
}
