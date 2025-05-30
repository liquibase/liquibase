package liquibase.filters

import liquibase.ContextExpression
import liquibase.GlobalConfiguration
import liquibase.Scope
import liquibase.changelog.ChangeSet
import liquibase.changelog.filter.ContextChangeSetFilter
import spock.lang.Specification
import spock.lang.Unroll

class ContextChangeSetFilterTest extends Specification {

    @Unroll
    def "validate context filter does not accept #context value while on STRICT mode"() {
        when:
        def changeSet = new ChangeSet(null)
        changeSet.setContextFilter(new ContextExpression(context))
        def changeSetFilterResult

        Scope.child(GlobalConfiguration.STRICT.getKey(), true, () -> {
            changeSetFilterResult = new ContextChangeSetFilter().accepts(changeSet)
        })

        then:
        changeSetFilterResult !=null
        changeSetFilterResult.isAccepted() == false
        changeSetFilterResult.getMessage() == "context value cannot be empty while on Strict mode"


        where:
        context | _
        ""      | _
        null    | _
        "   "   | _
    }



}
