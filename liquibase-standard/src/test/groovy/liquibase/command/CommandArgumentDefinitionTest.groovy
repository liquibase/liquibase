package liquibase.command

import liquibase.Scope
import liquibase.command.core.MockCommandStep
import liquibase.exception.CommandValidationException
import liquibase.util.StringUtil
import spock.lang.Specification

class CommandArgumentDefinitionTest extends Specification {

    def setup() {
        Scope.currentScope.getSingleton(CommandFactory).unregister("mock")
    }

    def validate() {
        when:
        def definition = new CommandArgumentDefinition("testArg", String)
        def commandScope = new CommandScope("mock")
        definition.validate(commandScope)

        then:
        notThrown(CommandValidationException)

        when:
        definition.required = true
        definition.validate(commandScope)
        then:
        def e = thrown(CommandValidationException)
        e.message == "Invalid argument 'testArg': missing required argument"

        when: //there is a default value but value is not set
        definition.defaultValue = "default value"
        definition.validate(commandScope)
        then:
        notThrown(CommandValidationException)

        when:
        definition.defaultValue = null
        commandScope.addArgumentValue(definition, "passed value")
        definition.validate(commandScope)
        then:
        notThrown(CommandValidationException)

        when:// by default hidden attribute is false
        definition.validate(commandScope)
        then:
        definition.hidden == false

        when:// when hidden attribute is set to true, no exception is thrown
        definition.hidden = true
        definition.validate(commandScope)
        then:
        notThrown(CommandValidationException)
    }

    def "toString test"() {
        when:
        def definition = new CommandArgumentDefinition("testArg", String)
        then:
        definition.toString() == "testArg"

        when:
        definition.required = true
        then:
        definition.toString() == "testArg (required)"
    }

    def "test builder"() {
        setup:
        MockCommandStep.reset()
        def builder = new CommandBuilder([["mock"]] as String[][])

        when:
        def arg1 = builder.argument("arg1", String).build()
        then:
        arg1.name == "arg1"
        !arg1.required
        arg1.defaultValue == null

        when:
        def arg2 = builder.argument("arg2", String).required().defaultValue("default value").description("This is arg2").build()
        then:
        arg2.name == "arg2"
        arg2.required
        arg2.defaultValue == "default value"

        when:
        builder.argument("kabob-case", String).build()
        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Invalid argument format: kabob-case"

        then:
        StringUtil.join(Scope.currentScope.getSingleton(CommandFactory).getCommandDefinition("mock").getArguments(), ", ") == "arg1=arg1, arg2=arg2 (required)"
    }
}
