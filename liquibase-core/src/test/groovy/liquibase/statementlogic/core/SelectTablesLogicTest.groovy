package liquibase.statementlogic.core

import liquibase.CatalogAndSchema
import liquibase.ExecutionEnvironment
import liquibase.action.QueryAction
import liquibase.database.ObjectQuotingStrategy
import liquibase.database.OfflineConnection
import liquibase.datatype.core.IntType
import liquibase.executor.ExecutorService
import liquibase.logging.LogFactory
import liquibase.logging.LogLevel
import liquibase.sdk.TestExamples
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

import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

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
            database.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS)
            def actions = StatementLogicFactory.instance.generateActions(statement, new ExecutionEnvironment(database))

            assert actions.size() == 1

            verifyService.permutation([schemaName: schemaName, catalogName: catalogName, tableName: tableName, connection: connection])
                    .asTable(["schemaName", "catalogName", "tableName"])
                    .data("action", actions[0])
                    .setup({
                if (connection.getConnectionUnavailableReason() != null) {
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
            def statement = new SelectMetaDataStatement(new Table(catalogName, schemaName, tableName))
            def database = connection.getConnectedDatabase();
            database.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS)
            def actions = StatementLogicFactory.instance.generateActions(statement, new ExecutionEnvironment(database))

            assert actions.size() == 1

            verifyService.permutation([schemaName: schemaName, catalogName: catalogName, tableName: tableName, connection: connection])
                    .asTable(["schemaName", "catalogName", "tableName"])
                    .data("action", actions[0])
                    .setup({
                if (connection.getConnectionUnavailableReason() != null) {
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
                TestExamples.getTableNames()
        ].combinations()
    }

    @Unroll("#featureName: on #connection")
    def "multiple tables in the schema"() {
        expect:
        def verifyService = VerifyService.getInstance(this.class.name, "fullSchemaMultipleTables");
        for (catalogAndSchema in connection.getTestCatalogsAndSchemas()) {
            def (catalogName, schemaName) = catalogAndSchema
            def statement = new SelectMetaDataStatement(new Table(catalogName, schemaName, null))
            def database = connection.getConnectedDatabase();
            database.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS)
            def actions = StatementLogicFactory.instance.generateActions(statement, new ExecutionEnvironment(database))

            assert actions.size() == 1

            verifyService.permutation([schemaName: schemaName, catalogName: catalogName, connection: connection])
                    .asTable(["schemaName", "catalogName"])
                    .data("action", actions[0])
                    .setup({
                if (connection.getConnectionUnavailableReason() != null) {
                    return new TestPermutation.CannotVerify("Connection Unavailable")
                }

                database.dropDatabaseObjects(new CatalogAndSchema(catalogName, schemaName))
                ExecutorService.instance.getExecutor(database).execute(new CreateTableStatement(catalogName, schemaName, "table_name").addColumn("id", new IntType()), new ExecutionEnvironment(database))
                ExecutorService.instance.getExecutor(database).execute(new CreateTableStatement(catalogName, schemaName, "other_table").addColumn("id", new IntType()), new ExecutionEnvironment(database))
                ExecutorService.instance.getExecutor(database).execute(new CreateTableStatement(catalogName, schemaName, "yet_another_table").addColumn("id", new IntType()), new ExecutionEnvironment(database))

                return new TestPermutation.OkResult()
            } as TestPermutation.Setup)
                    .expect({
                def results = (actions[0] as QueryAction).query(new ExecutionEnvironment(database)).toList(Table)
                assert results.size() == 3
                assertThat results.collect({it.getName().toLowerCase()}), containsInAnyOrder(["table_name", "other_table", "yet_another_table"] as String[])
            } as TestPermutation.Verification)
        }
        where:
        connection << connectionSupplier.getConnections().findAll({ !it.connectionClass.equals(OfflineConnection) })
    }

    @Unroll("#featureName: on #connection")
    def "only gets tables in the correct schema"() {
        expect:
        def verifyService = VerifyService.getInstance(this.class.name, "tablesFromCorrectSchema");
        def catalogName = connection.getPrimaryCatalog()
        def schemaName = connection.getPrimarySchema()
        def otherSchemas = connection.getTestCatalogsAndSchemas().findAll({ it[0] != null && it[1] != null && it[0].equalsIgnoreCase(catalogName) && !(it[1].equalsIgnoreCase(schemaName)) })

        if (otherSchemas.size() == 0) {
            LogFactory.instance.log.debug("No alternate schemas available to test")
            return
        }

        def alternateSchemaName = otherSchemas[0][1] as String;

        def statement = new SelectMetaDataStatement(new Table(catalogName, alternateSchemaName, null))
        def database = connection.getConnectedDatabase();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS)
        def actions = StatementLogicFactory.instance.generateActions(statement, new ExecutionEnvironment(database))

        assert actions.size() == 1

        verifyService.permutation([connection: connection, schemaName: schemaName, alternateSchemaName: alternateSchemaName, catalogName: catalogName])
                .asTable(["catalogName", "schemaName", "alternateSchemaName", "connection"])
                .data("action", actions[0])
                .setup({
            if (connection.getConnectionUnavailableReason() != null) {
                return new TestPermutation.CannotVerify("Connection Unavailable")
            }

            database.dropDatabaseObjects(new CatalogAndSchema(catalogName, schemaName))
            database.dropDatabaseObjects(new CatalogAndSchema(catalogName, alternateSchemaName))
            ExecutorService.instance.getExecutor(database).execute(new CreateTableStatement(catalogName, schemaName, "table_name").addColumn("id", new IntType()), new ExecutionEnvironment(database))
            ExecutorService.instance.getExecutor(database).execute(new CreateTableStatement(catalogName, schemaName, "other_table").addColumn("id", new IntType()), new ExecutionEnvironment(database))
            ExecutorService.instance.getExecutor(database).execute(new CreateTableStatement(catalogName, alternateSchemaName, "other_schema_table").addColumn("id", new IntType()), new ExecutionEnvironment(database))
            ExecutorService.instance.getExecutor(database).execute(new CreateTableStatement(catalogName, schemaName, "yet_another_table").addColumn("id", new IntType()), new ExecutionEnvironment(database))
            ExecutorService.instance.getExecutor(database).execute(new CreateTableStatement(catalogName, alternateSchemaName, "another_other_schema_table").addColumn("id", new IntType()), new ExecutionEnvironment(database))

            return new TestPermutation.OkResult()
        } as TestPermutation.Setup)
                .expect({
            def results = (actions[0] as QueryAction).query(new ExecutionEnvironment(database)).toList(Table)
            assertThat results.collect({it.getName().toLowerCase()}), containsInAnyOrder(["other_schema_table", "another_other_schema_table"] as String[])
        } as TestPermutation.Verification)

        where:
        connection << connectionSupplier.getConnections().findAll({ !it.connectionClass.equals(OfflineConnection) && it.getConnectedDatabase().supportsSchemas() })
    }

    @Unroll("#featureName: on #connection")
    def "only gets tables in the correct catalog"() {
        expect:
        def verifyService = VerifyService.getInstance(this.class.name, "tablesFromCorrectCatalog");
        def catalogName = connection.getPrimaryCatalog()
        def schemaName = connection.getPrimarySchema()
        def otherSchemas = connection.getTestCatalogsAndSchemas().findAll(
                {
                    it[0] != null && !(it[0].equalsIgnoreCase(catalogName)) && (it[1] == null || it[1].equalsIgnoreCase(schemaName))
                }
        )

        if (otherSchemas.size() == 0) {
            LogFactory.instance.log.debug("No alternate catalogs available to test")
            return
        }

        def alternateCatalogName = otherSchemas[0][0] as String;

        if (!connection.getConnectedDatabase().supportsSchemas()) {
            schemaName = null;
        }

        def statement = new SelectMetaDataStatement(new Table(alternateCatalogName, schemaName, null))
        def database = connection.getConnectedDatabase();
        database.setObjectQuotingStrategy(ObjectQuotingStrategy.QUOTE_ALL_OBJECTS)
        def actions = StatementLogicFactory.instance.generateActions(statement, new ExecutionEnvironment(database))

        assert actions.size() == 1

        verifyService.permutation([connection: connection, catalogName: catalogName, alternateCatalogName: alternateCatalogName, schemaName: schemaName])
                .asTable(["connection", "catalogName", "alternateCatalogName", "schemaName"])
                .data("action", actions[0])
                .setup({
            if (connection.getConnectionUnavailableReason() != null) {
                return new TestPermutation.CannotVerify("Connection Unavailable")
            }

            database.dropDatabaseObjects(new CatalogAndSchema(catalogName, schemaName))
            database.dropDatabaseObjects(new CatalogAndSchema(alternateCatalogName, schemaName))
            ExecutorService.instance.getExecutor(database).execute(new CreateTableStatement(catalogName, schemaName, "table_name").addColumn("id", new IntType()), new ExecutionEnvironment(database))
            ExecutorService.instance.getExecutor(database).execute(new CreateTableStatement(catalogName, schemaName, "other_table").addColumn("id", new IntType()), new ExecutionEnvironment(database))
            ExecutorService.instance.getExecutor(database).execute(new CreateTableStatement(alternateCatalogName, schemaName, "other_cat_table").addColumn("id", new IntType()), new ExecutionEnvironment(database))
            ExecutorService.instance.getExecutor(database).execute(new CreateTableStatement(catalogName, schemaName, "yet_another_table").addColumn("id", new IntType()), new ExecutionEnvironment(database))
            ExecutorService.instance.getExecutor(database).execute(new CreateTableStatement(alternateCatalogName, schemaName, "another_other_cat_table").addColumn("id", new IntType()), new ExecutionEnvironment(database))

            return new TestPermutation.OkResult()
        } as TestPermutation.Setup)
                .expect({
            def results = (actions[0] as QueryAction).query(new ExecutionEnvironment(database)).toList(Table)
            assertThat results.collect({it.getName().toLowerCase()}), containsInAnyOrder(["other_cat_table", "another_other_cat_table"] as String[])
        } as TestPermutation.Verification)

        where:
        connection << connectionSupplier.getConnections().findAll({ !it.connectionClass.equals(OfflineConnection) && it.getConnectedDatabase().supportsCatalogs() })
    }
}
