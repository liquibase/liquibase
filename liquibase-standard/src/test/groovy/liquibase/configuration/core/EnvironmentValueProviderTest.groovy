package liquibase.configuration.core

import liquibase.GlobalConfiguration
import liquibase.Scope
import liquibase.command.CommandScope
import liquibase.command.core.UpdateCommandStep
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
        input                                                | expected
        GlobalConfiguration.CHANGELOGLOCK_POLL_RATE.getKey() | "5"
        "LIQUIBASE_PRO_MARK_UNUSED_NOT_DROP"                 | "true"

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

    def "should see aliases as valid"() {
        given:
        def provider = new EnvironmentValueProvider() {
            @Override
            protected Map<?, ?> getMap() {
                return [
                        "LIQUIBASE_COMMAND_CONTEXTS": "some-context",
                        "LIQUIBASE_COMMAND_LABELS"  : "some-label"
                ]
            }
        }

        when:
        // Set strict to true to throw errors on unrecognized values
        Map<String, String> scopeValues = ["liquibase.strict": "true"]
        Scope.getCurrentScope().child(scopeValues, (Scope.ScopedRunner) { ->
            provider.validate(new CommandScope(UpdateCommandStep.COMMAND_NAME))
        })
        then:
        noExceptionThrown()
    }
}
