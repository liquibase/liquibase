package liquibase.configuration.core

import liquibase.configuration.core.MapConfigurationValueProvider
import spock.lang.Specification
import spock.lang.Unroll

class MapConfigurationValueProviderTest extends Specification {

    @Unroll
    def "keyMatches"() {
        expect:
        new MapConfigurationValueProvider([:]).keyMatches(wantedKey, storedKey) == matches

        where:
        wantedKey             | storedKey              | matches
        "single"              | "single"               | true
        "single"              | "SINGLE"               | true
        "single"              | "SiNglE"               | true
        "parent.child"        | "parent.child"         | true
        "parent.child"        | "PARENT.Child"         | true
        "parent.child"        | "PARENT-Child"         | false
        "parent.child"        | "PARENT_Child"         | false
        "parent.bothChildren" | "Parent.BothChildren"  | true
        "parent.bothChildren" | "parent.both-children" | true
        "parent.bothChildren" | "parent.both_children" | false
        "single"              | "invalid"              | false
        "parent.child"        | "parent"               | false
    }

//    def other() {
//        expect:
//        def provider = new MapConfigurationValueProvider([
//                "lower"          : "stored lower",
//                "lower.dot"      : "stored lower dot",
//                "lower_under.dot": "stored lower under dot",
//                "UPPER"          : "stored upper",
//                "UPPER.DOT"      : "stored upper dot",
//                "Mixed.Case"     : "stored mixed case",
//        ])
//
//        where:
//        requestedKey | expectedValue
//
//        "lower" | "saw lower" | "System property 'lower'"
//        "LOWER" | "saw lower" | "System property 'lower'"
//        "upper" | "saw upper" | "System property 'UPPER'"
//        "UPPER" | "saw upper" | "System property 'UPPER'"
//        "lower.underscore" | null | null
//        "upper.dot" | "saw upper dot" | "System property 'UPPER.DOT'"
//        "UPPER.DOT" | "saw upper dot" | "System property 'UPPER.DOT'"
//        "LOWER.UNDER.dot" | null | null
//        "LOWER_UNDER_DOT" | null | null
//        "invalid" | null | null
//        null | null | null
//    }
}
