package liquibase.command.core

import liquibase.ContextExpression
import liquibase.Labels
import liquibase.Scope
import liquibase.change.CheckSum
import liquibase.changelog.*
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

    private UUID randomUUID = UUID.randomUUID()
    private MockChangeLogHistoryService changeLogHistoryService

    def setup() {
        setupScopeId = Scope.enter([
                ("liquibase.plugin." + HubService.name): MockHubService,
                (Scope.Attr.database.name())           : new MockDatabase(),
        ])

        mockHubService = (MockHubService) Scope.currentScope.getSingleton(HubServiceFactory).getService()
        ChangeLogParserFactory.getInstance().register(new MockChangeLogParser(changeLogs: [
                "com/example/unregistered.mock": new DatabaseChangeLog(),
                "com/example/registered.mock"  : new DatabaseChangeLog(changeLogId: randomUUID.toString())
        ]))

        changeLogHistoryService = (MockChangeLogHistoryService) ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(Scope.currentScope.getDatabase())
        (changeLogHistoryService).ranChangeSets = [
                new RanChangeSet("test/changelog.xml", "1", "mock-author", CheckSum.parse("1:a"), new Date(), null, ChangeSet.ExecType.EXECUTED, "desc here", "comments here", new ContextExpression(), new Labels(), "deployment id"),
                new RanChangeSet("test/changelog.xml", "2", "mock-author", CheckSum.parse("1:a"), new Date(), null, ChangeSet.ExecType.EXECUTED, "desc here", "comments here", new ContextExpression(), new Labels(), "deployment id"),
                new RanChangeSet("test/changelog.xml", "3", "mock-author", CheckSum.parse("1:a"), new Date(), null, ChangeSet.ExecType.EXECUTED, "desc here", "comments here", new ContextExpression(), new Labels(), "deployment id"),

        ]
    }

    def cleanup() {
        mockHubService.reset()
        Scope.exit(setupScopeId)
        ChangeLogParserFactory.reset()
        changeLogHistoryService.reset()
    }

    def "Sync is successful with url passed"() {
        setup:
        mockHubService.environments = [
                new Environment(
                        id: randomUUID,
                        url: "jdbc://test",
                )
        ]

        when:
        def command = new SyncHubCommand()
        command.url = "jdbc://test"

        def result = command.run()

        then:
        assert result.succeeded: result.message
        assert mockHubService.sentObjects.toString() == "[setRanChangeSets/$randomUUID:[test/changelog.xml::1::mock-author, test/changelog.xml::2::mock-author, test/changelog.xml::3::mock-author]]"
    }

    def "Sync is successful with environmentId passed"() {
        setup:
        mockHubService.environments = [
                new Environment(
                        id: randomUUID,
                        url: "jdbc://test",
                ),
                new Environment(
                        id: UUID.randomUUID(),
                        url: "jdbc://test",
                )
        ]

        when:
        def command = new SyncHubCommand()
        command.url = "jdbc://test"
        command.hubEnvironmentId = randomUUID.toString()

        def result = command.run()

        then:
        assert result.succeeded: result.message
        assert mockHubService.sentObjects.toString() == "[setRanChangeSets/$randomUUID:[test/changelog.xml::1::mock-author, test/changelog.xml::2::mock-author, test/changelog.xml::3::mock-author]]"
    }

    def "Will auto-create environments if changeLogFile is passed"() {
        when:
        mockHubService.changeLogs = [
                new HubChangeLog(
                        id: randomUUID,
                        name: "Mock changelog",
                        project: new Project(
                                id: randomUUID,
                                name: "Mock Project",
                        )
                )
        ]
        def command = new SyncHubCommand()
        command.url = "jdbc://test2"
        command.changeLogFile = "com/example/registered.mock"

        def result = command.run()

        then:
        assert result.succeeded: result.message
        assert mockHubService.sentObjects["createEnvironment/$randomUUID" as String].toString() == ("[Environment jdbc://test2 (null)]")

    }

    def "Fails with invalid hubEnvironmentId"() {
        setup:
        mockHubService.environments = []

        when:
        def command = new SyncHubCommand()
        command.url = "jdbc://test2"
        command.hubEnvironmentId = randomUUID

        def result = command.run()

        then:
        assert !result.succeeded
        assert result.message == "Unknown hubEnvironmentId " + command.hubEnvironmentId
    }

    def "Fails with multiple matching environments"() {
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

    def "Fails with no environment and with no changeLogFile passed"() {
        when:
        def command = new SyncHubCommand()
        command.url = "jdbc://test2"

        def result = command.run()

        then:
        assert !result.succeeded
        assert result.message == "The url " + command.url + " does not match any defined environments. To auto-create an environment, please specify 'changeLogFile=<changeLogFileName>' in liquibase.properties or the command line."

    }

    def "Fails with no environment and unregistered changeLogFile passed"() {
        when:
        def command = new SyncHubCommand()
        command.url = "jdbc://test2"
        command.changeLogFile = "com/example/unregistered.mock"

        def result = command.run()

        then:
        assert !result.succeeded
        assert result.message == "Changelog com/example/unregistered.mock has not been registered with Liquibase Hub."

    }

    def "Fails with unknown changeLogId"() {
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
