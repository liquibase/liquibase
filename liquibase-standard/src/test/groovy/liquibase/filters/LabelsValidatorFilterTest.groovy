package liquibase.filters

import liquibase.GlobalConfiguration
import liquibase.changelog.RawChangeSet
import liquibase.changelog.filter.propertyvalidator.LabelsValidatorFilter
import spock.lang.Specification
import spock.lang.Unroll
import liquibase.Scope

class LabelsValidatorFilterTest extends Specification {

    @Unroll
    def "validate labels filter does not accept #label value while on STRICT mode"() {
        when:
        def changeSet = new RawChangeSet("testId", "testAuthor", "testFile")
        changeSet.setLabels(label)
        def validatorResult

        Scope.child(GlobalConfiguration.STRICT.getKey(), true, () -> {
            validatorResult = new LabelsValidatorFilter().accepts(changeSet)
        })

        then:
        validatorResult !=null
        validatorResult.isAccepted() == false
        validatorResult.getMessage() == "labels value cannot be empty while on Strict mode"


        where:
        label | _
        ""    | _
        "   " | _
    }



}
