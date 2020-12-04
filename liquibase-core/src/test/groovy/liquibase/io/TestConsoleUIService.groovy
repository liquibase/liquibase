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
        Scope.getCurrentScope().getUI().prompt("Enter (Y/N/S)?", "Y", 0, String.class)

        then:
        IllegalArgumentException iae = thrown()
        iae.message == "Value for countdown timer must be greater than 0"
    }

    def "Test for console input with good timer value"() {
        when:
        MockUIService mockUIService = new MockUIService(5, "Y");
        def setupScopeId = Scope.enter([
            (Scope.Attr.ui.toString()): mockUIService
        ])
        def input = Scope.getCurrentScope().getUI()
                         .prompt("Enter (Y/N/S)?", "S", 7, String.class)
        Scope.exit(setupScopeId)

        then:
        input == "Y"
    }

    def "Test for console boolean input value"() {
        when:
        MockUIService mockUIService = new MockUIService(2, "true");
        def setupScopeId = Scope.enter([
            (Scope.Attr.ui.toString()): mockUIService
        ])
        def input = Scope.getCurrentScope().getUI()
            .prompt("Enter (true/false)?", false, 5, Boolean.class)
        Scope.exit(setupScopeId)

        then:
        input
    }

    def "Test for console integer input value"() {
        when:
        MockUIService mockUIService = new MockUIService(5, "a", "3");
        def setupScopeId = Scope.enter([
            (Scope.Attr.ui.toString()): mockUIService
        ])
        def input = Scope.getCurrentScope().getUI()
                         .prompt("Enter (0-5)?", 1, 10, Integer.class)
        Scope.exit(setupScopeId)

        then:
        input == 3
    }

    def "Test for console integer default input value"() {
        when:
        MockUIService mockUIService = new MockUIService( 2, "")
        def setupScopeId = Scope.enter([
            (Scope.Attr.ui.toString()): mockUIService
        ])
        def input = Scope.getCurrentScope().getUI()
            .prompt("Enter (0-5)?", 1, 5, Integer.class)
        Scope.exit(setupScopeId)

        then:
        input == 1
    }

    def "Test for console input with default value"() {
        when:
        MockUIService mockUIService = new MockUIService(-1, "Y");
        def setupScopeId = Scope.enter([
            (Scope.Attr.ui.toString()): mockUIService
        ])
        def input =
            Scope.getCurrentScope().getUI()
                                   .prompt("Enter (Y/N/S)?", "S", 5, String.class)
        Scope.exit(setupScopeId)

        then:
        input == "S"
    }
}