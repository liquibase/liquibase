package liquibase.command.core

import liquibase.Scope
import liquibase.hub.HubServiceFactory
import spock.lang.Specification

class RegisterChangeLogCommandTest extends Specification {

    setup() {
        Scope.child()
        Scope.currentScope.getSingleton(HubServiceFactory.class)
    }

    def run() {
        when:
        def outputStream = new ByteArrayOutputStream()

        def command = new RegisterChangeLogCommand()
        command.setOutputStream(new PrintStream(outputStream))

        def result = command.run()

        def output = outputStream.toString()

        then:
        result.succeeded
        output == "x"
    }
}
