package liquibase

import liquibase.changelog.OfflineChangeLogHistoryService
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.database.core.HsqlDatabase
import liquibase.exception.UnexpectedLiquibaseException
import liquibase.logging.mdc.CustomMdcObject
import liquibase.logging.mdc.MdcManager
import liquibase.logging.mdc.MdcManagerFactory
import liquibase.logging.mdc.MdcObject
import spock.lang.Specification

import java.util.concurrent.ConcurrentHashMap

class ScopeTest extends Specification {

    def "getCurrentScope() creates root scope"() {
        expect:
        Scope.getCurrentScope().describe() == "scope(database=null)"
    }


    def "Nesting Scopes works"() {
        expect:
        Scope.currentScope.describe() == "scope(database=null)"

        assert Scope.currentScope.get("test1", String) == null
        assert Scope.currentScope.get("test2", String) == null
        assert Scope.currentScope.get("test3", String) == null

        Scope.child([test1: "Level 1 A", test2: "Level 1 B"], {
            assert Scope.currentScope.get("test1", String) == "Level 1 A"
            assert Scope.currentScope.get("test2", String) == "Level 1 B"
            assert Scope.currentScope.get("test3", String) == null

            Scope.child(["test1": "Level 2 A", "test3": "Level 2 C"], {
                assert Scope.currentScope.get("test1", String) == "Level 2 A"
                assert Scope.currentScope.get("test2", String) == "Level 1 B"
                assert Scope.currentScope.get("test3", String) == "Level 2 C"
            } as Scope.ScopedRunner)

            assert Scope.currentScope.get("test1", String) == "Level 1 A"
            assert Scope.currentScope.get("test2", String) == "Level 1 B"
            assert Scope.currentScope.get("test3", String) == null

            Scope.child(["test1": "Level 2 X", "test3": "Level 2 Y"], {
                assert Scope.currentScope.get("test1", String) == "Level 2 X"
                assert Scope.currentScope.get("test2", String) == "Level 1 B"
                assert Scope.currentScope.get("test3", String) == "Level 2 Y"

                Scope.child(["test1": "Level 3 D", "test2": "Level 3 E"], {
                    assert Scope.currentScope.get("test1", String) == "Level 3 D"
                    assert Scope.currentScope.get("test2", String) == "Level 3 E"
                    assert Scope.currentScope.get("test3", String) == "Level 2 Y"
                } as Scope.ScopedRunner)
            } as Scope.ScopedRunner)
        } as Scope.ScopedRunner)
    }

    def "start and end works"() {
        def mdcFactory = Scope.currentScope.getSingleton(MdcManagerFactory)
        def existingManager = mdcFactory.getMdcManager()
        def testManager = new TestMdcManager()
        mdcFactory.unregister(existingManager)
        mdcFactory.register(testManager)

        expect:
        Scope.currentScope.describe() == "scope(database=null)"

        assert Scope.currentScope.get("test1", String) == null
        assert Scope.currentScope.get("test2", String) == null
        assert Scope.currentScope.get("test3", String) == null

        def scope1 = Scope.enter(null, [test1: "Level 1 A", test2: "Level 1 B"])
        assert Scope.currentScope.get("test1", String) == "Level 1 A"
        assert Scope.currentScope.get("test2", String) == "Level 1 B"
        assert Scope.currentScope.get("test3", String) == null
        Scope.currentScope.addMdcValue("scope1", "scope1value")
        assert testManager.getValues().containsKey("scope1")

        def scope2 = Scope.enter(null, ["test1": "Level 2 A", "test3": "Level 2 C"])
        assert Scope.currentScope.get("test1", String) == "Level 2 A"
        assert Scope.currentScope.get("test2", String) == "Level 1 B"
        assert Scope.currentScope.get("test3", String) == "Level 2 C"
        Scope.currentScope.addMdcValue("cleanup1", "value1")
        Scope.currentScope.addMdcValue("nocleanup1", "value2", false)
        assert testManager.getValues().containsKey("cleanup1")
        assert testManager.getValues().containsKey("nocleanup1")
        assert testManager.getValues().containsKey("scope1")

        Scope.exit(scope2)
        assert Scope.currentScope.get("test1", String) == "Level 1 A"
        assert Scope.currentScope.get("test2", String) == "Level 1 B"
        assert Scope.currentScope.get("test3", String) == null
        assert !testManager.getValues().containsKey("cleanup1")
        assert testManager.getValues().containsKey("nocleanup1")
        assert testManager.getValues().containsKey("scope1")

        Scope.exit(scope1)
        assert Scope.currentScope.get("test1", String) == null
        assert Scope.currentScope.get("test2", String) == null
        assert Scope.currentScope.get("test3", String) == null
        assert !testManager.getValues().containsKey("scope1")

        cleanup:
        mdcFactory.unregister(testManager)
        mdcFactory.register(existingManager)
    }

    def "cannot end the wrong scope id"() {
        when:
        def scope1 = Scope.enter(null, [test1: "a"])
        def scope2 = Scope.enter(null, [test1: "b"])

        Scope.exit(scope1)

        then:
        def e = thrown(RuntimeException)
        e.message.startsWith("Cannot end scope ")
    }

    def "Constructor passed a null value gives useful error message"() {
        when:
            new Scope(null, Collections.emptyMap());

        then:
        def e = thrown(UnexpectedLiquibaseException)
        e.message == "Cannot pass a null parent to a new Scope. Use Scope.child to correctly create a nested scope"
    }

    def "scope generates deployment ID"() {
        when:
        def scope = Scope.getCurrentScope();
        def deploymentId = scope.getDeploymentId();

        then:
        deploymentId != null && ! deploymentId.isBlank() && ! deploymentId.isEmpty();
        scope.getDeploymentId() == deploymentId;
    }

    def "deprecated deployment id methods work correctly with scope deployment id"() {
        when:
        def database = new HsqlDatabase();
        def scope = Scope.getCurrentScope();
        def deploymentIdScope = scope.getDeploymentId();
        def changeLogHistoryService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database);
        def deploymentIdService1;
        def deploymentIdService2;

        changeLogHistoryService.generateDeploymentId();
        deploymentIdService1 = changeLogHistoryService.getDeploymentId();
        changeLogHistoryService.resetDeploymentId();
        changeLogHistoryService.generateDeploymentId();
        deploymentIdService2 = changeLogHistoryService.getDeploymentId();

        then:
        deploymentIdScope != null && ! deploymentIdScope.isBlank() && ! deploymentIdScope.isEmpty();
        deploymentIdService1 != null && ! deploymentIdService1.isBlank() && ! deploymentIdService1.isEmpty();
        deploymentIdService2 != null && ! deploymentIdService2.isBlank() && ! deploymentIdService2.isEmpty();
        deploymentIdScope == deploymentIdService1;
        deploymentIdScope == deploymentIdService2;
    }

    def "lazy initialization of scope manager for GraalVM compatibility"() {
        when:
        // Reset scope manager to test lazy initialization
        Scope.setScopeManager(null)
        def scope = Scope.getCurrentScope()

        then:
        // Should successfully create scope with lazy initialization
        scope != null
        scope.getResourceAccessor() != null
        scope.getUI() != null
        scope.getServiceLocator() != null
    }

    def "threadLocal inheritance for child threads"() {
        given:
        def inheritableValue = "inheritableValue"
        def scopeId = Scope.enter(null, [inheritableKey: inheritableValue])
        def childThreadValue = null

        when:
        // Create a child thread while scope is active - should inherit parent scope
        def childThread = Thread.start {
            childThreadValue = Scope.getCurrentScope().get("inheritableKey", String)
        }
        childThread.join(5000) // Wait up to 5 seconds

        then:
        // Child thread should inherit parent scope values through InheritableThreadLocal
        childThreadValue == inheritableValue

        cleanup:
        Scope.exit(scopeId)
    }

    def "concurrent scope access is thread safe"() {
        given:
        def threadCount = 10
        def errors = Collections.synchronizedList([])
        def latch = new java.util.concurrent.CountDownLatch(threadCount)

        when:
        // Create multiple threads that concurrently create and use scopes
        def threads = (0..<threadCount).collect { threadId ->
            Thread.start {
                try {
                    Scope.child([threadId: threadId], {
                        def currentScope = Scope.getCurrentScope()
                        def retrievedId = currentScope.get("threadId", Integer)
                        if (retrievedId != threadId) {
                            errors.add("Thread ${threadId} got wrong value: ${retrievedId}")
                        }
                    } as Scope.ScopedRunner)
                } catch (Exception e) {
                    errors.add("Thread ${threadId} failed: ${e.message}")
                } finally {
                    latch.countDown()
                }
            }
        }

        // Wait for all threads to complete
        def completed = latch.await(10, java.util.concurrent.TimeUnit.SECONDS)
        threads.each { it.join(1000) }

        then:
        completed
        errors.isEmpty()
    }

    def "custom scope manager can be set"() {
        given:
        def customManager = new SingletonScopeManager()
        def customScope = new Scope(Scope.getCurrentScope(), [:])
        customManager.setCurrentScope(customScope)

        when:
        Scope.setScopeManager(customManager)
        def currentScope = Scope.getCurrentScope()

        then:
        currentScope.getScopeId() == customScope.getScopeId()

        cleanup:
        // Reset to default
        Scope.setScopeManager(null)
    }

    private class TestMdcManager implements MdcManager {

        private Map<String, Object> values = new ConcurrentHashMap<>()

        @Override
        MdcObject put(String key, String value) {
            return put(key, (Object) value)
        }

        @Override
        MdcObject put(String key, String value, boolean removeWhenScopeExits) {
            return put(key, (Object) value)
        }

        @Override
        MdcObject put(String key, Map<String, Object> values, boolean removeWhenScopeExits) {
            return put(key, (Object) values)
        }

        @Override
        MdcObject put(String key, Map<String, Object> values) {
            return put(key, (Object) values)
        }

        @Override
        MdcObject put(String key, List<? extends CustomMdcObject> values) {
            return put(key, (Object) values)
        }

        @Override
        MdcObject put(String key, CustomMdcObject customMdcObject) {
            return put(key, (Object) customMdcObject)
        }

        @Override
        MdcObject put(String key, CustomMdcObject customMdcObject, boolean removeWhenScopeExits) {
            return put(key, (Object) customMdcObject)
        }

        MdcObject put(String key, Object object) {
            this.values.put(key, object)
            return new MdcObject(key, object)
        }

        @Override
        void remove(String key) {
            values.remove(key)
        }

        @Override
        void clear() {
            values.clear()
        }

        @Override
        int getPriority() {
            return Integer.MAX_VALUE
        }

        @Override
        Map<String, Object> getAll() {
            return Collections.unmodifiableMap(values)
        }

        Map<String, Object> getValues() {
            return values
        }
    }
}
