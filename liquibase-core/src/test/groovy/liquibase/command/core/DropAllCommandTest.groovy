package liquibase.command.core

import liquibase.Liquibase
import liquibase.Scope
import liquibase.configuration.HubConfiguration
import liquibase.configuration.LiquibaseConfiguration
import liquibase.database.core.MockDatabase
import liquibase.hub.HubService
import liquibase.hub.core.MockHubService
import liquibase.logging.core.BufferedLogService
import liquibase.logging.core.CompositeLogService
import liquibase.test.JUnitResourceAccessor
import liquibase.util.FileUtil
import spock.lang.Specification

import java.util.logging.Level

class DropAllCommandTest extends Specification {

    private String scopeId
    private File outputFile

    def setup() {
        URL url = Thread.currentThread().getContextClassLoader().getResource("liquibase/test-changelog.xml")
        File changelogFile = new File(url.toURI())
        String contents = FileUtil.getContents(changelogFile)
        outputFile = File.createTempFile("registeredChangelog-", ".xml", new File("target/test-classes"))
        outputFile.deleteOnExit()
        FileUtil.write(contents, outputFile)

        JUnitResourceAccessor testResourceAccessor = new JUnitResourceAccessor()
        Map<String, Object> scopeMap = new HashMap<>()
        scopeMap.put(Scope.Attr.resourceAccessor.name(), testResourceAccessor)
        scopeMap.put("liquibase.plugin." + HubService.name, MockHubService)
        scopeId = Scope.enter(scopeMap)
    }

    def "cleanup"() {
        Scope.exit(scopeId)
    }

    def "happyPath"() {
        when:
        def outputStream = new ByteArrayOutputStream()

        def command = new DropAllCommand()
        JUnitResourceAccessor testResourceAccessor = new JUnitResourceAccessor()
        Liquibase liquibase = new Liquibase(outputFile.getName(), testResourceAccessor, new MockDatabase())
        command.setLiquibase(liquibase)
        command.setChangeLogFile(outputFile.getName())
        command.setDatabase(new MockDatabase())
        command.setSchemas("")

        def result = command.run()

        then:
        result.succeeded
    }

    def "unregisteredChangeLog"() {
        when:
        def outputStream = new ByteArrayOutputStream()
        BufferedLogService bufferLog = new BufferedLogService()

        Scope.getCurrentScope().getUI()
        def hubConfiguration = LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class)
        hubConfiguration.setLiquibaseHubApiKey(UUID.randomUUID().toString())
        def command = new DropAllCommand()
        JUnitResourceAccessor testResourceAccessor = new JUnitResourceAccessor()
        Liquibase liquibase = new Liquibase(outputFile.getName(), testResourceAccessor, new MockDatabase())
        command.setLiquibase(liquibase)
        command.setChangeLogFile(outputFile.getName())
        command.setDatabase(new MockDatabase())
        command.setSchemas("")

        def result
        CompositeLogService compositeLogService = new CompositeLogService(true, bufferLog);
        Scope.child(Scope.Attr.logService.name(), compositeLogService, new Scope.ScopedRunner() {
            public void run() {
                result = command.run()
            }
        });

        then:
        result.succeeded
        bufferLog.getLogAsString(Level.INFO).contains("WARNING The changelog file specified is not registered with any Liquibase Hub project")

    }
}
