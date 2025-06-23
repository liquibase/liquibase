package liquibase.filters

import liquibase.GlobalConfiguration
import liquibase.Labels
import liquibase.changelog.ChangeSet
import spock.lang.Specification
import spock.lang.Unroll
import liquibase.changelog.filter.LabelChangeSetFilter
import liquibase.Scope

class LabelChangeSetFilterTest extends Specification {

    @Unroll
    def "validate labels filter does not accept #label value while on STRICT mode"() {
        when:
        def changeSet = new ChangeSet(null)
        changeSet.setLabels(new Labels(label))
        def changeSetFilterResult

        Scope.child(GlobalConfiguration.STRICT.getKey(), true, () -> {
            changeSetFilterResult = new LabelChangeSetFilter().accepts(changeSet)
        })

        then:
        changeSetFilterResult !=null
        changeSetFilterResult.isAccepted() == false
        changeSetFilterResult.getMessage() == "labels value cannot be empty while on Strict mode"


        where:
        label | _
        ""    | _
        "   " | _
    }



}
