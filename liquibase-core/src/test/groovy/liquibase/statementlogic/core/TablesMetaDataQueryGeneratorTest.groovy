package liquibase.statementlogic.core

import liquibase.action.QueryAction
import liquibase.command.DropAllCommand
import liquibase.database.OfflineConnection
import liquibase.datatype.core.IntType
import  liquibase.ExecutionEnvironment
import liquibase.executor.ExecutorService
import liquibase.lockservice.LockServiceFactory
import liquibase.sdk.supplier.database.ConnectionSupplier
import liquibase.sdk.supplier.database.DatabaseSupplier
import liquibase.sdk.verifytest.TestPermutation
import liquibase.sdk.verifytest.VerifyService
import liquibase.statement.core.CreateTableStatement
import liquibase.statement.core.MetaDataQueryStatement
import liquibase.statementlogic.StatementLogicFactory
import liquibase.structure.core.Table
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class TablesMetaDataQueryGeneratorTest extends Specification {

    @Shared databaseSupplier = new DatabaseSupplier()
    @Shared connectionSupplier = new ConnectionSupplier()

    @Unroll("#featureName: #connection")
    def emptyDatabase() {
        when:
        def verifyService = VerifyService.getInstance(this.class.name, "emptyDatabase");
        def schemaName = null
        def catalogName = null
        def tableName = "table_name"
        def statement = new MetaDataQueryStatement(new Table(schemaName, catalogName, tableName))

        def database = connection.getDatabase();

        def actions = StatementLogicFactory.instance.generateActions(statement, new ExecutionEnvironment(database))
        def options = new ExecutionEnvironment(database)

        then:
        actions.size() == 1
        def action = actions[0]

        verifyService.permutation([schemaName: schemaName, catalogName: catalogName, tableName: tableName, database: database, connection: connection])
                .asTable(["database", "connection", "schemaName", "catalogName", "tableName"])
                .data("action", action)
                .setup({
            if (!connection.connectionIsAvailable()) {
                return new TestPermutation.CannotVerify("Connection unavailable")
            }
            new DropAllCommand().setDatabase(database).execute()

            return new TestPermutation.OkResult()
        } as TestPermutation.Setup)
                .expect({
            def results = (action as QueryAction).query(options)
            assert results.toList().size() == 0
        } as TestPermutation.Verification)
                .test()

        where:
        connection << connectionSupplier.getConnections(databaseSupplier.allDatabases).findAll {
            !it.connectionClass.equals(OfflineConnection)
        }
    }

    @Unroll("#featureName: #tableName on #connection")
    def tableExists() {
        when:
        def verifyService = VerifyService.getInstance(this.class.name, "tableExists");
        def schemaName = null
        def catalogName = null
        def statement = new MetaDataQueryStatement(new Table(catalogName, schemaName, tableName))

        def database = connection.getDatabase();

        def actions = StatementLogicFactory.instance.generateActions(statement, new ExecutionEnvironment(database))
        def options = new ExecutionEnvironment(database)

        LockServiceFactory.instance.resetAll()

        then:
        actions.size() == 1
        def action = actions[0]

        verifyService.permutation([schemaName: schemaName, catalogName: catalogName, tableName: tableName, database: database, connection: connection])
                .asTable(["schemaName", "catalogName", "tableName"])
                .data("action", action)
                .setup({
            if (!connection.connectionIsAvailable()) {
                return new TestPermutation.CannotVerify("Connection unavailable")
            }

            new DropAllCommand().setDatabase(database).execute()

            ExecutorService.instance.getExecutor(database).execute(new CreateTableStatement(catalogName, schemaName, tableName).addColumn("id", new IntType()), options)

            return new TestPermutation.OkResult()
        } as TestPermutation.Setup)
                .expect({
            def results = (action as QueryAction).query(options)
            assert results.toList().size() == 1
        } as TestPermutation.Verification)
                .test()

        where:
        [connection, tableName] << [
                connectionSupplier.getConnections(databaseSupplier.allDatabases).findAll {
                    !it.connectionClass.equals(OfflineConnection)
                },
                ["table_name", "other_table"]
        ].combinations()
    }
}
