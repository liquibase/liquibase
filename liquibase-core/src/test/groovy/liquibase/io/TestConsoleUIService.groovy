package liquibase.io


import liquibase.Scope
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.changelog.DatabaseChangeLog
import liquibase.changelog.MockChangeLogHistoryService
import liquibase.command.core.SyncHubCommand
import liquibase.database.core.MockDatabase
import liquibase.exception.LiquibaseException
import liquibase.hub.HubService
import liquibase.hub.HubServiceFactory
import liquibase.hub.core.MockHubService
import liquibase.hub.model.Connection
import liquibase.hub.model.HubChangeLog
import liquibase.hub.model.Project
import liquibase.parser.ChangeLogParserFactory
import liquibase.parser.MockChangeLogParser
import spock.lang.Specification

class TestConsoleUIService extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    def "Test for console input with bad timer value"() {
        when:
        Scope.getCurrentScope().getUI().prompt("Enter (Y/N/S)?", "Y", 0, new TestConsoleDelegate("Y", 10))

        then:
        IllegalArgumentException iae = thrown()
        iae.message == "Value for countdown timer must be greater than 0"
    }

    def "Test for console input with good timer value"() {
        when:
        def input = Scope.getCurrentScope().getUI().prompt("Enter (Y/N/S)?", "S", 15, new TestConsoleDelegate("Y", 10))

        then:
        input == "Y"
    }

    def "Test for console input with default value"() {
        when:
        def input = Scope.getCurrentScope().getUI().prompt("Enter (Y/N/S)?", "S", 15, new TestConsoleDelegate("", 10))

        then:
        input == "S"
    }
}
