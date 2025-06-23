package liquibase.filters

import liquibase.GlobalConfiguration
import liquibase.Scope
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.changelog.filter.RunWithChangeSetFilter
import liquibase.sdk.resource.MockResourceAccessor
import spock.lang.Specification
import spock.lang.Unroll

class RunWithChangeSetFilterTest extends Specification {

    @Unroll
    def "validate runWith filter does not accept #runWith value while on STRICT mode"() {
        when:
        def changeSet
        def changeSetFilterResult

        Scope.child(GlobalConfiguration.STRICT.getKey(), true, () -> {
            changeSet = new ChangeSet("testWithStrict", "mallod", false, false, "test1.xml", null, null, null)
            changeSet.setRunWith(runWith)
            changeSetFilterResult = new RunWithChangeSetFilter().accepts(changeSet)
        })

        then:
        changeSetFilterResult !=null
        changeSetFilterResult.isAccepted() == false
        changeSetFilterResult.getMessage() == "runWith value cannot be empty while on Strict mode"


        where:
        runWith | _
        ""    | _
        "   " | _
    }



}
