package liquibase.configuration.core

import liquibase.command.CommandScope
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
                (key) : "test value",
                strict: String.valueOf(strict)
        ] as Properties)

        provider.validate(new CommandScope("update"))

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
                (key) : "test value",
                strict: "true",
        ] as Properties)

        provider.validate(new CommandScope("update"))

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
}
