package liquibase.command.core

import liquibase.configuration.ConfigurationValueObfuscator
import spock.lang.Specification

class StartH2CommandStepTest extends Specification {

    def "buildConnectionInfoMessage never echoes the raw password — only the obfuscated form appears (CWE-532)"() {
        // CWE-532 regression: the start-h2 command's stdout banner used to
        // concatenate the raw password into System.out (StartH2CommandStep.java:106
        // and :110 in the pre-fix code). Stdout is captured by terminal loggers, CI
        // output buffers, and is visible to anyone looking over the operator's
        // shoulder. PASSWORD_ARG is registered with
        // ConfigurationValueObfuscator.STANDARD for log/MDC redaction; this test
        // pins the same obfuscation behaviour for the stdout banner. The helper
        // contract is: the caller passes an *already-obfuscated* password — the
        // helper has no path to read the raw value.
        given:
        String rawPassword = "supersecret-pw-12345-NOT-A-REAL-CREDENTIAL"
        String obfuscated = ConfigurationValueObfuscator.STANDARD.obfuscate(rawPassword)

        when:
        String message = StartH2CommandStep.buildConnectionInfoMessage(
                "dbuser", obfuscated, 9090,
                "http://localhost:8080/dev-session-id",
                "http://localhost:8080/integration-session-id")

        then: "the raw password value never appears in the banner"
        !message.contains(rawPassword)

        and: "the obfuscated form (asterisks) appears in both Password: lines"
        message.contains("*****")
        // STANDARD obfuscator returns the constant "*****"; there are TWO Password
        // lines (dev + integration), each must show the obfuscated value.
        message.count("Password: *****") == 2

        and: "non-credential fields pass through unchanged"
        message.contains("Username: dbuser")
        message.contains("jdbc:h2:tcp://localhost:9090/mem:dev")
        message.contains("jdbc:h2:tcp://localhost:9090/mem:integration")
        message.contains("http://localhost:8080/dev-session-id")
        message.contains("http://localhost:8080/integration-session-id")
    }

    def "buildConnectionInfoMessage helper has the same obfuscation behaviour when called with the default 'letmein' password"() {
        // Sanity: even the documented default H2 password ("letmein") goes through
        // the obfuscator — no special-casing means no risk of forgetting to
        // obfuscate when the default changes.
        given:
        String defaultPassword = "letmein"
        String obfuscated = ConfigurationValueObfuscator.STANDARD.obfuscate(defaultPassword)

        when:
        String message = StartH2CommandStep.buildConnectionInfoMessage(
                "dbuser", obfuscated, 9090, "/dev", "/int")

        then:
        !message.contains("letmein")
        message.contains("*****")
    }
}
