package liquibase.command.core

import liquibase.Scope
import liquibase.changelog.DatabaseChangeLog
import liquibase.database.core.MockDatabase
import liquibase.hub.HubService
import liquibase.hub.HubServiceFactory
import liquibase.hub.core.MockHubService
import liquibase.hub.model.Environment
import liquibase.parser.ChangeLogParserFactory
import liquibase.parser.MockChangeLogParser
import spock.lang.Specification

class SyncHubCommandTest extends Specification {

    private String setupScopeId
    private MockHubService mockHubService

    def setup() {
        setupScopeId = Scope.enter([
                ("liquibase.plugin." + HubService.name): MockHubService,
                (Scope.Attr.database.name())           : new MockDatabase(),
        ])

        mockHubService = (MockHubService) Scope.currentScope.getSingleton(HubServiceFactory).getService()
        ChangeLogParserFactory.getInstance().register(new MockChangeLogParser(changeLogs: [
                "com/example/unregistered.mock": new DatabaseChangeLog(),
                "com/example/registered.mock": new DatabaseChangeLog(changeLogId: UUID.randomUUID().toString())
        ]))
    }

    def "cleanup"() {
        Scope.exit(setupScopeId)
        ChangeLogParserFactory.reset()
    }


    def "multiple matching environments"() {
        setup:
        mockHubService.environments = [
                new Environment(url: "jdbc://test1"),
                new Environment(url: "jdbc://test2"),
        ]

        when:
        def command = new SyncHubCommand()
        command.url = "jdbc://test2"

        def result = command.run()

        then:
        assert !result.succeeded
        assert result.message == "The url " + command.url + " is used by more than one environment. Please specify 'hubEnvironmentId=<hubEnvironmentId>' or 'changeLogFile=<changeLogFileName>' in liquibase.properties or the command line."
    }

    def "matches no environment, with no changeLogFile passed"() {
        when:
        def command = new SyncHubCommand()
        command.url = "jdbc://test2"

        def result = command.run()

        then:
        assert !result.succeeded
        assert result.message == "The url " + command.url + " does not match any defined environments. To auto-create an environment, please specify 'changeLogFile=<changeLogFileName>' in liquibase.properties or the command line."

    }

    def "matches no environment, with unregistered changeLogFile passed"() {
        when:
        def command = new SyncHubCommand()
        command.url = "jdbc://test2"
        command.changeLogFile = "com/example/unregistered.mock"

        def result = command.run()

        then:
        assert !result.succeeded
        assert result.message == "Changelog com/example/unregistered.mock has not been registered with Liquibase Hub."

    }

    def "matches no environment, with unknown changeLogId"() {
        when:
        def command = new SyncHubCommand()
        command.url = "jdbc://test2"
        command.changeLogFile = "com/example/registered.mock"

        def result = command.run()

        then:
        assert !result.succeeded
        assert result.message == "Changelog com/example/registered.mock has not been registered with Liquibase Hub."

    }

    def "matches no environment, can auto-create one"() {
        when:
        mockHubService.environments = []
        def command = new SyncHubCommand()
        command.url = "jdbc://test2"
        command.changeLogFile = "com/example/registered.mock"

        def result = command.run()

        then:
        assert !result.succeeded
        assert result.message == "Changelog com/example/unregistered.mock has not been registered with Liquibase Hub."

    }
}
