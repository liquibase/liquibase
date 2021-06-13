package liquibase.command.core


import spock.lang.Specification

class SyncHubCommandTest extends Specification {

    def "todo: move to integration tests"() {
        expect:
        true
    }

//    private String setupScopeId
//    private MockHubService mockHubService
//
//    private MockChangeLogHistoryService changeLogHistoryService
//
//    def setup() {
//        setupScopeId = Scope.enter([
//                ("liquibase.plugin." + HubService.name): MockHubService,
//                (Scope.Attr.database.name())           : new MockDatabase(),
//        ])
//
//        mockHubService = (MockHubService) Scope.currentScope.getSingleton(HubServiceFactory).getService()
//        mockHubService.reset()
//        ChangeLogParserFactory.getInstance().register(new MockChangeLogParser(changeLogs: [
//                "com/example/unregistered.mock": new DatabaseChangeLog(),
//                "com/example/registered.mock"  : new DatabaseChangeLog(changeLogId: MockHubService.randomUUID.toString())
//        ]))
//
//        changeLogHistoryService = (MockChangeLogHistoryService) ChangeLogHistoryServiceFactory.getInstance().getChangeLogService(Scope.currentScope.getDatabase())
//    }
//
//    def cleanup() {
//        mockHubService.reset()
//        Scope.exit(setupScopeId)
//        ChangeLogParserFactory.reset()
//        changeLogHistoryService.reset()
//    }
//
//    def "Sync is successful with url passed"() {
//        when:
//        def command = new SyncHubCommandStep()
//        command.url = "jdbc://test"
//        command.database = new MockDatabase()
//
//        command.run(resultsBuilder)
//
//        then:
//        notThrown(CommandExecutionException)
//        assert mockHubService.sentObjects.toString() == "[setRanChangeSets/Connection jdbc://test ($MockHubService.randomUUID):[test/changelog.xml::1::mock-author, test/changelog.xml::2::mock-author, test/changelog.xml::3::mock-author]]"
//    }
//
//    def "Sync is successful with connectionId passed"() {
//        setup:
//        def randomUUID = UUID.randomUUID()
//        def otherUUID = UUID.randomUUID()
//        mockHubService.returnConnections = [
//                new Connection(
//                        id: randomUUID,
//                        jdbcUrl: "jdbc://test",
//                ),
//                new Connection(
//                        id: otherUUID,
//                        jdbcUrl: "jdbc://test",
//                )
//        ]
//
//        when:
//        def command = new SyncHubCommandStep()
//        command.url = "jdbc://test"
//        command.hubConnectionId = randomUUID
//        command.database = new MockDatabase()
//
//        command.run(resultsBuilder)
//
//        then:
//        notThrown(CommandExecutionException)
//        assert mockHubService.sentObjects.toString() == "[setRanChangeSets/Connection jdbc://test ($randomUUID):[test/changelog.xml::1::mock-author, test/changelog.xml::2::mock-author, test/changelog.xml::3::mock-author]]"
//    }
//
//    def "Sync is successful with projectId passed"() {
//        setup:
//        mockHubService.reset()
//        def randomUUID = UUID.randomUUID()
//        def otherUUID = UUID.randomUUID()
//        mockHubService.returnConnections = [
//            new Connection(
//                id: MockHubService.randomUUID,
//                jdbcUrl: "jdbc://test",
//            )
//        ]
//
//        when:
//        def command = new SyncHubCommandStep()
//        command.url = "jdbc://test"
//        command.hubProjectId = MockHubService.randomUUID
//        command.database = new MockDatabase()
//
//        command.run(resultsBuilder)
//
//        then:
//        notThrown(CommandExecutionException)
//        assert mockHubService.sentObjects.toString() ==
//            "[setRanChangeSets/Connection jdbc://test ($MockHubService.randomUUID):[test/changelog.xml::1::mock-author, test/changelog.xml::2::mock-author, test/changelog.xml::3::mock-author]]"
//    }
//
//    def "Will auto-create connections if changeLogFile is passed"() {
//        mockHubService.returnConnections = []
//
//        when:
//        mockHubService.returnChangeLogs = [
//                new HubChangeLog(
//                        id: MockHubService.randomUUID,
//                        name: "Mock changelog",
//                        project: new Project(
//                                id: MockHubService.randomUUID,
//                                name: "Mock Project",
//                        )
//                )
//        ]
//        def command = new SyncHubCommandStep()
//        command.url = "jdbc://test2"
//        command.database = new MockDatabase()
//        command.changeLogFile = "com/example/registered.mock"
//
//        command.run(resultsBuilder)
//
//        then:
//        notThrown(CommandExecutionException)
//        assert mockHubService.sentObjects["createConnection/$MockHubService.randomUUID" as String].toString() == ("[Connection jdbc://test2 (null)]")
//
//    }
//
//    def "Fails with both hubConnectionId and projectId"() {
//        setup:
//        mockHubService.returnConnections = []
//
//        when:
//        def command = new SyncHubCommandStep()
//        command.url = "jdbc://test2"
//        command.hubConnectionId = MockHubService.randomUUID
//        command.hubProjectId = MockHubService.randomUUID
//
//        command.run(resultsBuilder)
//
//        then:
//        def e = thrown(CommandExecutionException)
//        assert e.message == "The syncHub command requires only one valid hubConnectionId or hubProjectId or unique URL. Please remove extra values."
//    }
//
//    def "Fails with invalid hubConnectionId"() {
//        setup:
//        mockHubService.returnConnections = []
//
//        when:
//        def command = new SyncHubCommandStep()
//        command.url = "jdbc://test2"
//        command.hubConnectionId = MockHubService.randomUUID
//
//        command.run(resultsBuilder)
//
//        then:
//        def e = thrown(CommandExecutionException)
//        assert e.message == "Hub connection Id " + command.hubConnectionId + " was either not found, or you do not have access"
//    }
//
//    def "Fails with multiple matching connections"() {
//        setup:
//        mockHubService.returnConnections = [
//                new Connection(jdbcUrl: "jdbc://test1"),
//                new Connection(jdbcUrl: "jdbc://test2"),
//        ]
//
//        when:
//        def command = new SyncHubCommandStep()
//        command.url = "jdbc://test2"
//
//        command.run(resultsBuilder)
//
//        then:
//        def e = thrown(CommandExecutionException)
//        assert e.message == "The url " + command.url + " is used by more than one connection. Please specify 'hubConnectionId=<hubConnectionId>' or 'changeLogFile=<changeLogFileName>' in liquibase.properties or the command line."
//    }
//
//    def "Fails with no connections and with no changeLogFile passed"() {
//        setup:
//        mockHubService.returnConnections = []
//
//        when:
//        def command = new SyncHubCommandStep()
//        command.url = "jdbc://test2"
//
//        command.run(resultsBuilder)
//
//        then:
//        def e = thrown(CommandExecutionException)
//        assert e.message == "The url " + command.url + " does not match any defined connections. To auto-create a connection, please specify a 'changeLogFile=<changeLogFileName>' in liquibase.properties or the command line which contains a registered changeLogId."
//
//    }
//
//    def "Fails with no connection and unregistered changeLogFile passed"() {
//        setup:
//        mockHubService.returnConnections = []
//
//        when:
//        def command = new SyncHubCommandStep()
//        command.url = "jdbc://test2"
//        command.changeLogFile = "com/example/unregistered.mock"
//
//        command.run(resultsBuilder)
//
//        then:
//        def e = thrown(CommandExecutionException)
//        assert e.message == "The url jdbc://test2 does not match any defined connections. To auto-create a connection, please specify a 'changeLogFile=<changeLogFileName>' in liquibase.properties or the command line which contains a registered changeLogId."
//
//    }
//
//    def "Fails with unknown changeLogId"() {
//        setup:
//        mockHubService.returnChangeLogs = []
//        mockHubService.returnConnections = []
//
//        when:
//        def command = new SyncHubCommandStep()
//        command.url = "jdbc://test2"
//        command.changeLogFile = "com/example/registered.mock"
//
//        command.run(resultsBuilder)
//
//        then:
//        def e = thrown(CommandExecutionException)
//        assert e.message == "Changelog com/example/registered.mock has an unrecognized changeLogId."
//
//    }


}
