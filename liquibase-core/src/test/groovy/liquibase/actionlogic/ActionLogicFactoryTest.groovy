package liquibase.actionlogic

import liquibase.Scope
import liquibase.action.UpdateSqlAction
import liquibase.action.core.CreateSequenceAction
import liquibase.action.core.DropSequenceAction
import liquibase.test.JUnitResourceAccessor
import spock.lang.Specification

class ActionLogicFactoryTest extends Specification {

    ActionLogicFactory emptyLogicFactory
    static ActionLogicFactory fullLogicFactory
    Scope testScope;

    def setup() {
        testScope = new Scope(new JUnitResourceAccessor(), new HashMap<String, Object>(["resourceAccessor": new JUnitResourceAccessor()]))

        emptyLogicFactory = new ActionLogicFactory(testScope) {
            @Override
            protected Class<? extends ActionLogic>[] getActionLogicClasses() {
                return new Class[0];
            }

            @Override
            protected TemplateActionLogic[] getTemplateActionLogic(Scope scope) {
                return new TemplateActionLogic[0];
            }
        }

        if (fullLogicFactory == null) {
            fullLogicFactory = new ActionLogicFactory(testScope)
        }
    }

    def "getActionLogic when empty"() {
        expect:
        emptyLogicFactory.getActionLogic(new UpdateSqlAction("some sql"), new Scope(new JUnitResourceAccessor(), new HashMap<String, Object>())) == null
    }

    def "getActionLogic"() {
        when:
        emptyLogicFactory.register(new MockActionLogic("create 1", 1, CreateSequenceAction))
        emptyLogicFactory.register(new MockActionLogic("create 2", 2, CreateSequenceAction))

        emptyLogicFactory.register(new MockActionLogic("drop 3", 3, DropSequenceAction))
        emptyLogicFactory.register(new MockActionLogic("drop 2", 2, DropSequenceAction))
        emptyLogicFactory.register(new MockActionLogic("drop 1", 1, DropSequenceAction))

        then:
        emptyLogicFactory.getActionLogic(new CreateSequenceAction(), new Scope(new JUnitResourceAccessor(), new HashMap<String, Object>())).toString() == "Mock action logic 'create 2'"
        emptyLogicFactory.getActionLogic(new DropSequenceAction(), new Scope(new JUnitResourceAccessor(), new HashMap<String, Object>())).toString() == "Mock action logic 'drop 3'"
        emptyLogicFactory.getActionLogic(new UpdateSqlAction("some sql"), new Scope(new JUnitResourceAccessor(), new HashMap<String, Object>())) == null

    }

    def "Automatically finds action classes"() {
        expect:
        fullLogicFactory.logic.size() > 0
    }

    def "Automatically registers TemplateActionLogic instances based on .logic files"() {
        expect:
        fullLogicFactory.logic.findAll({it.getClass() == TemplateActionLogic}).size() > 0
    }
}
