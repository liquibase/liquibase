package liquibase.command.core

import liquibase.Scope
import liquibase.hub.HubService
import liquibase.hub.core.MockHubService
import liquibase.util.StringUtil
import spock.lang.Specification

class RegisterChangeLogCommandTest extends Specification {

    private String scopeId

    def setup() {
        scopeId = Scope.enter([("liquibase.plugin." + HubService.name): MockHubService])
    }

    def "cleanup"() {
        Scope.exit(scopeId)
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
        StringUtil.standardizeLineEndings(output).trim() == StringUtil.standardizeLineEndings("""
See project Project 1
See project Project 2
""").trim()
    }
}
