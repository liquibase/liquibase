package liquibase.command

import liquibase.Scope
import spock.lang.Specification

class CommandFactoryTest extends Specification {

    def "getCommand for an existing command"() {
        when:
        def command = Scope.currentScope.getSingleton(CommandFactory).getCommandDefinition("update")

        then:
        command.name*.toString() == ["update"]
        command.pipeline*.class*.name == ["liquibase.command.core.UpdateCommandStep"]
        command.arguments.keySet().contains("changeLogFile")
    }

    def "getCommand for an invalid command"() {
        when:
        Scope.currentScope.getSingleton(CommandFactory).getCommandDefinition("invalid")

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Unknown command 'invalid'"
    }

    def "getCommands"() {
        when:
        def commands = Scope.currentScope.getSingleton(CommandFactory).getCommands()
        def sampleCommand = commands.iterator().next()

        then:
        commands.size() > 5
        commands*.name*.toString().contains("[update]")

        sampleCommand.name == ["calculateCheckSum"]
        sampleCommand.arguments.keySet().contains("changeLogFile")

    }
}
