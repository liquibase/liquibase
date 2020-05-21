package liquibase.command.core

import liquibase.changelog.ChangeLogHistoryService
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.changelog.RanChangeSet
import liquibase.database.Database
import liquibase.database.DatabaseConnection

import java.text.*
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class HistoryCommandTest extends Specification {
    @Shared
    def dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)

    def lastDefaultLocale;

    def setup() {
        lastDefaultLocale = Locale.getDefault()
        Locale.setDefault(Locale.US)
    }

    def cleanup() {
        Locale.setDefault(lastDefaultLocale);
        ChangeLogHistoryServiceFactory.reset()
    }

    @Unroll
    def "run"() {
        when:
        def conn = Mock(DatabaseConnection)
        conn.getURL() >> "jdbc:test://url"

        def database = Mock(Database)
        database.getConnection() >> conn

        def output = new ByteArrayOutputStream()

        def historyCommand = new HistoryCommand()
        historyCommand.database = database
        historyCommand.outputStream = new PrintStream(output)

        def historyService = Mock(ChangeLogHistoryService)
        historyService.getRanChangeSets() >> changeSets

        def historyFactory = Mock(ChangeLogHistoryServiceFactory)
        historyFactory.getChangeLogService(_) >> historyService

        ChangeLogHistoryServiceFactory.setInstance(historyFactory)


        historyCommand.run()

        then:
        new String(output.toByteArray()).replaceAll("\r\n", "\n").trim() == expectedOut.replaceAll("\r\n", "\n").trim()

        where:
        [changeSets, expectedOut] << [
                //no history
                [
                        [],
                        """
Liquibase History for jdbc:test://url

No changeSets deployed
"""
                ],

                //one changeSet
                [
                        [
                                new RanChangeSet("com/example/test.xml", "13",
                                    "test-user", null,
                                    new Date().parse('yyyy/MM/dd HH:mm:ss.S', '2019/07/09 12:15:32.31'),
                                    null, null, null,
                                    null, null, null, "1"),
                        ],
                        """
Liquibase History for jdbc:test://url

- Database updated at ${dateFormat.format(Date.parse("M/dd/yy h:mm a", "7/9/19 12:15 PM"))}. Applied 1 changeSet(s), DeploymentId: 1
  com/example/test.xml::13::test-user
"""
                ],

                //multiple changeSets in the same deployment
                [
                        [
                                new RanChangeSet("com/example/test.xml", "13", "test-user", null, new Date().parse('yyyy/MM/dd HH:mm:ss.S', '2019/07/09 12:15:32.31'), null, null, null, null, null, null, "1"),
                                new RanChangeSet("com/example/test.xml", "14", "other-user", null, new Date().parse('yyyy/MM/dd HH:mm:ss.S', '2019/07/09 12:15:32.91'), null, null, null, null, null, null, "1"),
                                new RanChangeSet("com/example/test.xml", "15", "test-user", null, new Date().parse('yyyy/MM/dd HH:mm:ss.S', '2019/07/09 12:15:34.13'), null, null, null, null, null, null, "1"),
                        ],
                        """
Liquibase History for jdbc:test://url

- Database updated at ${dateFormat.format(Date.parse("M/dd/yy h:mm a", "7/9/19 12:15 PM"))}. Applied 3 changeSet(s) in 1.982s, DeploymentId: 1
  com/example/test.xml::13::test-user
  com/example/test.xml::14::other-user
  com/example/test.xml::15::test-user
"""
                ],


                //larger example
                [
                        [
                                new RanChangeSet("com/example/test.xml", "13", "test-user", null, new Date().parse('yyyy/MM/dd HH:mm:ss.S', '2019/07/09 12:15:32.31'), null, null, null, null, null, null, "1"),
                                new RanChangeSet("com/example/test.xml", "14", "other-user", null, new Date().parse('yyyy/MM/dd HH:mm:ss.S', '2019/07/09 12:15:32.91'), null, null, null, null, null, null, "1"),
                                new RanChangeSet("com/example/test.xml", "15", "test-user", null, new Date().parse('yyyy/MM/dd HH:mm:ss.S', '2019/07/09 12:15:34.13'), null, null, null, null, null, null, "1"),

                                new RanChangeSet("com/example/test2.xml", "13", "test-user", null, new Date().parse('yyyy/MM/dd HH:mm:ss.S', '2019/07/09 14:18:33.13'), null, null, null, null, null, null, "2"),

                                new RanChangeSet("com/example/test.xml", "1", "test-user", null, new Date().parse('yyyy/MM/dd HH:mm:ss.S', '2019/07/09 18:22:32.31'), null, null, null, null, null, null, "3"),
                                new RanChangeSet("com/example/test.xml3", "2", "other-user", null, new Date().parse('yyyy/MM/dd HH:mm:ss.S', '2019/07/09 18:23:16.91'), null, null, null, null, null, null, "3"),
                                new RanChangeSet("com/example/test.xml", "3", "test-user", null, new Date().parse('yyyy/MM/dd HH:mm:ss.S', '2019/07/09 18:26:34.13'), null, null, null, null, null, null, "3"),
                        ],
                        """
Liquibase History for jdbc:test://url

- Database updated at ${dateFormat.format(Date.parse("M/dd/yy h:mm a", "7/9/19 12:15 PM"))}. Applied 3 changeSet(s) in 1.982s, DeploymentId: 1
  com/example/test.xml::13::test-user
  com/example/test.xml::14::other-user
  com/example/test.xml::15::test-user

- Database updated at ${dateFormat.format(Date.parse("M/dd/yy h:mm a", "7/9/19 2:18 PM"))}. Applied 1 changeSet(s), DeploymentId: 2
  com/example/test2.xml::13::test-user

- Database updated at ${dateFormat.format(Date.parse("M/dd/yy h:mm a", "7/9/19 6:22 PM"))}. Applied 3 changeSet(s) in 241.982s, DeploymentId: 3
  com/example/test.xml::1::test-user
  com/example/test.xml3::2::other-user
  com/example/test.xml::3::test-user
"""
                ],
        ]
    }
}
