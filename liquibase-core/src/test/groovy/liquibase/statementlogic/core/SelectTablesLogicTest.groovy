package liquibase.statementlogic.core

import liquibase.CatalogAndSchema
import liquibase.ExecutionEnvironment
import liquibase.action.QueryAction
import liquibase.database.OfflineConnection
import liquibase.datatype.core.IntType
import liquibase.executor.ExecutorService
import liquibase.logging.LogFactory
import liquibase.logging.LogLevel
import liquibase.sdk.supplier.database.ConnectionSupplier
import liquibase.sdk.verifytest.TestPermutation
import liquibase.sdk.verifytest.VerifyService
import liquibase.statement.core.CreateTableStatement
import liquibase.statement.core.SelectMetaDataStatement
import liquibase.statementlogic.StatementLogicFactory
import liquibase.structure.core.Table
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class SelectTablesLogicTest extends Specification {

    @Shared connectionSupplier = new ConnectionSupplier()

    def setup() {
        LogFactory.getInstance().getLog().setLogLevel(LogLevel.DEBUG)
    }

    def cleanup() {
        LogFactory.getInstance().getLog().setLogLevel(LogLevel.INFO)
    }

    @Unroll("#featureName: #connection")
    def emptyDatabase() {
        expect:
        def verifyService = VerifyService.getInstance(this.class.name, "emptyDatabase");
        for (catalogAndSchema in connection.getTestCatalogsAndSchemas()) {
            def (catalogName, schemaName) = catalogAndSchema
            def tableName = "table_name"
            def statement = new SelectMetaDataStatement(new Table(catalogName, schemaName, tableName))
            def database = connection.getConnectedDatabase();
            def actions = StatementLogicFactory.instance.generateActions(statement, new ExecutionEnvironment(database))

            assert actions.size() == 1

            verifyService.permutation([schemaName: schemaName, catalogName: catalogName, tableName: tableName, connection: connection])
                    .asTable(["schemaName", "catalogName", "tableName"])
                    .data("action", actions[0])
                    .setup({
                if (!connection.connectionIsAvailable()) {
                    return new TestPermutation.CannotVerify("Connection Unavailable")
                }
                database.dropDatabaseObjects(new CatalogAndSchema(catalogName, schemaName))

                return new TestPermutation.OkResult()
            } as TestPermutation.Setup)
                    .expect({
                def results = (actions[0] as QueryAction).query(new ExecutionEnvironment(database))
                assert results.toList().size() == 0
            } as TestPermutation.Verification)
        }
        where:
        connection << connectionSupplier.getConnections().findAll { !it.connectionClass.equals(OfflineConnection) }
    }

    @Unroll("#featureName: #tableName on #connection")
    def "table is the only table in this schema"() {
        expect:
        def verifyService = VerifyService.getInstance(this.class.name, "tableExists");
        for (catalogAndSchema in connection.getTestCatalogsAndSchemas()) {
            def (catalogName, schemaName) = catalogAndSchema
            def tableName = "table_name"
            def statement = new SelectMetaDataStatement(new Table(catalogName, schemaName, tableName))
            def database = connection.getConnectedDatabase();
            def actions = StatementLogicFactory.instance.generateActions(statement, new ExecutionEnvironment(database))

            assert actions.size() == 1

            verifyService.permutation([schemaName: schemaName, catalogName: catalogName, tableName: tableName, connection: connection])
                    .asTable(["schemaName", "catalogName", "tableName"])
                    .data("action", actions[0])
                    .setup({
                if (!connection.connectionIsAvailable()) {
                    return new TestPermutation.CannotVerify("Connection Unavailable")
                }

                database.dropDatabaseObjects(new CatalogAndSchema(catalogName, schemaName))
                ExecutorService.instance.getExecutor(database).execute(new CreateTableStatement(catalogName, schemaName, tableName).addColumn("id", new IntType()), new ExecutionEnvironment(database))

                return new TestPermutation.OkResult()
            } as TestPermutation.Setup)
                    .expect({
                def results = (actions[0] as QueryAction).query(new ExecutionEnvironment(database)).toList(Table)
                assert results.size() == 1
                assert results[0].name.equalsIgnoreCase(tableName) //using equalsIgnoreCase because databases vary in how they return the names.
            } as TestPermutation.Verification)
        }
        where:
        [connection, tableName] << [
                connectionSupplier.getConnections().findAll({ !it.connectionClass.equals(OfflineConnection) }),
                ["table_name", "other_table", "CAPITAL_TABLE", "Mixed_Case_Table"]
        ].combinations()
    }
}
