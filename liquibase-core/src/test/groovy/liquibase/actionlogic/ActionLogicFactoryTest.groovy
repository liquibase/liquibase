package liquibase.actionlogic

import liquibase.JUnitScope
import liquibase.Scope
import liquibase.action.UpdateSqlAction
import liquibase.action.core.CreateSequenceAction
import liquibase.action.core.DropSequenceAction
import liquibase.exception.ServiceNotFoundException
import liquibase.sdk.database.MockDatabase
import liquibase.servicelocator.ServiceLocator
import liquibase.test.JUnitResourceAccessor
import spock.lang.Specification

class ActionLogicFactoryTest extends Specification {

    ServiceLocator emptyLogicFactory
    Scope testScope;
    List<ActionLogic> emptyLogicFactoryServices;

    def setup() {
        emptyLogicFactory = new ServiceLocator() {

            @Override
            def <T> Iterator<T> findAllServices(Class<T> requiredInterface) throws ServiceNotFoundException {
                return emptyLogicFactoryServices.iterator()
            }

        }
        emptyLogicFactoryServices = []
        testScope = JUnitScope.instance.overrideSingleton(ServiceLocator, emptyLogicFactory)

    }

    def "getActionLogic when empty"() {
        expect:
        testScope.getSingleton(ActionLogicFactory).getActionLogic(new UpdateSqlAction("some sql"), JUnitScope.instance) == null
    }

    def "getActionLogic"() {
        when:
        emptyLogicFactoryServices.add(new MockActionLogic("create 1", 1, CreateSequenceAction))
        emptyLogicFactoryServices.add(new MockActionLogic("create 2", 2, CreateSequenceAction))

        emptyLogicFactoryServices.add(new MockActionLogic("drop 3", 3, DropSequenceAction))
        emptyLogicFactoryServices.add(new MockActionLogic("drop 2", 2, DropSequenceAction))
        emptyLogicFactoryServices.add(new MockActionLogic("drop 1", 1, DropSequenceAction))

        def scope = testScope.child(Scope.Attr.database, new MockDatabase())

        then:
        scope.getSingleton(ActionLogicFactory).getActionLogic(new CreateSequenceAction(), scope).toString() == "Mock action logic 'create 2'"
        scope.getSingleton(ActionLogicFactory).getActionLogic(new DropSequenceAction(), scope).toString() == "Mock action logic 'drop 3'"
        scope.getSingleton(ActionLogicFactory).getActionLogic(new UpdateSqlAction("some sql"), scope) == null

    }

    def "Automatically finds action classes"() {
        expect:
        JUnitScope.instance.getSingleton(ServiceLocator).findAllServices(ActionLogic.class).hasNext()
    }
}
