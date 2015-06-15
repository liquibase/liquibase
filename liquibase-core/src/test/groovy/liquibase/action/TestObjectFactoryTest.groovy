package liquibase.action

import liquibase.JUnitScope
import liquibase.action.core.AddAutoIncrementAction
import spock.lang.Specification

class TestObjectFactoryTest extends Specification {

    def "createAllPermutations"() {
        when:
        def permutations = JUnitScope.instance.getSingleton(TestObjectFactory).createAllPermutations(AddAutoIncrementAction, [
                columnName: null,
                columnDataType: null,
                startWith  : [1, 2, 10],
                incrementBy: [1, 5, 20]
        ])

        then:
        permutations.size() > 10
        permutations*.toString().contains("addAutoIncrement()") //empty permutation is included
        permutations*.toString().sort() == (permutations.collect { it.toString() }).unique().sort() //nothing is duplicated
    }
}
