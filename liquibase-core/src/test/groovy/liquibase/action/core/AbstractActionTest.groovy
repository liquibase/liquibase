package liquibase.action.core

import liquibase.JUnitScope
import liquibase.Scope
import liquibase.action.Action
import liquibase.action.QueryAction
import liquibase.action.TestObjectFactory
import liquibase.actionlogic.ActionExecutor
import liquibase.command.DropAllCommand
import liquibase.database.ConnectionSupplier
import liquibase.database.ConnectionSupplierFactory
import liquibase.database.Database
import liquibase.database.core.UnsupportedDatabase
import liquibase.diff.output.DiffOutputControl
import liquibase.diff.output.changelog.ActionGeneratorFactory
import liquibase.servicelocator.AbstractServiceFactory
import liquibase.servicelocator.Service
import liquibase.snapshot.Snapshot
import liquibase.structure.AbstractTestStructureSupplier
import liquibase.structure.DatabaseObject
import liquibase.structure.ObjectReference
import liquibase.structure.ObjectNameStrategy

import liquibase.structure.core.ForeignKey
import liquibase.structure.core.Index
import liquibase.structure.core.Schema
import liquibase.structure.core.Table
import liquibase.structure.core.UniqueConstraint
import org.junit.Assume
import org.slf4j.LoggerFactory
import org.spockframework.runtime.SpecificationContext
import spock.lang.Specification
import testmd.Permutation
import testmd.TestMD
import testmd.logic.Setup
import testmd.logic.SetupResult

abstract class AbstractActionTest extends Specification {

    def testMDPermutation(ConnectionSupplier conn, Scope scope) {
        return testMDPermutation(null, conn, scope)
    }

    protected abstract Snapshot createSnapshot(Action action, ConnectionSupplier connectionSupplier, Scope scope)

    def runStandardTest(Map parameters, Action action, ConnectionSupplier connectionSupplier, Scope scope, Closure assertClosure = {}, Closure setupClosure = {}) {
        return runStandardTest(parameters, null, action, connectionSupplier, scope, assertClosure, setupClosure)
    }

    def runStandardTest(Map parameters, Snapshot snapshot, Action action, ConnectionSupplier connectionSupplier, Scope scope, Closure assertClosure = {}, Closure setupClosure = {}) {
        def executor = scope.getSingleton(ActionExecutor)

        def errors = executor.validate(action, scope)
        Assume.assumeFalse(errors.toString() + " for action" + action.describe(), errors.hasErrors())

        def plan = executor.createPlan(action, scope)


        if (snapshot != null) {
            snapshot = createSnapshot(action, connectionSupplier, scope)
        }

        testMDPermutation(snapshot, setupClosure, connectionSupplier, scope)
                .addParameters(parameters)
                .addOperations(plan: plan)
                .run({
            def results = plan.execute(scope)

            if (!(action instanceof QueryAction)) {
                assert executor.checkStatus(action, scope).applied
            }

            assertClosure(plan, results)
        })

        return true;
    }

    protected List<ConnectionSupplier> getConnectionSuppliers() {
        JUnitScope.instance.getSingleton(ConnectionSupplierFactory).connectionSuppliers
    }

    protected List createAllPermutations(Class type, Map<String, List<Object>> defaultValues) {
        JUnitScope.instance.getSingleton(TestObjectFactory).createAllPermutations(type, defaultValues)
    }

    protected List<ObjectReference> getObjectNames(Class<? extends AbstractTestStructureSupplier> supplierType, ObjectNameStrategy strategy, Scope scope) {
        return scope.getSingleton(supplierType).getObjectNames(strategy, scope)
    }

    protected String correctObjectName(String name, Class<? extends DatabaseObject> type, Database database) {
        if (database.canStoreObjectName("lowercase", false, type)) {
            return name.toLowerCase();
        } else {
            return name.toUpperCase();
        }
    }

    def testMDPermutation(Snapshot snapshot, Closure setupClosure = {}, ConnectionSupplier conn, Scope scope) {
        def database = scope.database

        def permutation = new ActionTestPermutation(this.specificationContext, this, snapshot, setupClosure, conn, scope, [:])

        permutation.addParameter("connection", conn.toString())

        return TestMD.test(this.specificationContext, database.class)
                .withPermutation(permutation)
    }

    def setupDatabase(Snapshot snapshot, Closure setupClosure, ConnectionSupplier supplier, Scope scope) {
        Database database = scope.database
        if (database instanceof UnsupportedDatabase) {
            throw SetupResult.OK;
        }

        for (ObjectReference name : supplier.getAllContainers()) {
            new DropAllCommand(new ObjectReference(Schema, name)).execute(scope);
        }

        if (snapshot != null) {
            for (Schema schema : snapshot.get(Schema.class)) {
                new DropAllCommand(schema.toReference()).execute(scope);
            }

            def control = new DiffOutputControl()
            def executor = new ActionExecutor()

            for (def type : [Table, UniqueConstraint, Index, ForeignKey]) {
                for (def obj : snapshot.get(type)) {
                    for (def action : scope.getSingleton(ActionGeneratorFactory).fixMissing(obj, control, snapshot, new Snapshot(scope), scope)) {
                        LoggerFactory.getLogger(this.getClass()).debug("Setup action: " + executor.createPlan(action, scope).describe())
                        executor.execute(action, scope)
                    }
                }
            }
        }

        setupClosure()

        throw SetupResult.OK
    }


    def cleanupDatabase(Snapshot snapshot, ConnectionSupplier supplier, Scope scope) {}

    AbstractActionTest.TestDetails getTestDetails(Scope scope) {
        return scope.getSingleton(AbstractActionTest.TestDetailsFactory).getTestDetails(this, scope)
    }

    static class ActionTestPermutation extends Permutation {
        Scope scope
        Database database
        ConnectionSupplier conn
        AbstractActionTest test
        Snapshot snapshot
        Closure setupClosure

        ActionTestPermutation(SpecificationContext specificationContext, AbstractActionTest test, Snapshot snapshot, Closure setupClosure, ConnectionSupplier connectionSupplier, Scope scope, Map<String, Object> parameters) {
            super(specificationContext.currentIteration.parent.spec.getPackage() + "." + specificationContext.currentIteration.parent.spec.name, specificationContext.currentIteration.parent.name, parameters)
            this.scope = scope
            this.database = scope.database
            this.conn = connectionSupplier
            this.snapshot = snapshot
            this.test = test;
            this.setupClosure = setupClosure

            this.setup({ throw SetupResult.OK })
            this.cleanup({
                test.cleanupDatabase(snapshot, connectionSupplier, scope)
            })
        }

        @Override
        String formatNotVerifiedMessage(String message) {
            if (message != null) {
                if (message.startsWith("Cannot open connection: No suitable driver found for")) {
                    message = "No suitable driver"
                } else if (message.startsWith("Cannot open connection: Access denied for user")) {
                    message = "Access denied"
                }
            }
            return message;
        }

        @Override
        Permutation setup(Runnable setup) {
            super.setup({
                if (scope.database instanceof UnsupportedDatabase) {
                    throw new SetupResult.CannotVerify("Unsupported Database");
                }

                conn.connect(scope)
                test.setupDatabase(snapshot, setupClosure, conn, scope)
                setup.run();
            })
        }
    }

    public static class TestDetails implements Service {

    }

    public static class TestDetailsFactory<T extends AbstractActionTest.TestDetails> extends AbstractServiceFactory<T> {

        public TestDetailsFactory(Scope scope) {
            super(scope);
        }

        @Override
        protected Class<T> getServiceClass() {
            return (Class<T>) AbstractActionTest.TestDetails.class;
        }

        public T getTestDetails(AbstractActionTest test, Scope scope) {
            return getService(scope, test);
        }

        @Override
        protected int getPriority(AbstractActionTest.TestDetails obj, Scope scope, Object... args) {
            AbstractActionTest test = (AbstractActionTest) args[0];
            Class testName = test.getClass();
            Class testDetails = obj.getClass();

            if ((testName.getName() + '$TestDetails').equals(testDetails.getName())) {
                return Service.PRIORITY_DEFAULT;
            } else if ((testName.getSimpleName() + "Details" + scope.getDatabase().getShortName()).equalsIgnoreCase(testDetails.getSimpleName())) {
                return Service.PRIORITY_SPECIALIZED;
            } else {
                return Service.PRIORITY_NOT_APPLICABLE;
            }
        }

    }
}