package liquibase.actionlogic

import liquibase.Scope
import liquibase.action.UpdateSqlAction
import liquibase.action.core.CreateSequenceAction
import liquibase.action.core.DropSequenceAction
import spock.lang.Specification

class ActionLogicFactoryTest extends Specification {

    ActionLogicFactory emptyLogicFactory

    def setup() {
        emptyLogicFactory = new ActionLogicFactory() {
            @Override
            protected Class<? extends ActionLogic>[] getActionLogicClasses() {
                return new Class[0];
            }
        }
    }

    def "getActionLogic when empty"() {
        expect:
        emptyLogicFactory.getActionLogic(new UpdateSqlAction("some sql"), new Scope(new HashMap<String, Object>())) == null
    }

    def "getActionLogic"() {
        when:
        emptyLogicFactory.register(new MockActionLogic("create 1", 1, CreateSequenceAction))
        emptyLogicFactory.register(new MockActionLogic("create 2", 2, CreateSequenceAction))

        emptyLogicFactory.register(new MockActionLogic("drop 3", 3, DropSequenceAction))
        emptyLogicFactory.register(new MockActionLogic("drop 2", 2, DropSequenceAction))
        emptyLogicFactory.register(new MockActionLogic("drop 1", 1, DropSequenceAction))

        then:
        emptyLogicFactory.getActionLogic(new CreateSequenceAction(), new Scope(new HashMap<String, Object>())).toString() == "Mock action logic 'create 2'"
        emptyLogicFactory.getActionLogic(new DropSequenceAction(), new Scope(new HashMap<String, Object>())).toString() == "Mock action logic 'drop 3'"
        emptyLogicFactory.getActionLogic(new UpdateSqlAction("some sql"), new Scope(new HashMap<String, Object>())) == null

    }
}
