package liquibase.filters

import liquibase.GlobalConfiguration
import liquibase.Scope
import liquibase.changelog.ChangeSet
import liquibase.changelog.filter.DbmsChangeSetFilter
import liquibase.database.core.MockDatabase
import spock.lang.Specification
import spock.lang.Unroll

class DbmsChangeSetFilterTest extends Specification {

    @Unroll
    def "validate dbms filter does not accept #dbms value while on STRICT mode"() {
        when:
        def changeSet = new ChangeSet(null)
        changeSet.setDbms(dbms)
        def changeSetFilterResult

        Scope.child(GlobalConfiguration.STRICT.getKey(), true, () -> {
            changeSetFilterResult = new DbmsChangeSetFilter(new MockDatabase()).accepts(changeSet)
        })

        then:
        changeSetFilterResult !=null
        changeSetFilterResult.isAccepted() == false
        changeSetFilterResult.getMessage() == "dbms value cannot be empty while on Strict mode"


        where:
        dbms  | _
        ""    | _
        "   " | _
    }



}
