package liquibase.snapshot


import liquibase.Scope
import liquibase.command.util.CommandUtil
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.extension.testing.command.CommandTests
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.resource.SearchPathResourceAccessor
import liquibase.statement.SqlStatement
import liquibase.statement.core.RawSqlStatement
import liquibase.structure.core.Column
import liquibase.structure.core.Table
import liquibase.structure.core.View
import org.junit.Rule
import spock.lang.Specification
import spock.lang.Unroll

class JdbcDatabaseSnapshotIntegrationTest extends Specification {

    @Rule
    public DatabaseTestSystem h2 = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("h2")

    @Rule
    public DatabaseTestSystem mssql = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("mssql")

    @Unroll
    def "getTables, getColumns and getViews works with underscores in schema names"() {
        when:
        def connection = h2.getConnection()
        def db = DatabaseFactory.instance.findCorrectDatabaseImplementation(new JdbcConnection(connection))
        db.execute([
                new RawSqlStatement("create schema if not exists \"TEST_SCHEMA\""),
                new RawSqlStatement("create schema if not exists \"TEST-SCHEMA\""),
                new RawSqlStatement("create table if not exists \"TEST-SCHEMA\".test_table (id int)"),
                new RawSqlStatement("create view if not exists \"TEST-SCHEMA\".test_view as select * from \"TEST-SCHEMA\".test_table"),
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
                new RawSqlStatement("drop schema \"test_schema\" if exists"),
                new RawSqlStatement("drop schema \"test-schema\" if exists"),
        ] as SqlStatement[], null)
    }

    def "columnstore indexes are deserialized out of snapshot json files properly"() {
        when:
        mssql.executeSql('CREATE TABLE address(id1 int, id2 int, id3 int)')
        mssql.executeSql('CREATE COLUMNSTORE INDEX IX_address ON address(id1, id2, id3)')
        def snapshotFile = CommandTests.takeDatabaseSnapshot(mssql.getDatabaseFromFactory(), "json")
        def outputFile = 'output.mssql.sql'
        // The results of this operation are unimportant. All this serves to do is to cause Liquibase to deserialize
        // the snapshot file.
        Scope.child(Collections.singletonMap(Scope.Attr.resourceAccessor.name(), new SearchPathResourceAccessor(".,target/test-classes/")), new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                CommandUtil.runDiff(mssql.getDatabaseFromFactory(), 'offline:sqlserver?snapshot=' + snapshotFile.toString(), outputFile)
            }
        })

        then:
        noExceptionThrown()

        cleanup:
        CommandUtil.runDropAll(mssql)
    }
}
