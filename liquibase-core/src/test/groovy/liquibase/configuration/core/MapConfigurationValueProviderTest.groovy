package liquibase.configuration.core

import liquibase.configuration.core.MapConfigurationValueProvider
import spock.lang.Specification
import spock.lang.Unroll

class MapConfigurationValueProviderTest extends Specification {

    def "empty values count as not set"() {
        when:
        def provider = new MapConfigurationValueProvider(["empty.property":""])

        then:
        provider.getProvidedValue("empty.property") == null
        provider.getProvidedValue("empty-property") == null
    }

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
}
