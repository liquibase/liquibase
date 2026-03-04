package liquibase.command.core

import spock.lang.Specification
import spock.lang.Unroll

class ProCommandsRegistryTest extends Specification {

    @Unroll
    def "isProCommand should return true for Pro command: #command"() {
        expect:
        ProCommandsRegistry.isProCommand(command) == true

        where:
        command << [
            "flow",
            "checks",
            "rollbackonechangeset",
            "rollbackoneupdate",
            "updateonechangeset",
            "dbclhistory",
            "databasechangeloghistory",
            "connect",
            "driftdetect",
            "driftreport",
            "snapshotreference"
        ]
    }

    @Unroll
    def "isProCommand should return true for Pro command with different case: #command"() {
        expect:
        ProCommandsRegistry.isProCommand(command) == true

        where:
        command << [
            "FLOW",
            "Checks",
            "RollbackOneChangeset",
            "ROLLBACKONEUPDATE",
            "UpdateOneChangeset"
        ]
    }

    @Unroll
    def "isProCommand should return false for Community command: #command"() {
        expect:
        ProCommandsRegistry.isProCommand(command) == false

        where:
        command << [
            "update",
            "rollback",
            "status",
            "validate",
            "diff",
            "snapshot",
            "generateChangeLog",
            "clearChecksums",
            "history",
            "tag",
            "dropAll"
        ]
    }

    def "isProCommand should return false for null or empty input"() {
        expect:
        ProCommandsRegistry.isProCommand(null) == false
        ProCommandsRegistry.isProCommand("") == false
        ProCommandsRegistry.isProCommand("   ") == false
    }

    @Unroll
    def "isProSubcommand should return true for valid Pro subcommand: #command #subcommand"() {
        expect:
        ProCommandsRegistry.isProSubcommand(command, subcommand) == true

        where:
        command   | subcommand
        "checks"  | "run"
        "checks"  | "show"
        "checks"  | "enable"
        "checks"  | "disable"
        "flow"    | "validate"
        "CHECKS"  | "RUN"
        "Flow"    | "Validate"
    }

    @Unroll
    def "isProSubcommand should return false for invalid combinations: #command #subcommand"() {
        expect:
        ProCommandsRegistry.isProSubcommand(command, subcommand) == false

        where:
        command        | subcommand
        "update"       | "run"     // Community command with Pro subcommand
        "checks"       | "update"  // Pro command with invalid subcommand
        "invalid"      | "run"     // Invalid command
        "flow"         | "invalid" // Pro command with invalid subcommand
        null           | "run"
        "checks"       | null
        ""             | "run"
        "checks"       | ""
    }

    @Unroll
    def "getFormattedCommand should format correctly: #command #subcommand -> #expected"() {
        expect:
        ProCommandsRegistry.getFormattedCommand(command, subcommand) == expected

        where:
        command  | subcommand | expected
        "checks" | "run"      | "checks run"
        "flow"   | "validate" | "flow validate"
        "checks" | null       | "checks"
        "flow"   | ""         | "flow"
        "update" | "sql"      | "update sql"
    }

    def "PRO_COMMANDS contains expected commands"() {
        expect:
        ProCommandsRegistry.PRO_COMMANDS.contains("flow")
        ProCommandsRegistry.PRO_COMMANDS.contains("checks")
        ProCommandsRegistry.PRO_COMMANDS.contains("rollbackonechangeset")
        ProCommandsRegistry.PRO_COMMANDS.contains("rollbackoneupdate")
        ProCommandsRegistry.PRO_COMMANDS.contains("updateonechangeset")
        ProCommandsRegistry.PRO_COMMANDS.contains("dbclhistory")
        ProCommandsRegistry.PRO_COMMANDS.contains("databasechangeloghistory")
        ProCommandsRegistry.PRO_COMMANDS.contains("connect")
        ProCommandsRegistry.PRO_COMMANDS.contains("driftdetect")
        ProCommandsRegistry.PRO_COMMANDS.contains("driftreport")
        ProCommandsRegistry.PRO_COMMANDS.contains("snapshotreference")

        and: "does not contain Community commands"
        !ProCommandsRegistry.PRO_COMMANDS.contains("update")
        !ProCommandsRegistry.PRO_COMMANDS.contains("rollback")
        !ProCommandsRegistry.PRO_COMMANDS.contains("status")
    }

    def "PRO_SUBCOMMANDS contains expected subcommands"() {
        expect:
        ProCommandsRegistry.PRO_SUBCOMMANDS.contains("run")
        ProCommandsRegistry.PRO_SUBCOMMANDS.contains("show")
        ProCommandsRegistry.PRO_SUBCOMMANDS.contains("enable")
        ProCommandsRegistry.PRO_SUBCOMMANDS.contains("disable")
        ProCommandsRegistry.PRO_SUBCOMMANDS.contains("validate")

        and: "does not contain invalid subcommands"
        !ProCommandsRegistry.PRO_SUBCOMMANDS.contains("update")
        !ProCommandsRegistry.PRO_SUBCOMMANDS.contains("rollback")
    }
}