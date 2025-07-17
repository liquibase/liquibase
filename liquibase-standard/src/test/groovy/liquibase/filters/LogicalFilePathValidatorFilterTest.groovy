package liquibase.filters

import liquibase.GlobalConfiguration
import liquibase.Scope
import liquibase.validator.RawChangeSet
import liquibase.validator.propertyvalidator.LogicalFilePathValidatorFilter
import spock.lang.Specification
import spock.lang.Unroll

class LogicalFilePathValidatorFilterTest extends Specification {

    @Unroll
    def "validate logicalFilePath filter does not accept #logicalFilePath value while on STRICT mode"() {
        when:
        def changeSet = new RawChangeSet("testId", "testAuthor", "testFile")
        changeSet.setLogicalFilePath(logicalFilePath)
        def validatorResult

        Scope.child(GlobalConfiguration.STRICT.getKey(), true, () -> {
            validatorResult = new LogicalFilePathValidatorFilter().accepts(changeSet)
        })

        then:
        validatorResult !=null
        validatorResult.isAccepted() == false
        validatorResult.getMessage() == "logicalFilePath value cannot be empty while on Strict mode"


        where:
        logicalFilePath | _
        ""    | _
        "   " | _
    }



}
