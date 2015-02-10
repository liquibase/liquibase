package liquibase.action.core

import liquibase.Scope
import liquibase.actionlogic.ActionExecutor
import liquibase.database.ConnectionSupplier
import liquibase.database.Database
import liquibase.database.core.UnsupportedDatabase
import liquibase.structure.core.Table
import spock.lang.Specification
import testmd.Permutation
import testmd.TestMD
import testmd.logic.SetupResult

abstract class AbstractActionTest extends Specification {

    def testMDPermutation(ConnectionSupplier conn, Scope scope) {
        def database = scope.database

        def permutation = new ActionTestPermutation(this, conn, scope, [database: database.class.name])

        return TestMD.test(this.class, "${database.shortName}", database.class)
                .permutation(permutation)
    }

    abstract def setupDatabase(ConnectionSupplier supplier, Scope scope);

    def cleanupDatabase(ConnectionSupplier supplier, scope) {
        Database database = scope.get(Scope.Attr.database, Database)
        if (database instanceof UnsupportedDatabase) {
            return;
        }

        for (def tableName : supplier.getReferenceObjectNames(Table.class, false, false)) {
            if (!database.canStoreObjectName(tableName.name, Table)) {
                continue;
            }
            new ActionExecutor().execute(new DropTableAction(tableName), scope)
        }
    }

    static class ActionTestPermutation extends Permutation {
        Scope scope
        Database database
        ConnectionSupplier conn
        AbstractActionTest test

        ActionTestPermutation(AbstractActionTest test, ConnectionSupplier connectionSupplier, Scope scope, Map<String, Object> parameters) {
            super(parameters)
            this.scope = scope
            this.database = scope.database
            this.conn = connectionSupplier
            this.test = test;

            this.setup({throw SetupResult.OK})
            this.cleanup({
                test.cleanupDatabase(connectionSupplier, scope)
            })
        }

        @Override
        Permutation setup(Runnable setup) {
            super.setup({
                if (scope.database instanceof UnsupportedDatabase) {
                    throw new SetupResult.CannotVerify("Unsupported Database");
                }

                conn.connect(scope)
                test.setupDatabase(conn, scope)
                setup.run();
            })
        }
    }
}
