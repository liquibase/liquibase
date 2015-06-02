package liquibase.action

import liquibase.JUnitScope
import liquibase.action.core.AddAutoIncrementAction
import spock.lang.Specification

class TestObjectFactoryTest extends Specification {

    def "creates permutations with no default value"() {
        when:
        def permutations = JUnitScope.instance.getSingleton(TestObjectFactory).createAllPermutations(AddAutoIncrementAction, JUnitScope.instance)
        def permutationsAsStrings = (permutations.collect {it.toString()}).unique()

        then:
        permutations.size() > 10
        permutations.size() == permutationsAsStrings.size()


    }
}
