package liquibase.filters

import liquibase.GlobalConfiguration
import liquibase.Scope
import liquibase.changelog.RawChangeSet
import liquibase.changelog.filter.propertyvalidator.DbmsValidatorFilter
import spock.lang.Specification
import spock.lang.Unroll

class DbmsValidatorFilterTest extends Specification {

    @Unroll
    def "validate dbms filter does not accept #dbms value while on STRICT mode"() {
        when:
        def changeSet = new RawChangeSet("testAuthor", "testChangeSet", "testFile")
        changeSet.setDbms(dbms)
        def validatorResult

        Scope.child(GlobalConfiguration.STRICT.getKey(), true, () -> {
            validatorResult = new DbmsValidatorFilter().accepts(changeSet)
        })

        then:
        validatorResult !=null
        validatorResult.isAccepted() == false
        validatorResult.getMessage().contains("dbms value cannot be empty while on Strict mode")


        where:
        dbms  | _
        ""    | _
        "   " | _
    }

    @Unroll
    def "validate valid #dbms value(s) is(are) accepted"() {
        when:
        def changeSet = new RawChangeSet("testAuthor", "testChangeSet", "testFile")
        changeSet.setDbms(dbms)
        def validatorResult

        Scope.child(GlobalConfiguration.STRICT.getKey(), true, () -> {
            validatorResult = new DbmsValidatorFilter().accepts(changeSet)
        })

        then:
        validatorResult != null
        validatorResult.isAccepted() == true

        where:
        dbms << ["h2, mysql", "mysql", "postgresql", "oracle, mssql,db2,  sqlite"]
    }

    def "valiidate invalid/non-existent dbms value is not accepted"() {
        when:
        def changeSet = new RawChangeSet("testAuthor", "testChangeSet", "testFile")
        changeSet.setDbms("nonExistentDbms")
        def validatorResult

        Scope.child(GlobalConfiguration.STRICT.getKey(), true, () -> {
            validatorResult = new DbmsValidatorFilter().accepts(changeSet)
        })

        then:
        validatorResult != null
        validatorResult.isAccepted() == false
        validatorResult.getMessage().contains("nonExistentDbms is not a valid dbms value")
    }

}
