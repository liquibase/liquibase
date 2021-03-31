package liquibase.configuration.core

import liquibase.configuration.AbstractMapConfigurationValueProvider
import spock.lang.Specification
import spock.lang.Unroll

/**
 * Tests the base {@link AbstractMapConfigurationValueProvider} logic. Can't call this
 * AbstractMapConfigurationValueProviderTest or maven won't pick it up
 */
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

    static class MapConfigurationValueProvider extends AbstractMapConfigurationValueProvider {

        private final Map<?, ?> map

        MapConfigurationValueProvider(Map<?, ?> map) {
            this.map = map
        }

        @Override
        int getPrecedence() {
            return -1
        }

        @Override
        protected String getSourceDescription() {
            return "Test map"
        }

        @Override
        protected Map<?, ?> getMap() {
            return map
        }
    }

}
