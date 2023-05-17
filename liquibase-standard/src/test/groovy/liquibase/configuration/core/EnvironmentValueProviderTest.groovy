package liquibase.configuration.core

import liquibase.GlobalConfiguration
import spock.lang.Specification
import spock.lang.Unroll

class EnvironmentValueProviderTest extends Specification {

    @Unroll
    def "getProvidedValue"() {
        when:
        def provider = new EnvironmentValueProvider() {
            @Override
            protected Map<?, ?> getMap() {
                return [
                        "LIQUIBASE_CHANGELOG_LOCK_POLL_RATE": "5",
                        "LIQUIBASE_PRO_MARK_UNUSED_NOT_DROP": "true",
                ]
            }
        }

        then:
        provider.getProvidedValue(input).value == expected

        where:
        input                                                   | expected
        GlobalConfiguration.CHANGELOGLOCK_POLL_RATE.getKey()    | "5"
        "LIQUIBASE_PRO_MARK_UNUSED_NOT_DROP"                    | "true"

    }

    @Unroll
    def "keyMatches"() {
        expect:
        assert new EnvironmentValueProvider().keyMatches(wantedKey, storedKey) == matches

        where:
        wantedKey        | storedKey         | matches
        "single"         | "SINGLE"          | true
        "parent.child"   | "parent.CHILD"    | true
        "parent.child"   | "parent-CHILD"    | true
        "parent.child"   | "parent_CHILD"    | true
        "parent.twoWord" | "parent_two_word" | true
        "parent.twoWord" | "parent_twoword"  | false
        "invalid"        | "parent_child"    | false
        "no.space"       | "no_space "       | true
    }
}