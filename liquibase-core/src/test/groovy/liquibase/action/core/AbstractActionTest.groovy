package liquibase.action.core

import liquibase.Scope
import liquibase.action.Action
import liquibase.actionlogic.ActionExecutor
import liquibase.database.ConnectionSupplier
import liquibase.database.Database
import liquibase.database.core.UnsupportedDatabase
import liquibase.diff.output.DiffOutputControl
import liquibase.diff.output.changelog.ActionGeneratorFactory
import liquibase.snapshot.Snapshot
import liquibase.structure.core.Table
import org.codehaus.groovy.runtime.StackTraceUtils
import org.spockframework.runtime.model.FeatureInfo
import org.spockframework.runtime.model.SpecInfo
import spock.lang.Specification
import testmd.Permutation
import testmd.TestMD
import testmd.logic.SetupResult

abstract class AbstractActionTest extends Specification {

    def testMDPermutation(Snapshot snapshot, ConnectionSupplier conn, Scope scope) {
        def database = scope.database

        def testName = specificationContext.iterationInfo.parent.name

        def permutation = new ActionTestPermutation(testName, this, snapshot, conn, scope, [:])

        return TestMD.test("${this.class.name}_${database.shortName}", testName, database.class)
                .withPermutation(permutation)
    }

    def setupDatabase(Snapshot snapshot, ConnectionSupplier supplier, Scope scope) {
        Database database = scope.database
        if (database instanceof UnsupportedDatabase) {
            throw SetupResult.OK;
        }

        def control = new DiffOutputControl()
        def executor = new ActionExecutor()

        for (def obj : snapshot.get(Table.class)) {
            for (def action : scope.getSingleton(ActionGeneratorFactory).fixMissing(obj, control, scope, scope)) {
                println executor.createPlan(action, scope).describe()
                executor.execute(action, scope)
            }
        }

        throw SetupResult.OK
    }



    def cleanupDatabase(Snapshot snapshot, ConnectionSupplier supplier, scope) {
        Database database = scope.get(Scope.Attr.database, Database)
        if (database instanceof UnsupportedDatabase) {
            return;
        }

        for (def obj : snapshot.get(Table.class)) {
            def action = new DropTableAction(obj.getName())
            println new ActionExecutor().createPlan(action, scope).describe()
            new ActionExecutor().execute(action, scope)
        }
    }

    static class ActionTestPermutation extends Permutation {
        Scope scope
        Database database
        ConnectionSupplier conn
        AbstractActionTest test
        Snapshot snapshot

        ActionTestPermutation(String testName, AbstractActionTest test, Snapshot snapshot, ConnectionSupplier connectionSupplier, Scope scope, Map<String, Object> parameters) {
            super(testName, parameters)
            this.scope = scope
            this.database = scope.database
            this.conn = connectionSupplier
            this.snapshot = snapshot
            this.test = test;

            this.setup({throw SetupResult.OK})
            this.cleanup({
                test.cleanupDatabase(snapshot, connectionSupplier, scope)
            })
        }

        @Override
        Permutation setup(Runnable setup) {
            super.setup({
                if (scope.database instanceof UnsupportedDatabase) {
                    throw new SetupResult.CannotVerify("Unsupported Database");
                }

                conn.connect(scope)
                test.setupDatabase(snapshot, conn, scope)
                setup.run();
            })
        }
    }
}
