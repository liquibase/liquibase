package liquibase.integration.commandline

import spock.lang.Specification
import spock.lang.Unroll

/**
 * Integration test for Pro command error messaging in CLI.
 * Tests that attempting to run Pro commands without a license
 * shows appropriate error messages instead of generic "Unexpected argument" errors.
 */
class LiquibaseCommandLineProCommandTest extends Specification {

    def setup() {
        // Ensure we're in Community mode by clearing any license configuration
        System.clearProperty("liquibase.licenseKey")
        System.clearProperty("LIQUIBASE_LICENSE_KEY")
    }

    @Unroll
    def "Pro command '#command' should show license error message"() {
        given:
        ByteArrayOutputStream errStream = new ByteArrayOutputStream()
        System.setErr(new PrintStream(errStream))

        LiquibaseCommandLine liquibaseCommandLine = new LiquibaseCommandLine()

        when:
        int exitCode = liquibaseCommandLine.execute([command] as String[])

        then:
        exitCode != 0
        String errorOutput = errStream.toString()
        errorOutput.contains("Error parsing command line: Using '$command' requires a valid Liquibase license key")
        errorOutput.contains("Get a free Liquibase license key and trial at https://liquibase.com/trial")
        !errorOutput.contains("Unexpected argument(s):")

        cleanup:
        System.setErr(System.err)

        where:
        command << [
            "flow",
            "checks",
            "rollbackOneChangeset",
            "rollbackOneUpdate",
            "updateOneChangeset",
            "dbclHistory",
            "databaseChangelogHistory",
            "connect",
            "driftDetect",
            "driftReport"
        ]
    }

    @Unroll
    def "Pro subcommand '#command #subcommand' should show license error message"() {
        given:
        ByteArrayOutputStream errStream = new ByteArrayOutputStream()
        System.setErr(new PrintStream(errStream))

        LiquibaseCommandLine liquibaseCommandLine = new LiquibaseCommandLine()

        when:
        int exitCode = liquibaseCommandLine.execute([command, subcommand] as String[])

        then:
        exitCode != 0
        String errorOutput = errStream.toString()
        errorOutput.contains("Error parsing command line: Using '$command $subcommand' requires a valid Liquibase license key")
        errorOutput.contains("Get a free Liquibase license key and trial at https://liquibase.com/trial")
        !errorOutput.contains("Unexpected argument(s):")

        cleanup:
        System.setErr(System.err)

        where:
        command  | subcommand
        "checks" | "run"
        "checks" | "show"
        "checks" | "enable"
        "checks" | "disable"
    }

    def "Non-Pro command should still show 'Unexpected argument' error"() {
        given:
        ByteArrayOutputStream errStream = new ByteArrayOutputStream()
        System.setErr(new PrintStream(errStream))

        LiquibaseCommandLine liquibaseCommandLine = new LiquibaseCommandLine()

        when:
        int exitCode = liquibaseCommandLine.execute(["nonexistentcommand"] as String[])

        then:
        exitCode != 0
        String errorOutput = errStream.toString()
        errorOutput.contains("Unexpected argument(s): nonexistentcommand")
        !errorOutput.contains("requires a valid Liquibase license key")

        cleanup:
        System.setErr(System.err)
    }

    def "Case insensitive Pro command detection works"() {
        given:
        ByteArrayOutputStream errStream = new ByteArrayOutputStream()
        System.setErr(new PrintStream(errStream))

        LiquibaseCommandLine liquibaseCommandLine = new LiquibaseCommandLine()

        when:
        int exitCode = liquibaseCommandLine.execute(["FLOW"] as String[])

        then:
        exitCode != 0
        String errorOutput = errStream.toString()
        errorOutput.contains("Error parsing command line: Using 'FLOW' requires a valid Liquibase license key")

        cleanup:
        System.setErr(System.err)
    }

    def "Mixed case Pro subcommand detection works"() {
        given:
        ByteArrayOutputStream errStream = new ByteArrayOutputStream()
        System.setErr(new PrintStream(errStream))

        LiquibaseCommandLine liquibaseCommandLine = new LiquibaseCommandLine()

        when:
        int exitCode = liquibaseCommandLine.execute(["Checks", "Run"] as String[])

        then:
        exitCode != 0
        String errorOutput = errStream.toString()
        errorOutput.contains("Error parsing command line: Using 'Checks Run' requires a valid Liquibase license key")

        cleanup:
        System.setErr(System.err)
    }

    def "Pro command with invalid subcommand shows single command error"() {
        given:
        ByteArrayOutputStream errStream = new ByteArrayOutputStream()
        System.setErr(new PrintStream(errStream))

        LiquibaseCommandLine liquibaseCommandLine = new LiquibaseCommandLine()

        when:
        int exitCode = liquibaseCommandLine.execute(["checks", "invalidsubcommand"] as String[])

        then:
        exitCode != 0
        String errorOutput = errStream.toString()
        // Should show error for 'checks' command only since 'invalidsubcommand' is not a valid Pro subcommand
        errorOutput.contains("Error parsing command line: Using 'checks' requires a valid Liquibase license key")

        cleanup:
        System.setErr(System.err)
    }
}
