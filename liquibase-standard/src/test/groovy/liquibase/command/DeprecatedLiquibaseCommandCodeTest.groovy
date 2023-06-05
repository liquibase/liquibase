package liquibase.command


import liquibase.command.core.MockLiquibaseCommand
import spock.lang.Specification

/**
 * Test deprecated styles of code to ensure we are API compatible with them.
 * Be liberal with type casts so ensure we are using the correct APIs
 */
class DeprecatedLiquibaseCommandCodeTest extends Specification {

    def "4_3 command lookup and execution"() {
        when:
        MockLiquibaseCommand mockCommand = (MockLiquibaseCommand) CommandFactory.getInstance().getCommand("mock")
        mockCommand.setValue1("Value 1");
        def commandResult = CommandFactory.getInstance().execute(mockCommand);

        then:
        commandResult.message == "Mock command ran with value1 = Value 1"
    }
}
