package liquibase.command

import liquibase.change.CheckSum
import liquibase.changelog.ChangeLogHistoryService
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.changelog.ChangeSet
import liquibase.changelog.RanChangeSet
import liquibase.command.core.InternalHistoryCommandStep
import liquibase.database.Database
import liquibase.database.DatabaseConnection
import spock.lang.Specification

import java.nio.charset.StandardCharsets

import static liquibase.servicelocator.PrioritizedService.PRIORITY_DATABASE

class InternalHistoryCommandStepTest extends Specification {

    def "displays the history in tables"() {
        given:
        def changeLogHistoryService = Mock(ChangeLogHistoryService)
        changeLogHistoryService.getRanChangeSets() >> [
                new RanChangeSet("some/change/log", "some/id", "me", CheckSum.compute("foobar"), new Date(1670000000000), "", ChangeSet.ExecType.EXECUTED, "", "", null, null, "deployment-id-1"),
                new RanChangeSet("some/change/log", "some/other/id", "me", CheckSum.compute("foobar"), new Date(1670000000000), "", ChangeSet.ExecType.EXECUTED, "", "", null, null, "deployment-id-1"),
                new RanChangeSet("some/change/log", "yet/another/id", "me", CheckSum.compute("foobar"), new Date(1675000000000), "", ChangeSet.ExecType.EXECUTED, "", "", null, null, "deployment-id-2")
        ]
        def database = databaseAt("jdbc:some://url")
        changeLogHistoryService.supports(database) >> true
        changeLogHistoryService.getPriority() >> PRIORITY_DATABASE
        ChangeLogHistoryServiceFactory.getInstance().register(changeLogHistoryService)
        def step = new InternalHistoryCommandStep()
        def outputStream = new ByteArrayOutputStream()
        def command = new CommandScope("history")
                .addArgumentValue("database", database)
                .addArgumentValue("format", "TABULAR")
        def builder = new CommandResultsBuilder(command, outputStream)

        when:
        step.run(builder)

        then:
        def output = new String(outputStream.toByteArray(), StandardCharsets.UTF_8)
        output.trim() == """
Liquibase History for jdbc:some://url

+-----------------+------------------+-----------------+-------------------+---------------+------------------------------------+
| Deployment ID   | Update date      | Change log path | Change set author | Change set ID | Checksum                           |
+-----------------+------------------+-----------------+-------------------+---------------+------------------------------------+
| deployment-id-1 | 12/2/22, 5:53 PM | some/change/log | me                | some/id       | 8:3858f62230ac3c915f300c664312c63f |
+-----------------+------------------+-----------------+-------------------+---------------+------------------------------------+
| deployment-id-1 | 12/2/22, 5:53 PM | some/change/log | me                | some/other/id | 8:3858f62230ac3c915f300c664312c63f |
+-----------------+------------------+-----------------+-------------------+---------------+------------------------------------+

+-----------------+------------------+-----------------+-------------------+----------------+------------------------------------+
| Deployment ID   | Update date      | Change log path | Change set author | Change set ID  | Checksum                           |
+-----------------+------------------+-----------------+-------------------+----------------+------------------------------------+
| deployment-id-2 | 1/29/23, 2:46 PM | some/change/log | me                | yet/another/id | 8:3858f62230ac3c915f300c664312c63f |
+-----------------+------------------+-----------------+-------------------+----------------+------------------------------------+

""".trim()

    }

    private Database databaseAt(String url) {
        def database = Mock(Database)
        def connection = Mock(DatabaseConnection)
        connection.getURL() >> url
        database.getConnection() >> connection
        return database
    }
}
