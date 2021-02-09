package liquibase.command.core

import liquibase.Scope
import liquibase.changelog.DatabaseChangeLog
import liquibase.configuration.HubConfiguration
import liquibase.configuration.LiquibaseConfiguration
import liquibase.hub.HubService
import liquibase.hub.HubServiceFactory
import liquibase.hub.core.MockHubService
import liquibase.test.JUnitResourceAccessor
import liquibase.util.FileUtil
import spock.lang.Specification

import java.util.regex.Matcher
import java.util.regex.Pattern

class RegisterChangeLogCommandTest extends Specification {

    private String scopeId
    private File outputFile
    private File emptyOutputFile
    private File outputFileJSON
    private File outputFileYaml
    private File outputFileYml

    def setup() {
        URL url = Thread.currentThread().getContextClassLoader().getResource("liquibase/test-changelog.xml")
        URL jsonUrl = Thread.currentThread().getContextClassLoader().getResource("liquibase/simple.json")
        URL yamlUrl = Thread.currentThread().getContextClassLoader().getResource("liquibase/simple.yml")
        File changelogFile = new File(url.toURI())
        File jsonChangeLogFile = new File(jsonUrl.toURI())
        File yamlChangeLogFile = new File(yamlUrl.toURI())
        String contents = FileUtil.getContents(changelogFile)
        String jsonContents = FileUtil.getContents(jsonChangeLogFile)
        String yamlContents = FileUtil.getContents(yamlChangeLogFile)
        outputFile = File.createTempFile("registerChangelog-", ".xml", new File("target/test-classes"))
        outputFile.deleteOnExit()
        FileUtil.write(contents, outputFile)

        URL urlForEmptyChangelog = Thread.currentThread().getContextClassLoader().getResource("liquibase/test-changelog-empty.xml")
        changelogFile = new File(urlForEmptyChangelog.toURI())
        contents = FileUtil.getContents(changelogFile)
        emptyOutputFile = File.createTempFile("registerChangelog-", ".xml", new File("target/test-classes"))
        emptyOutputFile.deleteOnExit()
        FileUtil.write(contents, emptyOutputFile)

        outputFileJSON = File.createTempFile("registerChangelog-", ".json", new File("target/test-classes"))
        outputFileJSON.deleteOnExit()
        FileUtil.write(jsonContents, outputFileJSON)

        outputFileYaml = File.createTempFile("registerChangelog-", ".yaml", new File("target/test-classes"))
        outputFileYaml.deleteOnExit()
        FileUtil.write(yamlContents, outputFileYaml)

        outputFileYml = File.createTempFile("registerChangelog-", ".yml", new File("target/test-classes"))
        outputFileYml.deleteOnExit()
        FileUtil.write(contents, outputFileYml)

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

        def command = new RegisterChangeLogCommand()
        command.setHubProjectId(((MockHubService) Scope.currentScope.getSingleton(HubServiceFactory).getService()).projects.get(0).getId())
        command.setOutputStream(new PrintStream(outputStream))
        command.setChangeLogFile(outputFile.getName())
        command.configure([changeLog: new DatabaseChangeLog("com/example/test.xml")])

        def result = command.run()

        def hubChangeLog = command.getHubChangeLog()
        String contents = FileUtil.getContents(outputFile.getAbsoluteFile())
        String patternString = ".*changeLogId=.*\\W\\>.*"
        Pattern pattern = Pattern.compile(patternString,Pattern.DOTALL);
        Matcher matcher = pattern.matcher(contents)

        then:
        result.succeeded
        hubChangeLog.id != null
        hubChangeLog.fileName == "com/example/test.xml"
        hubChangeLog.name == "com/example/test.xml"
        matcher.matches()
    }

    def "happyPathEmptyChangeLog"() {
        when:
        def outputStream = new ByteArrayOutputStream()

        def command = new RegisterChangeLogCommand()
        command.setHubProjectId(((MockHubService) Scope.currentScope.getSingleton(HubServiceFactory).getService()).projects.get(0).getId())
        command.setOutputStream(new PrintStream(outputStream))
        command.setChangeLogFile(emptyOutputFile.getName())
        command.configure([changeLog: new DatabaseChangeLog("com/example/test.xml")])

        def result = command.run()

        def hubChangeLog = command.getHubChangeLog()
        String contents = FileUtil.getContents(emptyOutputFile.getAbsoluteFile())
        String patternString = ".*changeLogId=.*\\W\\/>.*"
        Pattern pattern = Pattern.compile(patternString,Pattern.DOTALL);
        Matcher matcher = pattern.matcher(contents)

        then:
        result.succeeded
        hubChangeLog.id != null
        hubChangeLog.fileName == "com/example/test.xml"
        hubChangeLog.name == "com/example/test.xml"
        matcher.matches()
    }

    def "happyPathJSON"() {
        when:
        def outputStream = new ByteArrayOutputStream()

        def command = new RegisterChangeLogCommand()
        command.setHubProjectId(((MockHubService) Scope.currentScope.getSingleton(HubServiceFactory).getService()).projects.get(0).getId())
        command.setOutputStream(new PrintStream(outputStream))
        command.setChangeLogFile(outputFileJSON.getName())
        command.configure([changeLog: new DatabaseChangeLog("com/example/test.json")])

        def result = command.run()

        def hubChangeLog = command.getHubChangeLog()

        then:
        result.succeeded
        hubChangeLog.id != null
        hubChangeLog.fileName == "com/example/test.json"
        hubChangeLog.name == "com/example/test.json"
    }

    def "happyPathYaml"() {
        when:
        def outputStream = new ByteArrayOutputStream()

        def command = new RegisterChangeLogCommand()
        command.setHubProjectId(((MockHubService) Scope.currentScope.getSingleton(HubServiceFactory).getService()).projects.get(0).getId())
        command.setOutputStream(new PrintStream(outputStream))
        command.setChangeLogFile(outputFileYaml.getName())
        command.configure([changeLog: new DatabaseChangeLog("com/example/test.yaml")])

        def result = command.run()

        def hubChangeLog = command.getHubChangeLog()
        def contents = outputFileYaml.getText()

        then:
        result.succeeded
        hubChangeLog.id != null
        hubChangeLog.fileName == "com/example/test.yaml"
        hubChangeLog.name == "com/example/test.yaml"
        contents.contains("- changeLogId: ")
    }

    def "happyPathYml"() {
        when:
        def outputStream = new ByteArrayOutputStream()

        def command = new RegisterChangeLogCommand()
        command.setHubProjectId(((MockHubService) Scope.currentScope.getSingleton(HubServiceFactory).getService()).projects.get(0).getId())
        command.setOutputStream(new PrintStream(outputStream))
        command.setChangeLogFile(outputFileYml.getName())
        command.configure([changeLog: new DatabaseChangeLog("com/example/test.yml")])

        def result = command.run()

        def hubChangeLog = command.getHubChangeLog()

        then:
        result.succeeded
        hubChangeLog.id != null
        hubChangeLog.fileName == "com/example/test.yml"
        hubChangeLog.name == "com/example/test.yml"
    }

    def "changeLogAlreadyRegistered"() {
        when:
        def outputStream = new ByteArrayOutputStream()

        def hubConfiguration = LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class)
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
        result.message.contains("is already registered with changeLogId '" + uuid.toString() + "'")
    }
}
