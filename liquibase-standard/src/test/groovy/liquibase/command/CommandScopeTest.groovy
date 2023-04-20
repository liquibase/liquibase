package liquibase.command

import liquibase.Scope
import liquibase.command.core.MockCommandStep
import liquibase.configuration.ConfiguredValue
import liquibase.configuration.ConfiguredValueModifier
import liquibase.configuration.ConfiguredValueModifierFactory
import liquibase.configuration.ProvidedValue
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

        Scope.getCurrentScope().getSingleton(CommandFactory.class).unregister("mock command")
        def arg = new CommandBuilder([["mock command"]] as String[][]).argument(argumentName, String).defaultValue(defaultValue).build()

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
        argValue.getProvidedValue().getRequestedKey() == expectedRequestedKey
        argValue.getProvidedValue().getSourceDescription() == expectedSource
        argValue.getValue() == scope.getArgumentValue(arg)

        Scope.exit(scopeId)

        where:
        argumentName        | defaultValue               | passedArg              | passedValue       | expectedValue          | expectedActualKey                        | expectedSource              | expectedRequestedKey
        "arg1"              | null                       | "arg1"                 | "arg 1"           | "arg 1"                | "arg1"                                   | "Command argument"          | "liquibase.command.mock.arg1"
        "unsetArg"          | null                       | null                   | null              | null                   | "liquibase.command.mock.unsetArg"        | "No configured value found" | "liquibase.command.mock.unsetArg"
        "argWithDefault"    | "default value"            | null                   | null              | "default value"        | "liquibase.command.mock.argWithDefault"  | "Default value"             | "liquibase.command.mock.argWithDefault"
        "setArgWithDefault" | "overridden default value" | "setArgWithDefault"    | "set arg value"   | "set arg value"        | "setArgWithDefault"                      | "Command argument"          | "liquibase.command.mock.setArgWithDefault"
        "argSetFromScope"   | "default value"            | null                   | null              | "value from scope"     | "liquibase.command.mock.argSetFromScope" | "Scoped value"              | "liquibase.command.mock.argSetFromScope"
        "argSetFromScope"   | null                       | null                   | null              | "value from scope"     | "liquibase.command.mock.argSetFromScope" | "Scoped value"              | "liquibase.command.mock.argSetFromScope"
        "otherArgInScope"   | null                       | null                   | null              | "other value in scope" | "liquibase.command.otherArgInScope"      | "Scoped value"              | "liquibase.command.otherArgInScope"
        "otherArgInScope"   | "default value"            | null                   | null              | "other value in scope" | "liquibase.command.otherArgInScope"      | "Scoped value"              | "liquibase.command.otherArgInScope"
        "argSavedKabobCase" | null                       | "arg-saved-kabob-case" | "kabob value"     | "kabob value"          | "arg-saved-kabob-case"                   | "Command argument"          | "liquibase.command.mock.argSavedKabobCase"
        "argSavedUpperCase" | null                       | "ARGSAVEDUPPERCASE"    | "uppercase value" | "uppercase value"      | "ARGSAVEDUPPERCASE"                      | "Command argument"          | "liquibase.command.mock.argSavedUpperCase"
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

        new CommandBuilder([["mock"]] as String[][]).argument("requiredArg", String).required().build()
        new CommandBuilder([["mock"]] as String[][]).argument("optionalArg", String).optional().build()

        def scope = new CommandScope("mock")
        scope.setOutput(output)
        scope.execute()

        then:
        def e = thrown(CommandValidationException)
        e.message == "Invalid argument 'requiredArg': missing required argument"
    }

    def "ValueModifiers are used in getArgumentValue"() {
        when:
        def valueModifier = new ConfiguredValueModifier<String>() {
            @Override
            int getOrder() {
                return 5
            }

            @Override
            void override(ConfiguredValue<String> object) {
                object.override("MODIFIED!", "Mock Modifier")
            }
        }
        Scope.currentScope.getSingleton(ConfiguredValueModifierFactory).register(valueModifier)

        def scope = new CommandScope("mock")
        scope.addArgumentValue(MockCommandStep.VALUE_1_ARG, "Original Value")

        then:
        scope.getArgumentValue(MockCommandStep.VALUE_1_ARG) == "MODIFIED!"

        Scope.currentScope.getSingleton(ConfiguredValueModifierFactory).unregister(valueModifier)

    }
}
