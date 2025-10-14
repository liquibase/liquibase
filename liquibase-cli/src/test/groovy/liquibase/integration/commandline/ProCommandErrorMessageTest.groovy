package liquibase.integration.commandline

import liquibase.command.core.ProCommandsRegistry
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Unit test to verify Pro command error detection works correctly
 * at the method level without requiring full CLI execution.
 */
class ProCommandErrorMessageTest extends Specification {

    def liquibaseCommandLine = new LiquibaseCommandLine()

    @Unroll
    def "checkForProCommandError should return license error for Pro command: #command"() {
        when:
        String result = liquibaseCommandLine.checkForProCommandError([command])

        then:
        result != null
        result.contains("Error parsing command line: Using '$command' requires a valid Liquibase license key")
        result.contains("Get a free Liquibase license key and trial at https://liquibase.com/trial")

        where:
        command << [
            "flow",
            "checks",
            "rollbackOneChangeset",
            "rollbackOneUpdate",
            "updateOneChangeset",
            "dbclHistory",
            "connect"
        ]
    }

    @Unroll
    def "checkForProCommandError should return subcommand license error for: #command #subcommand"() {
        when:
        String result = liquibaseCommandLine.checkForProCommandError([command, subcommand])

        then:
        result != null
        result.contains("Error parsing command line: Using '$command $subcommand' requires a valid Liquibase license key")
        result.contains("Get a free Liquibase license key and trial at https://liquibase.com/trial")

        where:
        command  | subcommand
        "checks" | "run"
        "checks" | "show"
        "checks" | "enable"
        "checks" | "disable"
        "flow"   | "validate"
    }

    @Unroll
    def "checkForProCommandError should return null for OSS command: #command"() {
        when:
        String result = liquibaseCommandLine.checkForProCommandError([command])

        then:
        result == null

        where:
        command << [
            "update",
            "rollback",
            "status",
            "validate",
            "diff",
            "snapshot",
            "invalidcommand",
            "nonexistent"
        ]
    }

    def "checkForProCommandError should return null for null or empty input"() {
        expect:
        liquibaseCommandLine.checkForProCommandError(null) == null
        liquibaseCommandLine.checkForProCommandError([]) == null
    }

    def "checkForProCommandError should handle Pro command with invalid subcommand"() {
        when:
        String result = liquibaseCommandLine.checkForProCommandError(["checks", "invalidsubcommand"])

        then:
        result != null
        result.contains("Error parsing command line: Using 'checks' requires a valid Liquibase license key")
        !result.contains("checks invalidsubcommand") // Should not use the invalid subcommand
    }

    def "ProCommandsRegistry integration works correctly"() {
        expect:
        ProCommandsRegistry.isProCommand("flow") == true
        ProCommandsRegistry.isProCommand("checks") == true
        ProCommandsRegistry.isProCommand("update") == false
        ProCommandsRegistry.isProSubcommand("checks", "run") == true
        ProCommandsRegistry.isProSubcommand("checks", "invalid") == false
    }
}