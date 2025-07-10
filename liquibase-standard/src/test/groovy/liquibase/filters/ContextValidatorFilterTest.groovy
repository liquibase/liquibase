package liquibase.filters

import liquibase.GlobalConfiguration
import liquibase.Scope
import liquibase.changelog.RawChangeSet
import liquibase.changelog.filter.propertyvalidator.ContextValidatorFilter
import spock.lang.Specification
import spock.lang.Unroll

class ContextValidatorFilterTest extends Specification {

    @Unroll
    def "validate context filter does not accept #context value while on STRICT mode"() {
        when:
        def changeSet = new RawChangeSet("testAuthor", "testChangeSet", "testFile");
        changeSet.setContexts(context)
        def validatorResult

        Scope.child(GlobalConfiguration.STRICT.getKey(), true, () -> {
            validatorResult = new ContextValidatorFilter().accepts(changeSet)
        })

        then:
        validatorResult !=null
        validatorResult.isAccepted() == false
        validatorResult.getMessage().contains("context value cannot be empty while on Strict mode")


        where:
        context | _
        ""      | _
        "   "   | _
    }



}
