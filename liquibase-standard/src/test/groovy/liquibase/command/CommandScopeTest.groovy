package liquibase.command

import liquibase.Scope
import liquibase.command.core.MockCommandStep
import liquibase.configuration.ConfiguredValue
import liquibase.configuration.ConfiguredValueModifier
import liquibase.configuration.ConfiguredValueModifierFactory
import liquibase.configuration.ProvidedValue
import liquibase.exception.CommandValidationException
import spock.lang.Specification
import spock.lang.Unroll

class CommandScopeTest extends Specification {

    def setup() {
        MockCommandStep.reset()
    }

    @Unroll
    def "can get and set argument values"() {
        when:
        def scope = new CommandScope("mock")

        Scope.getCurrentScope().getSingleton(CommandFactory.class).unregister("mock command")
        def arg = new CommandBuilder([["mock command"]] as String[][]).argument(argumentName, String).defaultValue(defaultValue).build()

        def scopeId = Scope.enter([
                "liquibase.command.mock.argSetFromScope": "value from scope",
                "liquibase.command.argSetFromScope"     : "value from scope directly under command",
                "liquibase.command.otherArgInScope"     : "other value in scope",
        ])

        if (passedArg != null) {
            scope.addArgumentValue(passedArg, passedValue)
        }

        def argValue = scope.getConfiguredValue(arg)

        then:
        argValue.value == expectedValue
        argValue.getProvidedValue().getActualKey() == expectedActualKey
        argValue.getProvidedValue().getRequestedKey() == expectedRequestedKey
        argValue.getProvidedValue().getSourceDescription() == expectedSource
        argValue.getValue() == scope.getArgumentValue(arg)

        Scope.exit(scopeId)

        where:
        argumentName        | defaultValue               | passedArg              | passedValue       | expectedValue          | expectedActualKey                        | expectedSource              | expectedRequestedKey
        "arg1"              | null                       | "arg1"                 | "arg 1"           | "arg 1"                | "arg1"                                   | "Command argument"          | "liquibase.command.mock.arg1"
        "unsetArg"          | null                       | null                   | null              | null                   | "liquibase.command.mock.unsetArg"        | "No configured value found" | "liquibase.command.mock.unsetArg"
        "argWithDefault"    | "default value"            | null                   | null              | "default value"        | "liquibase.command.mock.argWithDefault"  | "Default value"             | "liquibase.command.mock.argWithDefault"
        "setArgWithDefault" | "overridden default value" | "setArgWithDefault"    | "set arg value"   | "set arg value"        | "setArgWithDefault"                      | "Command argument"          | "liquibase.command.mock.setArgWithDefault"
        "argSetFromScope"   | "default value"            | null                   | null              | "value from scope"     | "liquibase.command.mock.argSetFromScope" | "Scoped value"              | "liquibase.command.mock.argSetFromScope"
        "argSetFromScope"   | null                       | null                   | null              | "value from scope"     | "liquibase.command.mock.argSetFromScope" | "Scoped value"              | "liquibase.command.mock.argSetFromScope"
        "otherArgInScope"   | null                       | null                   | null              | "other value in scope" | "liquibase.command.otherArgInScope"      | "Scoped value"              | "liquibase.command.otherArgInScope"
        "otherArgInScope"   | "default value"            | null                   | null              | "other value in scope" | "liquibase.command.otherArgInScope"      | "Scoped value"              | "liquibase.command.otherArgInScope"
        "argSavedKabobCase" | null                       | "arg-saved-kabob-case" | "kabob value"     | "kabob value"          | "arg-saved-kabob-case"                   | "Command argument"          | "liquibase.command.mock.argSavedKabobCase"
        "argSavedUpperCase" | null                       | "ARGSAVEDUPPERCASE"    | "uppercase value" | "uppercase value"      | "ARGSAVEDUPPERCASE"                      | "Command argument"          | "liquibase.command.mock.argSavedUpperCase"
    }

    def "constructor fails for unknown commands"() {
        when:
        new CommandScope("invalid", "command")

        then:
        def e = thrown(IllegalArgumentException)
        e.message == "Unknown command 'invalid command'"
    }

    def "execute"() {
        when:
        def output = new ByteArrayOutputStream()
        def scope = new CommandScope("mock")
        scope.setOutput(output)

        MockCommandStep.logic = new MockCommandStep() {
            @Override
            void run(CommandResultsBuilder resultsBuilder) throws Exception {
                resultsBuilder.getOutputStream().write("Sent output".getBytes());
                resultsBuilder.addResult("result1", "result #1")
                resultsBuilder.addResult("result2", "result #2")
            }
        }

        def results = scope.execute()

        then:
        output.toString() == "Sent output"
        results.getResults() == [
                result1: "result #1",
                result2: "result #2"
        ]
    }

    def "execute with failing argument validation"() {
        when:
        def output = new ByteArrayOutputStream()

        new CommandBuilder([["mock"]] as String[][]).argument("requiredArg", String).required().build()
        new CommandBuilder([["mock"]] as String[][]).argument("optionalArg", String).optional().build()

        def scope = new CommandScope("mock")
        scope.setOutput(output)
        scope.execute()

        then:
        def e = thrown(CommandValidationException)
        e.message == "Invalid argument 'requiredArg': missing required argument"
    }

    def "clearCredentialArguments redacts credential-bearing keys but leaves others"() {
        // CWE-316 regression: credential-bearing argument values must be overwritten
        // with "*****" so the original Strings become GC-eligible and don't linger
        // in heap for the rest of the JVM lifetime. Non-credential keys (username,
        // url, driver) must pass through unchanged so post-execution diagnostics
        // still have context. Matching is case-insensitive and substring-based on
        // the lowercased key.
        given:
        def scope = new CommandScope("mock")
        scope.addArgumentValue("password",                "supersecret-pw-12345")
        scope.addArgumentValue("apiSecret",               "secret-svc-token")
        scope.addArgumentValue("authToken",               "bearer-xyz")
        scope.addArgumentValue("AccessKey",               "AKIA-EXAMPLE")
        scope.addArgumentValue("PASSWORD",                "case-insensitive-match")
        scope.addArgumentValue("ldap.passwd",             "embedded-token-match")
        // CommandScope receives liquibaseProLicenseKey via Main.createLiquibaseCommand()
        // putting it into argsMap. Lowercase form "liquibaseprolicensekey" contains
        // the "licensekey" token, so it must be redacted alongside passwords / secrets.
        scope.addArgumentValue("liquibaseProLicenseKey",  "PRO-LICENSE-KEY-VALUE-12345")
        scope.addArgumentValue("username",                "regular-user")
        scope.addArgumentValue("url",                     "jdbc:postgresql://host:5432/db")
        scope.addArgumentValue("driver",                  "org.postgresql.Driver")

        when:
        scope.clearCredentialArguments()
        def values = scope.@argumentValues

        then:
        values["password"]               == "*****"
        values["apiSecret"]              == "*****"
        values["authToken"]              == "*****"
        values["AccessKey"]              == "*****"
        values["PASSWORD"]               == "*****"
        values["ldap.passwd"]            == "*****"
        values["liquibaseProLicenseKey"] == "*****"
        values["username"]               == "regular-user"
        values["url"]                    == "jdbc:postgresql://host:5432/db"
        values["driver"]                 == "org.postgresql.Driver"
    }

    def "clearCredentialArguments is a no-op when there are no arguments"() {
        given:
        def scope = new CommandScope("mock")

        when:
        scope.clearCredentialArguments()

        then:
        noExceptionThrown()
        scope.@argumentValues.isEmpty()
    }

    def "clearCredentialArguments uses Locale.ROOT so Turkish-locale 'I' does not break the ASCII substring match"() {
        // CWE-316 regression for the Turkish-locale "dotless i" bug (per @coderabbitai
        // on PR #7741): under tr-TR, bare String.toLowerCase() converts ASCII 'I' to
        // dotless 'ı' (U+0131), so "argument__APIKEY".toLowerCase() becomes
        // "argument__apıkey" — which does NOT contain the ASCII token "apikey". On a
        // JVM whose default locale is Turkish (or any other locale with non-ASCII
        // lowercasing of ASCII letters), credentials with uppercase-I keys would
        // silently survive execute() unredacted. clearCredentialArguments() must
        // therefore use toLowerCase(Locale.ROOT) explicitly. This test exercises the
        // bug condition directly so the contract is locked in by CI, not just by a
        // comment.
        given:
        Locale savedDefault = Locale.getDefault()
        Locale.setDefault(Locale.forLanguageTag("tr-TR"))

        def scope = new CommandScope("mock")
        scope.addArgumentValue("argument__APIKEY",     "secret-api-key-12345")
        scope.addArgumentValue("argument__PASSWORD",   "secret-pw-67890")
        scope.addArgumentValue("argument__LICENSEKEY", "license-XYZ-ABC")
        scope.addArgumentValue("argument__username",   "regular-user")

        when:
        scope.clearCredentialArguments()
        def values = scope.@argumentValues

        then:
        values["argument__APIKEY"]     == "*****"
        values["argument__PASSWORD"]   == "*****"
        values["argument__LICENSEKEY"] == "*****"
        values["argument__username"]   == "regular-user"

        cleanup:
        Locale.setDefault(savedDefault)
    }

    def "execute does NOT clear credential arguments automatically — opt-in semantics"() {
        // Regression: PR #7741 originally clamped clearCredentialArguments() into
        // execute()'s outer finally to satisfy CWE-316. That broke any caller that
        // re-uses a CommandScope (see PostgreSQLIntegrationTest.testStatusRunDuringUpdate
        // which executes 3× on the same scope: first call succeeded; second/third saw
        // "*****" in argumentValues["password"] and got "FATAL: password authentication
        // failed for user 'lbuser'" from the JDBC driver). The fix moved the clearing
        // out of execute() and made it caller-invoked via the (now public)
        // clearCredentialArguments() method. This spec pins that execute() alone does
        // NOT touch credential argument values, so re-use cases continue to work.
        // Pre-supplies 'requiredArg' so the test is order-independent w.r.t. the
        // 'execute with failing argument validation' spec above which registers
        // requiredArg as required on 'mock'.
        given:
        def scope = new CommandScope("mock")
        scope.addArgumentValue("requiredArg", "anything")
        scope.addArgumentValue("password",    "supersecret-pw-12345")
        scope.addArgumentValue("username",    "regular-user")

        MockCommandStep.logic = new MockCommandStep() {
            @Override
            void run(CommandResultsBuilder resultsBuilder) throws Exception {
                // no-op pipeline body — we are pinning the absence of auto-clearing, not the step.
            }
        }

        when:
        scope.execute()

        then:
        scope.@argumentValues["password"]    == "supersecret-pw-12345"
        scope.@argumentValues["requiredArg"] == "anything"
        scope.@argumentValues["username"]    == "regular-user"
    }

    def "execute called twice on the same scope reads original credentials both times — regression for PostgreSQLIntegrationTest.testStatusRunDuringUpdate"() {
        // The canary scenario that surfaced the auto-clear regression. A caller
        // creates a CommandScope, executes it (e.g., 'status'), then re-executes
        // the SAME scope (e.g., 'update', or 'status' again after a concurrent
        // update). With the old auto-clear in execute()'s finally, the second
        // execute() would see argumentValues["password"] == "*****" and the JDBC
        // driver would reject the connection with "password authentication failed".
        // Post-fix: both executions see the original credential.
        given:
        def scope = new CommandScope("mock")
        scope.addArgumentValue("requiredArg", "anything")
        scope.addArgumentValue("password",    "supersecret-pw-12345")

        def passwordsObservedByPipeline = []
        MockCommandStep.logic = new MockCommandStep() {
            @Override
            void run(CommandResultsBuilder resultsBuilder) throws Exception {
                // Capture what the pipeline would have read at execute() time.
                passwordsObservedByPipeline << resultsBuilder.getCommandScope().@argumentValues["password"]
            }
        }

        when:
        scope.execute()
        scope.execute()
        scope.execute()

        then:
        // All three executions saw the same original credential — none was redacted
        // between calls. This is the contract that re-use callers depend on.
        passwordsObservedByPipeline == ["supersecret-pw-12345", "supersecret-pw-12345", "supersecret-pw-12345"]
        scope.@argumentValues["password"] == "supersecret-pw-12345"
    }

    def "execute does NOT clear credentials even when the pipeline throws — opt-in semantics also apply on the failure path"() {
        // Counterpart to the success-path spec above: under the new opt-in semantics,
        // execute() must NOT auto-clear on exception either. If a caller needs the
        // CWE-316 protection on the failure path, they wrap the execute() call in
        // try-finally and call clearCredentialArguments() themselves (see
        // Main.executeAndClearCredentials()).
        given:
        def scope = new CommandScope("mock")
        scope.addArgumentValue("requiredArg", "anything")
        scope.addArgumentValue("password",    "supersecret-pw-12345")

        MockCommandStep.logic = new MockCommandStep() {
            @Override
            void run(CommandResultsBuilder resultsBuilder) throws Exception {
                throw new RuntimeException("simulated command failure")
            }
        }

        when:
        scope.execute()

        then:
        thrown(liquibase.exception.CommandExecutionException)
        scope.@argumentValues["password"] == "supersecret-pw-12345"
    }

    def "caller-invoked try-finally pattern around execute clears credentials post-execution (CWE-316 wiring documented for CLI integrators)"() {
        // The pattern recommended for CLI / single-use callers: wrap execute() in
        // try-finally and call clearCredentialArguments() in the finally. This spec
        // documents the contract: the pipeline sees the original credential during
        // execute(), and the credential is wiped immediately after the call returns.
        // Main.executeAndClearCredentials() is the canonical implementation.
        given:
        def scope = new CommandScope("mock")
        scope.addArgumentValue("requiredArg", "anything")
        scope.addArgumentValue("password",    "supersecret-pw-12345")

        def passwordSeenByPipeline = null
        MockCommandStep.logic = new MockCommandStep() {
            @Override
            void run(CommandResultsBuilder resultsBuilder) throws Exception {
                passwordSeenByPipeline = resultsBuilder.getCommandScope().@argumentValues["password"]
            }
        }

        when:
        try {
            scope.execute()
        } finally {
            scope.clearCredentialArguments()
        }

        then:
        passwordSeenByPipeline == "supersecret-pw-12345"
        scope.@argumentValues["password"] == "*****"
    }

    def "ValueModifiers are used in getArgumentValue"() {
        when:
        def valueModifier = new ConfiguredValueModifier<String>() {
            @Override
            int getOrder() {
                return 5
            }

            @Override
            void override(ConfiguredValue<String> object) {
                object.override("MODIFIED!", "Mock Modifier")
            }
        }
        Scope.currentScope.getSingleton(ConfiguredValueModifierFactory).register(valueModifier)

        def scope = new CommandScope("mock")
        scope.addArgumentValue(MockCommandStep.VALUE_1_ARG, "Original Value")

        then:
        scope.getArgumentValue(MockCommandStep.VALUE_1_ARG) == "MODIFIED!"

        Scope.currentScope.getSingleton(ConfiguredValueModifierFactory).unregister(valueModifier)

    }
}
