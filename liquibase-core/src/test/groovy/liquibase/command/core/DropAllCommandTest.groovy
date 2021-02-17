package liquibase.command.core

import liquibase.Liquibase
import liquibase.Scope

import liquibase.database.core.MockDatabase
import liquibase.exception.CommandExecutionException
import liquibase.hub.HubConfiguration
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

        command.run()

        then:
        notThrown(CommandExecutionException)
    }

    def "unregisteredChangeLog"() {
        when:
        def outputStream = new ByteArrayOutputStream()
        BufferedLogService bufferLog = new BufferedLogService()

        Scope.getCurrentScope().getUI()

        Scope.child(HubConfiguration.LIQUIBASE_HUB_API_KEY.getKey(), UUID.randomUUID().toString(), {
            def command = new DropAllCommand()
            JUnitResourceAccessor testResourceAccessor = new JUnitResourceAccessor()
            Liquibase liquibase = new Liquibase(outputFile.getName(), testResourceAccessor, new MockDatabase())
            command.setLiquibase(liquibase)
            command.setChangeLogFile(outputFile.getName())
            command.setDatabase(new MockDatabase())
            command.setSchemas("")

            CompositeLogService compositeLogService = new CompositeLogService(true, bufferLog);
            Scope.child(Scope.Attr.logService.name(), compositeLogService, {
                command.run()
            })
        })

        then:
        notThrown(CommandExecutionException)

        bufferLog.getLogAsString(Level.INFO).contains("WARNING The changelog file specified is not registered with any Liquibase Hub project")

    }
}
