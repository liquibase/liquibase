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

    def "execute invokes credential clearing in its outer finally on the success path"() {
        // CWE-316 wiring check: confirms the outer finally added to execute()
        // actually calls clearCredentialArguments(). Pre-supplies the 'requiredArg'
        // value so this test is order-independent w.r.t. the 'execute with failing
        // argument validation' spec that registers requiredArg as required on
        // 'mock' (Spock test order in a single Specification is declaration-order,
        // but the side effect of CommandBuilder is global to the JVM).
        given:
        def scope = new CommandScope("mock")
        scope.addArgumentValue("requiredArg", "anything")
        scope.addArgumentValue("password",    "supersecret-pw-12345")
        scope.addArgumentValue("username",    "regular-user")

        MockCommandStep.logic = new MockCommandStep() {
            @Override
            void run(CommandResultsBuilder resultsBuilder) throws Exception {
                // no-op pipeline body — we are testing the finally-block wiring, not the step.
            }
        }

        when:
        scope.execute()

        then:
        scope.@argumentValues["password"]    == "*****"
        scope.@argumentValues["requiredArg"] == "anything"
        scope.@argumentValues["username"]    == "regular-user"
    }

    def "execute invokes credential clearing in its outer finally even when the pipeline throws"() {
        // CWE-316 wiring check: the outer finally must run on the failure path too.
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
