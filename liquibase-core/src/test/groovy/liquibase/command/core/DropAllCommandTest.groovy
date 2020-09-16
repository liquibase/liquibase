package liquibase.command.core

import liquibase.Liquibase
import liquibase.Scope
import liquibase.changelog.DatabaseChangeLog
import liquibase.configuration.HubConfiguration
import liquibase.configuration.LiquibaseConfiguration
import liquibase.database.core.MockDatabase
import liquibase.database.core.MockDatabaseTest
import liquibase.hub.HubService
import liquibase.hub.core.MockHubService
import liquibase.test.JUnitResourceAccessor
import liquibase.util.FileUtil
import spock.lang.Specification

class DropAllCommandTest extends Specification {

    private String scopeId
    private File outputFile

    def setup() {
        URL url = Thread.currentThread().getContextClassLoader().getResource("liquibase/test-changelog.xml")
        File changelogFile = new File(url.toURI())
        String contents = FileUtil.getContents(changelogFile)
        outputFile = File.createTempFile("registerChangelog-", ".xml", new File("target/test-classes"))
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

        def hubConfiguration = LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class)
        hubConfiguration.setLiquibaseHubProject("PROJECT 1")
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
}
