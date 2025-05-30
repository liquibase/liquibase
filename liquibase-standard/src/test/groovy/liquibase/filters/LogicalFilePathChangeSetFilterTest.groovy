package liquibase.filters

import liquibase.GlobalConfiguration
import liquibase.Scope
import liquibase.changelog.ChangeSet
import liquibase.changelog.filter.LogicalFilePathChangeSetFilter
import spock.lang.Specification
import spock.lang.Unroll

class LogicalFilePathChangeSetFilterTest extends Specification {

    @Unroll
    def "validate logicalFilePath filter does not accept #logicalFilePath value while on STRICT mode"() {
        when:
        def changeSet = new ChangeSet(null)
        changeSet.setLogicalFilePath(logicalFilePath)
        def changeSetFilterResult

        Scope.child(GlobalConfiguration.STRICT.getKey(), true, () -> {
            changeSetFilterResult = new LogicalFilePathChangeSetFilter().accepts(changeSet)
        })

        then:
        changeSetFilterResult !=null
        changeSetFilterResult.isAccepted() == false
        changeSetFilterResult.getMessage() == "logicalFilePath value cannot be empty while on Strict mode"


        where:
        logicalFilePath | _
        ""    | _
        null  | _
        "   " | _
    }



}
