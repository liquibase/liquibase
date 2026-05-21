package liquibase.configuration.core

import liquibase.Scope
import liquibase.command.CommandScope
import liquibase.configuration.LiquibaseConfiguration
import spock.lang.Specification
import spock.lang.Unroll

class DefaultsFileValueProviderTest extends Specification {

    def "can load from stream"() {
        when:
        def stream = new ByteArrayInputStream("""
long.key: Long Key
long.multiWord: Long MultiWord
""".getBytes())
        def provider = new DefaultsFileValueProvider(stream, "Test stream")

        then:
        provider.getProvidedValue("long.key").getValue() == "Long Key"
        provider.getProvidedValue("long.multiWord").getValue() == "Long MultiWord"
    }

    @Unroll
    def "getProvidedValue"() {
        setup:
        def provider = new DefaultsFileValueProvider([
                "long.key"       : "Long Key",
                "long.multiWord" : "Long MultiWord",
                "long.kabob-case": "Long KabobCase",
                "long.other.key" : "Long other key",
                "shortKey"       : "Short Key",
                "word"           : "Word",
                "short-kabob"    : "Short kabob",
                "UPPER-KABOB"    : "UPPER KABOB",
        ] as Properties)

        expect:
        provider.getProvidedValue(actualKey)?.getValue() == expected

        where:
        actualKey                      | expected
        "long.key"                     | "Long Key"
        "long.multiWord"               | "Long MultiWord"
        "long.kabobCase"               | "Long KabobCase"
        "long.other.key"               | "Long other key"
        "shortKey"                     | "Short Key"
        "word"                         | "Word"
        "liquibase.shortKey"           | "Short Key"
        "liquibase.command.shortKey"   | "Short Key"
        "liquibase.shortKabob"         | "Short kabob"
        "liquibase.command.shortKabob" | "Short kabob"
        "invalid.key"                  | null
        "upper.kabob"                  | "UPPER KABOB"
    }

    @Unroll("#featureName: #key")
    def "validate valid values"() {
        when:
        def provider = new DefaultsFileValueProvider([
                (key) : "test value"
        ] as Properties)

        //
        // Set the strict setting in the Scope for this test
        //
        LiquibaseConfiguration configuration = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class)
        configuration.registerProvider(new ScopeValueProvider())

        Map<String, String> scopeValues = ["liquibase.strict":strict] as Map<String, String>
        Scope.getCurrentScope().child(scopeValues, (Scope.ScopedRunner) { ->
            provider.validate(new CommandScope("update"))
        })

        then:
        noExceptionThrown()

        where:
        key                                      | strict
        "invalid"                                | false
        "logLevel"                               | true
        "log-level"                              | true
        "liquibase.logLevel"                     | true
        "liquibase.LOGLEVEL"                     | true
        "url"                                    | true
        "changelogFile"                          | true
        "changeLogFile"                          | true
        "changelog-file"                         | true
        "liquibase.command.update.changelogFile" | true
        "liquibase.command.changelogFile"        | true
        "parameter.my-property"                  | true
        "liquibase.command.invalid"              | false
        "liquibase.invalid"                      | false
        "external.config"                        | false
    }

    @Unroll("#featureName: #key")
    def "validate invalid values"() {
        when:
        def provider = new DefaultsFileValueProvider([
                (key) : "test value"
        ] as Properties)

        //
        // Set the strict setting in the Scope for this test
        //
        LiquibaseConfiguration configuration = Scope.getCurrentScope().getSingleton(LiquibaseConfiguration.class)
        configuration.registerProvider(new ScopeValueProvider())

        Map<String, String> scopeValues = ["liquibase.strict":"true"]
        Scope.getCurrentScope().child(scopeValues, (Scope.ScopedRunner) { ->
            provider.validate(new CommandScope("update"))
        })

        then:
        def e = thrown(IllegalArgumentException)
        e.message.contains("\n - '$key'\n")


        where:
        key                         | notes
        "invalid"                   | ""
        "force"                     | "not for this command"
        "liquibase.invalid"         | ""
        "liquibase.command.invalid" | ""
        "external.config"           | ""
    }

    @Unroll
    def "defaults-file loader keeps \${...} placeholders verbatim — CWE-94 no-expansion contract: #variant"() {
        // CWE-94 regression: pairs with the guard comments inside DefaultsFileValueProvider.
        // A comment can be deleted without CI signal (per @wwillard7800's review on #7744);
        // this spec pins the runtime contract so any future change that introduces ${...}
        // expansion (and silently removes the guard comment) trips a CI failure rather
        // than passing silently. CVE-2022-33980 in Apache Commons Configuration's
        // StringSubstitutor is the canonical precedent — interpolation in a defaults
        // loader became an RCE / info-disclosure vector once StringSubstitutor was wired
        // into the load path. Bare java.util.Properties.load is the chosen mitigation.
        given:
        def stream = new ByteArrayInputStream(("placeholder=" + placeholder + "\n").getBytes("UTF-8"))

        when:
        def provider = new DefaultsFileValueProvider(stream, "Test no-expansion")

        then:
        provider.getProvidedValue("placeholder").getValue() == placeholder

        where:
        variant                                  | placeholder
        "env-var lookup"                         | '${env:HOME}'
        "credential exfiltration via env-var"    | '${env:AWS_SECRET_ACCESS_KEY}'
        "file-inclusion / arbitrary read"        | '${file:/etc/passwd}'
        "key reference / property chaining"      | '${other.key}'
        "nested / recursive expansion"           | '${outer${inner}}'
        "empty placeholder"                      | '${}'
        "placeholder embedded inside a JDBC URL" | 'jdbc:postgresql://host:5432/${dbname}'
    }
}
