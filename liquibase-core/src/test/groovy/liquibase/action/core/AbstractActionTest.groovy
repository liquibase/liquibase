package liquibase.action.core

import liquibase.Scope
import liquibase.actionlogic.ActionExecutor
import liquibase.command.DropAllCommand
import liquibase.database.ConnectionSupplier
import liquibase.database.Database
import liquibase.database.core.UnsupportedDatabase
import liquibase.diff.output.DiffOutputControl
import liquibase.diff.output.changelog.ActionGeneratorFactory
import liquibase.snapshot.Snapshot
import liquibase.structure.core.Schema
import liquibase.structure.core.Table
import org.slf4j.LoggerFactory
import org.spockframework.runtime.SpecificationContext
import spock.lang.Specification
import testmd.Permutation
import testmd.TestMD
import testmd.logic.SetupResult

abstract class AbstractActionTest extends Specification {

    def testMDPermutation(Snapshot snapshot, ConnectionSupplier conn, Scope scope) {
        def database = scope.database

        def permutation = new ActionTestPermutation(this.specificationContext, this, snapshot, conn, scope, [:])

        permutation.addParameter("connection", conn.toString())

        return TestMD.test(this.specificationContext, database.class)
                .withPermutation(permutation)
    }

    def setupDatabase(Snapshot snapshot, ConnectionSupplier supplier, Scope scope) {
        Database database = scope.database
        if (database instanceof UnsupportedDatabase) {
            throw SetupResult.OK;
        }

        for (Schema schema : snapshot.get(Schema.class)) {
            new DropAllCommand(schema).execute(scope);
        }

        def control = new DiffOutputControl()
        def executor = new ActionExecutor()

        for (def obj : snapshot.get(Table.class)) {
            for (def action : scope.getSingleton(ActionGeneratorFactory).fixMissing(obj, control, snapshot, new Snapshot(scope), scope)) {
                LoggerFactory.getLogger(this.getClass()).debug("Executing: "+executor.createPlan(action, scope).describe())
                executor.execute(action, scope)
            }
        }

        throw SetupResult.OK
    }



    def cleanupDatabase(Snapshot snapshot, ConnectionSupplier supplier, Scope scope) {
        Database database = scope.database
        if (database instanceof UnsupportedDatabase) {
            return;
        }

        for (def obj : snapshot.get(Table.class)) {
            def action = new DropTableAction(obj.getName())
            LoggerFactory.getLogger(this.getClass()).debug("Executing: "+new ActionExecutor().createPlan(action, scope).describe())
            new ActionExecutor().execute(action, scope)
        }
    }

    static class ActionTestPermutation extends Permutation {
        Scope scope
        Database database
        ConnectionSupplier conn
        AbstractActionTest test
        Snapshot snapshot

        ActionTestPermutation(SpecificationContext specificationContext, AbstractActionTest test, Snapshot snapshot, ConnectionSupplier connectionSupplier, Scope scope, Map<String, Object> parameters) {
            super(specificationContext.currentIteration.parent.name, parameters)
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
                test.setupDatabase(snapshot, conn, scope)
                setup.run();
            })
        }
    }
}
