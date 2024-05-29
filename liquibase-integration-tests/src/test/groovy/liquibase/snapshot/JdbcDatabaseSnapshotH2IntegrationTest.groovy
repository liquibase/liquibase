package liquibase.snapshot

import liquibase.Scope
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.statement.SqlStatement
import liquibase.statement.core.RawParameterizedSqlStatement
import liquibase.structure.core.Column
import liquibase.structure.core.Table
import liquibase.structure.core.View
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@LiquibaseIntegrationTest
class JdbcDatabaseSnapshotH2IntegrationTest extends Specification {

    @Shared
    public DatabaseTestSystem h2 = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("h2")

    @Unroll
    def "getTables, getColumns and getViews works with underscores in schema names"() {
        when:
        def connection = h2.getConnection()
        def db = DatabaseFactory.instance.findCorrectDatabaseImplementation(new JdbcConnection(connection))
        db.execute([
                new RawParameterizedSqlStatement("create schema if not exists \"TEST_SCHEMA\""),
                new RawParameterizedSqlStatement("create schema if not exists \"TEST-SCHEMA\""),
                new RawParameterizedSqlStatement("create table if not exists \"TEST-SCHEMA\".test_table (id int)"),
                new RawParameterizedSqlStatement("create view if not exists \"TEST-SCHEMA\".test_view as select * from \"TEST-SCHEMA\".test_table"),
        ] as SqlStatement[], null)

        then:
        SnapshotGeneratorFactory.instance.has(new Table(null, "TEST-SCHEMA", "TEST_TABLE"), db)
        !SnapshotGeneratorFactory.instance.has(new Table(null, "TEST_SCHEMA", "TEST_TABLE"), db)

        SnapshotGeneratorFactory.instance.has(new Column(Table.class,null, "TEST-SCHEMA", "TEST_TABLE", "ID"), db)
        !SnapshotGeneratorFactory.instance.has(new Column(Table.class, null, "TEST_SCHEMA", "TEST_TABLE","ID"), db)

        SnapshotGeneratorFactory.instance.has(new View(null, "TEST-SCHEMA", "TEST_VIEW"), db)
        !SnapshotGeneratorFactory.instance.has(new View(null, "TEST_SCHEMA", "TEST_VIEW"), db)

        cleanup:
        db.execute([
                new RawParameterizedSqlStatement("drop schema \"test_schema\" if exists"),
                new RawParameterizedSqlStatement("drop schema \"test-schema\" if exists"),
        ] as SqlStatement[], null)
    }
}
