package liquibase.command

import liquibase.Scope
import liquibase.changelog.ChangeLogHistoryService
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.changelog.ChangeSet
import liquibase.changelog.RanChangeSet
import liquibase.command.core.HistoryCommandStep
import liquibase.command.core.HistoryFormat
import liquibase.database.Database
import liquibase.database.DatabaseConnection
import liquibase.util.StringUtil
import spock.lang.Specification

import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat

import static liquibase.servicelocator.PrioritizedService.PRIORITY_DATABASE

class HistoryCommandStepTest extends Specification {

    HistoryCommandStep historyCommand

    ByteArrayOutputStream outputStream

    Database database

    ChangeLogHistoryServiceFactory changeLogHistoryServiceFactory = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class)

    ChangeLogHistoryService changeLogHistoryService

    def setup() {
        changeLogHistoryService = Mock(ChangeLogHistoryService)
        changeLogHistoryService.getRanChangeSets() >> [
                new RanChangeSet("some/change/log", "some/id", "me", null, new Date(1670000000000), "tag1", ChangeSet.ExecType.EXECUTED, "", "", null, null, "deployment-id-1"),
                new RanChangeSet("some/change/log", "some/other/id", "me", null, new Date(1670000000000), "", ChangeSet.ExecType.EXECUTED, "", "", null, null, "deployment-id-1"),
                new RanChangeSet("some/change/log", "yet/another/id", "me", null, new Date(1675000000000), "tag2", ChangeSet.ExecType.EXECUTED, "", "", null, null, "deployment-id-2")
        ]
        database = databaseAt("jdbc:some://url")
        changeLogHistoryService.supports(database) >> true
        changeLogHistoryService.getPriority() >> PRIORITY_DATABASE
        changeLogHistoryServiceFactory.register(changeLogHistoryService)

        historyCommand = new HistoryCommandStep()
        outputStream = new ByteArrayOutputStream()
    }

    void cleanup() {
        changeLogHistoryServiceFactory.unregister(changeLogHistoryService)
    }

    def "displays the history in tabular format"() {
        given:
        def command = new CommandScope(HistoryCommandStep.COMMAND_NAME)
                .provideDependency(Database.class, database)
                .addArgumentValue(HistoryCommandStep.DATE_FORMAT_ARG, new SimpleDateFormat("yyyy"))
                .addArgumentValue(HistoryCommandStep.FORMAT_ARG.getName(), format)

        def builder = new CommandResultsBuilder(command, outputStream)

        when:
        historyCommand.run(builder)

        then:
        def output = new String(outputStream.toByteArray(), StandardCharsets.UTF_8)
        StringUtil.standardizeLineEndings(output.trim()) == StringUtil.standardizeLineEndings("""
Liquibase History for jdbc:some://url

+-----------------+-------------+-----------------+------------------+---------------+------+
| Deployment ID   | Update Date | Changelog Path  | Changeset Author | Changeset ID  | Tag  |
+-----------------+-------------+-----------------+------------------+---------------+------+
| deployment-id-1 | 2022        | some/change/log | me               | some/id       | tag1 |
+-----------------+-------------+-----------------+------------------+---------------+------+
| deployment-id-1 | 2022        | some/change/log | me               | some/other/id |      |
+-----------------+-------------+-----------------+------------------+---------------+------+

+-----------------+-------------+-----------------+------------------+----------------+------+
| Deployment ID   | Update Date | Changelog Path  | Changeset Author | Changeset ID   | Tag  |
+-----------------+-------------+-----------------+------------------+----------------+------+
| deployment-id-2 | 2023        | some/change/log | me               | yet/another/id | tag2 |
+-----------------+-------------+-----------------+------------------+----------------+------+

""".trim())

        where:
        format    | _
        "TABULAR" | _
        "TabuLAr" | _
        null      | _
    }

    def "displays the history in text format"() {
        given:
        def command = new CommandScope(HistoryCommandStep.COMMAND_NAME)
                .provideDependency(Database.class, database)
                .addArgumentValue(HistoryCommandStep.DATE_FORMAT_ARG, new SimpleDateFormat("yyyy"))
                .addArgumentValue(HistoryCommandStep.FORMAT_ARG, HistoryFormat.TEXT)

        def builder = new CommandResultsBuilder(command, outputStream)

        when:
        historyCommand.run(builder)

        then:
        def output = new String(outputStream.toByteArray(), StandardCharsets.UTF_8)
        StringUtil.standardizeLineEndings(output.trim()) == StringUtil.standardizeLineEndings("""
Liquibase History for jdbc:some://url

- Database updated at 2022. Applied 2 changeset(s) in 0.0s, DeploymentId: deployment-id-1
  some/change/log::some/id::me
  some/change/log::some/other/id::me

- Database updated at 2023. Applied 1 changeset(s), DeploymentId: deployment-id-2
  some/change/log::yet/another/id::me
""".trim())
    }

    private Database databaseAt(String url) {
        def database = Mock(Database)
        def connection = Mock(DatabaseConnection)
        connection.getURL() >> url
        database.getConnection() >> connection
        return database
    }

    def "displays only tagged changeset"() {
        given:
        def command = new CommandScope(HistoryCommandStep.COMMAND_NAME)
                .provideDependency(Database.class, database)
                .addArgumentValue(HistoryCommandStep.DATE_FORMAT_ARG, new SimpleDateFormat("yyyy"))
                .addArgumentValue(HistoryCommandStep.SHOW_TAGS_ARG.getName(), true)

        def builder = new CommandResultsBuilder(command, outputStream)

        when:
        historyCommand.run(builder)

        then:
        def output = new String(outputStream.toByteArray(), StandardCharsets.UTF_8)
        StringUtil.standardizeLineEndings(output.trim()) == StringUtil.standardizeLineEndings("""
Liquibase History for jdbc:some://url

+-----------------+-------------+-----------------+------------------+--------------+------+
| Deployment ID   | Update Date | Changelog Path  | Changeset Author | Changeset ID | Tag  |
+-----------------+-------------+-----------------+------------------+--------------+------+
| deployment-id-1 | 2022        | some/change/log | me               | some/id      | tag1 |
+-----------------+-------------+-----------------+------------------+--------------+------+

+-----------------+-------------+-----------------+------------------+----------------+------+
| Deployment ID   | Update Date | Changelog Path  | Changeset Author | Changeset ID   | Tag  |
+-----------------+-------------+-----------------+------------------+----------------+------+
| deployment-id-2 | 2023        | some/change/log | me               | yet/another/id | tag2 |
+-----------------+-------------+-----------------+------------------+----------------+------+

""".trim())
    }


    def "displays only a given tag"() {
        given:
        def command = new CommandScope(HistoryCommandStep.COMMAND_NAME)
                .provideDependency(Database.class, database)
                .addArgumentValue(HistoryCommandStep.DATE_FORMAT_ARG, new SimpleDateFormat("yyyy"))
                .addArgumentValue(HistoryCommandStep.TAG_FILTER_ARG.getName(), "tag2")

        def builder = new CommandResultsBuilder(command, outputStream)

        when:
        historyCommand.run(builder)

        then:
        def output = new String(outputStream.toByteArray(), StandardCharsets.UTF_8)
        StringUtil.standardizeLineEndings(output.trim()) == StringUtil.standardizeLineEndings("""
Liquibase History for jdbc:some://url

+-----------------+-------------+-----------------+------------------+----------------+------+
| Deployment ID   | Update Date | Changelog Path  | Changeset Author | Changeset ID   | Tag  |
+-----------------+-------------+-----------------+------------------+----------------+------+
| deployment-id-2 | 2023        | some/change/log | me               | yet/another/id | tag2 |
+-----------------+-------------+-----------------+------------------+----------------+------+

""".trim())
    }

    def "displays only 2 selected tagged changeset"() {
        given:
        def command = new CommandScope(HistoryCommandStep.COMMAND_NAME)
                .provideDependency(Database.class, database)
                .addArgumentValue(HistoryCommandStep.DATE_FORMAT_ARG, new SimpleDateFormat("yyyy"))
                .addArgumentValue(HistoryCommandStep.TAG_FILTER_ARG.getName(), "tag2,tag1")

        def builder = new CommandResultsBuilder(command, outputStream)

        when:
        historyCommand.run(builder)

        then:
        def output = new String(outputStream.toByteArray(), StandardCharsets.UTF_8)
        StringUtil.standardizeLineEndings(output.trim()) == StringUtil.standardizeLineEndings("""
Liquibase History for jdbc:some://url

+-----------------+-------------+-----------------+------------------+--------------+------+
| Deployment ID   | Update Date | Changelog Path  | Changeset Author | Changeset ID | Tag  |
+-----------------+-------------+-----------------+------------------+--------------+------+
| deployment-id-1 | 2022        | some/change/log | me               | some/id      | tag1 |
+-----------------+-------------+-----------------+------------------+--------------+------+

+-----------------+-------------+-----------------+------------------+----------------+------+
| Deployment ID   | Update Date | Changelog Path  | Changeset Author | Changeset ID   | Tag  |
+-----------------+-------------+-----------------+------------------+----------------+------+
| deployment-id-2 | 2023        | some/change/log | me               | yet/another/id | tag2 |
+-----------------+-------------+-----------------+------------------+----------------+------+

""".trim())
    }

    def "displays error message when tag is not found"() {
        given:
        def command = new CommandScope(HistoryCommandStep.COMMAND_NAME)
                .provideDependency(Database.class, database)
                .addArgumentValue(HistoryCommandStep.DATE_FORMAT_ARG, new SimpleDateFormat("yyyy"))
                .addArgumentValue(HistoryCommandStep.TAG_FILTER_ARG.getName(), "tagDoesNotExist")

        def builder = new CommandResultsBuilder(command, outputStream)

        when:
        historyCommand.run(builder)

        then:
        def output = new String(outputStream.toByteArray(), StandardCharsets.UTF_8)
        StringUtil.standardizeLineEndings(output.trim()) == StringUtil.standardizeLineEndings("""
Liquibase History for jdbc:some://url

No changesets with tag(s) [tagDoesNotExist] deployed
""".trim())
    }

}
