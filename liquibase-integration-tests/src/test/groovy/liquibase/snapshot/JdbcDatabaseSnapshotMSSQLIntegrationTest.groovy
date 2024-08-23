package liquibase.snapshot

import liquibase.Scope
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.command.CommandTests
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.resource.SearchPathResourceAccessor
import liquibase.util.FileUtil
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class JdbcDatabaseSnapshotMSSQLIntegrationTest extends Specification {

    @Shared
    public DatabaseTestSystem mssql = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("mssql")

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

    def "snapshot int column must by int and not int size 10"() {
        when:
        mssql.executeSql('CREATE TABLE address(id1 int)')
        def outputFile = 'diff-output.mssql.sql'
        CommandUtil.runDiffChangelog('offline:oracle', mssql.getDatabaseFromFactory(), outputFile)
        def contents = FileUtil.getContents(new File(outputFile))

        then:
        contents.toString().contains("(id1 int)")

        cleanup:
        CommandUtil.runDropAll(mssql)
    }
}
