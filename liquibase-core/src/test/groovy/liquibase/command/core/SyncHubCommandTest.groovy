package liquibase.command.core


import liquibase.Scope
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.changelog.DatabaseChangeLog
import liquibase.changelog.MockChangeLogHistoryService
import liquibase.database.core.MockDatabase
import liquibase.hub.HubService
import liquibase.hub.HubServiceFactory
import liquibase.hub.core.MockHubService
import liquibase.hub.model.Environment
import liquibase.hub.model.HubChangeLog
import liquibase.hub.model.Project
import liquibase.parser.ChangeLogParserFactory
import liquibase.parser.MockChangeLogParser
import spock.lang.Specification

class SyncHubCommandTest extends Specification {

    private String setupScopeId
    private MockHubService mockHubService

    private MockChangeLogHistoryService changeLogHistoryService

    def setup() {
        setupScopeId = Scope.enter([
                ("liquibase.plugin." + HubService.name): MockHubService,
                (Scope.Attr.database.name())           : new MockDatabase(),
        ])

        mockHubService = (MockHubService) Scope.currentScope.getSingleton(HubServiceFactory).getService()
        ChangeLogParserFactory.getInstance().register(new MockChangeLogParser(changeLogs: [
                "com/example/unregistered.mock": new DatabaseChangeLog(),
                "com/example/registered.mock"  : new DatabaseChangeLog(changeLogId: MockHubService.randomUUID.toString())
        ]))

        changeLogHistoryService = (MockChangeLogHistoryService) ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(Scope.currentScope.getDatabase())
    }

    def cleanup() {
        mockHubService.reset()
        Scope.exit(setupScopeId)
        ChangeLogParserFactory.reset()
        changeLogHistoryService.reset()
    }

    def "Sync is successful with url passed"() {
        when:
        def command = new SyncHubCommand()
        command.url = "jdbc://test"
        command.database = new MockDatabase()

        def result = command.run()

        then:
        assert result.succeeded: result.message
        assert mockHubService.sentObjects.toString() == "[setRanChangeSets/Environment jdbc://test ($MockHubService.randomUUID):[test/changelog.xml::1::mock-author, test/changelog.xml::2::mock-author, test/changelog.xml::3::mock-author]]"
    }

    def "Sync is successful with environmentId passed"() {
        setup:
        def randomUUID = UUID.randomUUID()
        def otherUUID = UUID.randomUUID()
        mockHubService.returnEnvironments = [
                new Environment(
                        id: randomUUID,
                        jdbcUrl: "jdbc://test",
                ),
                new Environment(
                        id: otherUUID,
                        jdbcUrl: "jdbc://test",
                )
        ]

        when:
        def command = new SyncHubCommand()
        command.url = "jdbc://test"
        command.hubEnvironmentId = randomUUID
        command.database = new MockDatabase()

        def result = command.run()

        then:
        assert result.succeeded: result.message
        assert mockHubService.sentObjects.toString() == "[setRanChangeSets/Environment jdbc://test ($randomUUID):[test/changelog.xml::1::mock-author, test/changelog.xml::2::mock-author, test/changelog.xml::3::mock-author]]"
    }

    def "Will auto-create environments if changeLogFile is passed"() {
        mockHubService.returnEnvironments = []

        when:
        mockHubService.returnChangeLogs = [
                new HubChangeLog(
                        id: MockHubService.randomUUID,
                        name: "Mock changelog",
                        project: new Project(
                                id: MockHubService.randomUUID,
                                name: "Mock Project",
                        )
                )
        ]
        def command = new SyncHubCommand()
        command.url = "jdbc://test2"
        command.database = new MockDatabase()
        command.changeLogFile = "com/example/registered.mock"

        def result = command.run()

        then:
        assert result.succeeded: result.message
        assert mockHubService.sentObjects["createEnvironment/$MockHubService.randomUUID" as String].toString() == ("[Environment jdbc://test2 (null)]")

    }

    def "Fails with invalid hubEnvironmentId"() {
        setup:
        mockHubService.returnEnvironments = []

        when:
        def command = new SyncHubCommand()
        command.url = "jdbc://test2"
        command.hubEnvironmentId = MockHubService.randomUUID

        def result = command.run()

        then:
        assert !result.succeeded
        assert result.message == "Unknown hubEnvironmentId " + command.hubEnvironmentId
    }

    def "Fails with multiple matching environments"() {
        setup:
        mockHubService.returnEnvironments = [
                new Environment(jdbcUrl: "jdbc://test1"),
                new Environment(jdbcUrl: "jdbc://test2"),
        ]

        when:
        def command = new SyncHubCommand()
        command.url = "jdbc://test2"

        def result = command.run()

        then:
        assert !result.succeeded
        assert result.message == "The url " + command.url + " is used by more than one environment. Please specify 'hubEnvironmentId=<hubEnvironmentId>' or 'changeLogFile=<changeLogFileName>' in liquibase.properties or the command line."
    }

    def "Fails with no environment and with no changeLogFile passed"() {
        setup:
        mockHubService.returnEnvironments = []

        when:
        def command = new SyncHubCommand()
        command.url = "jdbc://test2"

        def result = command.run()

        then:
        assert !result.succeeded
        assert result.message == "The url " + command.url + " does not match any defined environments. To auto-create an environment, please specify a 'changeLogFile=<changeLogFileName>' in liquibase.properties or the command line which contains a registered changeLogId."

    }

    def "Fails with no environment and unregistered changeLogFile passed"() {
        setup:
        mockHubService.returnEnvironments = []

        when:
        def command = new SyncHubCommand()
        command.url = "jdbc://test2"
        command.changeLogFile = "com/example/unregistered.mock"

        def result = command.run()

        then:
        assert !result.succeeded
        assert result.message == "The url jdbc://test2 does not match any defined environments. To auto-create an environment, please specify a 'changeLogFile=<changeLogFileName>' in liquibase.properties or the command line which contains a registered changeLogId."

    }

    def "Fails with unknown changeLogId"() {
        setup:
        mockHubService.returnChangeLogs = []
        mockHubService.returnEnvironments = []

        when:
        def command = new SyncHubCommand()
        command.url = "jdbc://test2"
        command.changeLogFile = "com/example/registered.mock"

        def result = command.run()

        then:
        assert !result.succeeded
        assert result.message == "Changelog com/example/registered.mock has an unrecognized changeLogId."

    }


}
