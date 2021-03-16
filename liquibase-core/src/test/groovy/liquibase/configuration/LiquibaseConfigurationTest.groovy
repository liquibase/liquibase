package liquibase.configuration

import liquibase.Scope
import spock.lang.Specification

class LiquibaseConfigurationTest extends Specification {

    def "getCurrentValueDetails"() {
        when:

        System.setProperty("test.current-value", "From system")
        def currentValue = Scope.child(["test.current-value": "From scope"], new Scope.ScopedRunnerWithReturn<CurrentValueDetails>() {
            @Override
            CurrentValueDetails run() throws Exception {
                return Scope.currentScope.getSingleton(LiquibaseConfiguration).getCurrentValue("test.current-value")
            }
        })

        then:
        currentValue.value == "From scope"
        currentValue.sourceHistory*.describe() == ["Scoped value 'test.current-value'", "System property 'test.current-value'"]
    }

    def "autoRegisters and sorts providers"() {
        expect:
        Scope.getCurrentScope().getSingleton(LiquibaseConfiguration).configurationValueProviders*.getClass()*.getName().contains("liquibase.configuration.core.SystemPropertyValueProvider")
    }

    def "autoRegisters definitions"() {
        expect:
        Scope.getCurrentScope().getSingleton(LiquibaseConfiguration).getRegisteredDefinitions().size() > 10
    }

}
