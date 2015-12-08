package liquibase.servicelocator

import liquibase.JUnitScope
import liquibase.actionlogic.ActionLogic
import spock.lang.Specification

public class ServiceLocatorTest extends Specification {

    def "findAllServices when there are valid services"() {
        expect:
        assert JUnitScope.instance.getSingleton(ServiceLocator).findAllServices(ActionLogic).iterator().hasNext()
    }

    def "findAllServices when there no valid services"() {
        expect:
        assert !JUnitScope.instance.getSingleton(ServiceLocator).findAllServices(ServiceLocatorTest).iterator().hasNext()
    }
}
