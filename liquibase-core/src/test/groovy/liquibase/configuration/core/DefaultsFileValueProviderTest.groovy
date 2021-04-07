package liquibase.configuration.core

import spock.lang.Specification
import spock.lang.Unroll

class DefaultsFileValueProviderTest extends Specification {

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
        actualKey           | expected
        "long.key"          | "Long Key"
        "long.multiWord"    | "Long MultiWord"
        "long.kabobCase"    | "Long KabobCase"
        "long.other.key"    | "Long other key"
        "shortKey"          | "Short Key"
        "word"              | "Word"
        "actual.shortKey"   | "Short Key"
        "actual.shortKabob" | "Short kabob"
        "invalid.key"       | null
        "upper.kabob"       | "UPPER KABOB"
    }
}
