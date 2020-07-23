package liquibase.command.core

import liquibase.Scope
import liquibase.changelog.DatabaseChangeLog
import liquibase.configuration.HubConfiguration
import liquibase.configuration.LiquibaseConfiguration
import liquibase.hub.HubService
import liquibase.hub.core.MockHubService
import spock.lang.Specification

class RegisterChangeLogCommandTest extends Specification {

    private String scopeId

    def setup() {
        scopeId = Scope.enter([("liquibase.plugin." + HubService.name): MockHubService])
    }

    def "cleanup"() {
        Scope.exit(scopeId)
    }

    def "happyPath"() {
        when:
        def outputStream = new ByteArrayOutputStream()

        def hubConfiguration = LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class)
        hubConfiguration.setLiquibaseHubProject("PROJECT 1")
        def command = new RegisterChangeLogCommand()
        command.setOutputStream(new PrintStream(outputStream))

        def result = command.run()

        def hubChangeLog = command.getHubChangeLog()

        then:
        result.succeeded
        hubChangeLog.id != null
        hubChangeLog.id.toString() == "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        hubChangeLog.externalChangeLogId.toString() == "3fa85f64-5717-4562-b3fc-2c963f66afa6"
        hubChangeLog.fileName == "string"
        hubChangeLog.name == "changelog"
    }

    def "changeLogAlreadyRegistered"() {
        when:
        def outputStream = new ByteArrayOutputStream()

        def hubConfiguration = LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class)
        hubConfiguration.setLiquibaseHubProject("PROJECT 1")
        def command = new RegisterChangeLogCommand()
        command.setChangeLogFile("changelog.xml")
        DatabaseChangeLog changeLog = new DatabaseChangeLog(".")
        def uuid = UUID.randomUUID().toString()
        changeLog.setChangeLogId(uuid)
        Map<String, Object> argsMap = new HashMap<>()
        argsMap.put("changeLog", changeLog)
        command.configure(argsMap)
        command.setOutputStream(new PrintStream(outputStream))

        def result = command.run()

        then:
        ! result.succeeded
        result.message.contains("is already registered with changeLogId=" + uuid.toString())
    }
}
