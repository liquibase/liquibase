package liquibase.actionlogic

import liquibase.JUnitEmptyScope
import liquibase.Scope
import liquibase.action.*
import liquibase.database.jvm.MockJdbcConnection
import liquibase.exception.ActionPerformException
import liquibase.exception.ValidationErrors
import liquibase.sdk.database.MockDatabase
import liquibase.test.JUnitResourceAccessor
import spock.lang.Specification

class ActionExecutorTest extends Specification {

    Scope scope;

    def setup() {
        scope = JUnitEmptyScope.getNewInstance(new MockDatabase()).child(["liquibase.actionlogic.ActionLogicFactory": new ActionLogicFactory(new Scope(new JUnitResourceAccessor(), new HashMap<String, Object>())) {
            @Override
            protected Class<? extends ActionLogic>[] findAllServiceClasses(Scope scope) {
                return new Class[0];
            }

            @Override
            protected TemplateActionLogic[] getTemplateActionLogic(Scope scope) {
                return new TemplateActionLogic[0];
            }
        }])
        scope.database.setConnection(new MockJdbcConnection())
    }

    def "execute when null actionLogic"() {
        when:
        new ActionExecutor().execute(new MockAction(), scope) == null

        then:
        def e = thrown(ActionPerformException)
        e.message == "No supported ActionLogic implementation found for 'mock()' against scope(database=jdbc mock)"
    }

    def "execute when validation fails with errors"() {
        when:
        scope.getSingleton(ActionLogicFactory).register(new MockExternalInteractionLogic("mock logic", 1, MockAction) {

            ValidationErrors validate(Action action, Scope scope) {
                return new ValidationErrors()
                        .addError("Mock Validation Error")
                        .addError("Another Error")
            }
        })

        new ActionExecutor().execute(new MockAction(), scope)

        then:
        def e = thrown(ActionPerformException)
        e.message == "Validation Error(s): Mock Validation Error; Another Error for mock() with liquibase.actionlogic.ActionExecutorTest\$2"
    }

    def "execute update logic"() {
        when:
        scope.getSingleton(ActionLogicFactory).register(new MockExternalInteractionLogic("mock logic", 1, MockAction, {
            return new UpdateResult(12, "update logic ran");
        }))

        def result = new ActionExecutor().execute(new MockAction(), scope)

        then:
        result instanceof UpdateResult
        result.message == "update logic ran"
        ((UpdateResult) result).numberAffected == 12
    }

    def "execute 'execute' logic"() {
        when:
        scope.getSingleton(ActionLogicFactory).register(new MockExternalInteractionLogic("mock logic", 1, MockAction, {
            return new ExecuteResult("execute logic ran");
        }))

        def result = new ActionExecutor().execute(new MockAction(), scope)

        then:
        result instanceof ExecuteResult
        result.message == "execute logic ran"
    }

    def "execute 'query' logic"() {
        when:
        scope.getSingleton(ActionLogicFactory).register(new MockExternalInteractionLogic("mock logic", 1, MockAction, {
            return new RowBasedQueryResult("DATA", "query logic ran");
        }))

        def result = new ActionExecutor().execute(new MockAction(), scope)

        then:
        result instanceof QueryResult
        result.message == "query logic ran"
    }

    def "execute 'rewrite' logic with an empty rewrite action list throws an exception"() {
        when:
        def factory = scope.getSingleton(ActionLogicFactory)
        factory.register(new MockActionLogic("mock logic", 1, MockAction, {
            return new DelegateResult();
        }))

        def result = new ActionExecutor().execute(new MockAction(), scope)

        then:
        def e = thrown(ActionPerformException)
        e.message == "liquibase.actionlogic.MockActionLogic tried to handle 'mock()' but returned no actions to run"
    }

    def "execute 'rewrite' logic with a single rewrite action"() {
        when:
        def factory = scope.getSingleton(ActionLogicFactory)
        factory.register(new MockActionLogic("mock logic", 1, MockAction, {
            return new DelegateResult(new UpdateSqlAction("sql action 1"));
        }))
        factory.register(new MockExternalInteractionLogic("mock sql", 1, UpdateSqlAction) {
            @Override
            ActionResult execute(Action action, Scope scope) throws ActionPerformException {
                return new ExecuteResult("executed sql: " + ((AbstractSqlAction) action).sql);
            }
        })

        then:
        def result = new ActionExecutor().execute(new MockAction(), scope)

        result instanceof ExecuteResult
        result.message == "executed sql: sql action 1"
    }

    def "execute 'rewrite' logic with multiple rewrite actions"() {
        when:
        def factory = scope.getSingleton(ActionLogicFactory)
        factory.register(new MockActionLogic("mock logic", 1, MockAction, {
            return new DelegateResult(new UpdateSqlAction("sql action 1"), new UpdateSqlAction("sql action 2"));
        }))
        factory.register(new MockExternalInteractionLogic("mock sql", 1, UpdateSqlAction) {
            @Override
            ActionResult execute(Action action, Scope scope) throws ActionPerformException {
                return new ExecuteResult("executed sql: " + ((AbstractSqlAction) action).sql);
            }
        })

        then:
        def result = new ActionExecutor().execute(new MockAction(), scope)

        def nestedActions = new ArrayList<Map.Entry>(((CompoundResult) result).resultsBySource.entrySet())

        nestedActions.size() == 2
        nestedActions[0].key == new UpdateSqlAction("sql action 1")
        nestedActions[0].value.message == "executed sql: sql action 1"

        nestedActions[1].key == (new UpdateSqlAction("sql action 2"))
        nestedActions[1].value.message == "executed sql: sql action 2"
    }

    def "execute 'rewrite' logic with multiple levels of rewrite actions"() {
        when:
        def factory = scope.getSingleton(ActionLogicFactory)
        factory.register(new MockActionLogic("mock logic", 1, MockAction, {
            return new DelegateResult(new UpdateSqlAction("sql action 1"), new ExecuteSqlAction("exec sql action"), new UpdateSqlAction("sql action 2"));
        }))
        factory.register(new MockExternalInteractionLogic("mock sql", 1, UpdateSqlAction) {
            @Override
            ActionResult execute(Action action, Scope scope) throws ActionPerformException {
                return new ExecuteResult("executed sql: " + ((AbstractSqlAction) action).sql);
            }
        })

        factory.register(new MockActionLogic("mock execute sql", 1, ExecuteSqlAction) {
            @Override
            ActionResult execute(Action action, Scope scope) throws ActionPerformException {
                return new DelegateResult(new UpdateSqlAction("nested 1"), new UpdateSqlAction("nested 2"));
            }
        })

        then:
        def result = new ActionExecutor().execute(new MockAction(), scope)

        result instanceof CompoundResult
        def nestedActions = new ArrayList<Map.Entry>(((CompoundResult) result).resultsBySource.entrySet())

        nestedActions.size() == 4
        nestedActions[0].key == new UpdateSqlAction("sql action 1")
        nestedActions[0].value.message == "executed sql: sql action 1"

        nestedActions[1].key == new UpdateSqlAction("nested 1")
        nestedActions[1].value.message == "executed sql: nested 1"

        nestedActions[2].key == new UpdateSqlAction("nested 2")
        nestedActions[2].value.message == "executed sql: nested 2"

        nestedActions[3].key == (new UpdateSqlAction("sql action 2"))
        nestedActions[3].value.message == "executed sql: sql action 2"
    }
}
