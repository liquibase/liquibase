package liquibase.command

import liquibase.Scope
import spock.lang.Specification

class CommandFactoryTest extends Specification {

    def "getCommand for an existing command"() {
        when:
        def command = Scope.currentScope.getSingleton(CommandFactory).getCommandDefinition("update")

        then:
        command.name*.toString() == ["update"]
        command.pipeline*.class*.name.contains("liquibase.command.core.UpdateCommandStep")
        command.arguments.keySet().contains("changelogFile")
    }

    def "getCommand for an existing command from cache"() {
        when:
        def command1 = Scope.currentScope.getSingleton(CommandFactory).getCommandDefinition("update")
        def command2 = Scope.currentScope.getSingleton(CommandFactory).getCommandDefinition("update")

        then: "definitions are equal and carry the same metadata"
        command1 == command2
        command1.arguments.keySet() == command2.arguments.keySet()
        command1.pipeline*.class == command2.pipeline*.class
    }

    def "getCommandDefinition returns isolated pipeline step instances"() {
        // Two concurrent CommandScopes must never share CommandStep instances: steps hold execution
        // state, and a shared instance lets one thread's cleanUp tear down another thread's run (#6927)
        when:
        def factory = Scope.currentScope.getSingleton(CommandFactory)
        def command1 = factory.getCommandDefinition("update")
        def command2 = factory.getCommandDefinition("update")

        then:
        command1.pipeline.every { s1 -> !command2.pipeline.any { s2 -> s1.is(s2) } }
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
        def commands = Scope.currentScope.getSingleton(CommandFactory).getCommands(false)
        def sampleCommand = commands.iterator().next()

        then:
        commands.size() > 5
        commands*.name*.toString().contains("[update]")
        !commands*.name*.toString().contains("[internalDiff]")

        sampleCommand.name == ["calculateChecksum"]
        sampleCommand.arguments.keySet().contains("changelogFile")

    }

    def "getCommand brings all the dependencies for a given command in correct oreder"() {
        when:
        def command = Scope.currentScope.getSingleton(CommandFactory).getCommandDefinition("tag")

        then:
        command.name*.toString() == ["tag"]
        command.arguments.keySet().contains("tag")
        command.arguments.keySet().contains("database")
        command.pipeline*.class*.name == ["liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep", "liquibase.command.core.helpers.DbUrlConnectionCommandStep", "liquibase.command.core.helpers.LockServiceCommandStep", "liquibase.command.core.TagCommandStep"]

    }
}
