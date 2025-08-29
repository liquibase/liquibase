package liquibase.filters

import liquibase.GlobalConfiguration
import liquibase.Scope
import liquibase.validator.RawChangeSet
import liquibase.validator.propertyvalidator.RunWithValidatorFilter
import spock.lang.Specification
import spock.lang.Unroll

class RunWithValidatorFilterTest extends Specification {

    @Unroll
    def "validate runWith filter does not accept #runWith value while on STRICT mode"() {
        when:
        def changeSet = new RawChangeSet("testId", "testAuthor", "testFile")
        changeSet.setRunWith(runWith)
        def validatorResult

        Scope.child(GlobalConfiguration.STRICT.getKey(), true, () -> {
            validatorResult = new RunWithValidatorFilter().accepts(changeSet)
        })

        then:
        validatorResult !=null
        validatorResult.isAccepted() == false
        validatorResult.getMessage().contains("runWith value cannot be empty while on Strict mode")


        where:
        runWith | _
        ""    | _
        "   " | _
    }

    @Unroll
    def "validate valid #runWith value is accepted"() {
        when:
        def changeSet = new RawChangeSet("testAuthor", "testChangeSet", "testFile")
        changeSet.setRunWith(runWith)
        def validatorResult

        Scope.child(GlobalConfiguration.STRICT.getKey(), true, () -> {
            validatorResult = new RunWithValidatorFilter().accepts(changeSet)
        })

        then:
        validatorResult != null
        validatorResult.isAccepted() == true

        where:
        runWith << ["jdbc"]
    }

    def "valiidate invalid/non-existent runWith value is not accepted"() {
        when:
        def changeSet = new RawChangeSet("testAuthor", "testChangeSet", "testFile")
        changeSet.setRunWith("nonExistentRunWith")
        def validatorResult

        Scope.child(GlobalConfiguration.STRICT.getKey(), true, () -> {
            validatorResult = new RunWithValidatorFilter().accepts(changeSet)
        })

        then:
        validatorResult != null
        validatorResult.isAccepted() == false
        validatorResult.getMessage().contains("nonExistentRunWith is not a valid runWith value")
    }



}
