package liquibase.actionlogic

import liquibase.JUnitScope
import liquibase.Scope
import liquibase.action.UpdateSqlAction
import liquibase.action.core.CreateSequenceAction
import liquibase.action.core.DropSequenceAction
import liquibase.sdk.database.MockDatabase
import liquibase.test.JUnitResourceAccessor
import spock.lang.Specification

class ActionLogicFactoryTest extends Specification {

    ActionLogicFactory emptyLogicFactory
    Scope testScope;

    def setup() {
        testScope = JUnitScope.instance

        emptyLogicFactory = new ActionLogicFactory(testScope) {
            @Override
            protected Class<? extends ActionLogic>[] findAllServiceClasses(Scope scope) {
                return new Class[0];
            }

            @Override
            protected TemplateActionLogic[] getTemplateActionLogic(Scope scope) {
                return new TemplateActionLogic[0];
            }
        }
    }

    def "getActionLogic when empty"() {
        expect:
        emptyLogicFactory.getActionLogic(new UpdateSqlAction("some sql"), JUnitScope.instance) == null
    }

    def "getActionLogic"() {
        when:
        emptyLogicFactory.register(new MockActionLogic("create 1", 1, CreateSequenceAction))
        emptyLogicFactory.register(new MockActionLogic("create 2", 2, CreateSequenceAction))

        emptyLogicFactory.register(new MockActionLogic("drop 3", 3, DropSequenceAction))
        emptyLogicFactory.register(new MockActionLogic("drop 2", 2, DropSequenceAction))
        emptyLogicFactory.register(new MockActionLogic("drop 1", 1, DropSequenceAction))

        def scope = JUnitScope.getInstance(new MockDatabase())

        then:
        emptyLogicFactory.getActionLogic(new CreateSequenceAction(), scope).toString() == "Mock action logic 'create 2'"
        emptyLogicFactory.getActionLogic(new DropSequenceAction(), scope).toString() == "Mock action logic 'drop 3'"
        emptyLogicFactory.getActionLogic(new UpdateSqlAction("some sql"), scope) == null

    }

    def "Automatically finds action classes"() {
        expect:
        JUnitScope.instance.getSingleton(ActionLogicFactory).registry.size() > 0
    }

//    enable if/when we create .logic files
//    def "Automatically registers TemplateActionLogic instances based on .logic files"() {
//        expect:
//        JUnitScope.instance.getSingleton(ActionLogicFactory).registry.findAll({it.getClass() == TemplateActionLogic}).size() > 0
//    }
}
