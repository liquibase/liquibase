package liquibase.snapshot

import liquibase.Scope
import liquibase.change.core.CreateIndexChange
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.command.CommandScope

import liquibase.command.core.GenerateChangelogCommandStep
import liquibase.command.core.SnapshotCommandStep
import liquibase.command.core.helpers.DbUrlConnectionCommandStep
import liquibase.command.core.helpers.PreCompareCommandStep
import liquibase.command.core.helpers.ReferenceDbUrlConnectionCommandStep
import liquibase.command.util.CommandUtil
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.diff.DiffResult
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.parser.core.json.JsonChangeLogParser
import liquibase.resource.ResourceAccessor
import liquibase.resource.SearchPathResourceAccessor
import liquibase.statement.SqlStatement
import liquibase.statement.core.RawSqlStatement
import liquibase.structure.DatabaseObject
import liquibase.structure.core.Index
import liquibase.util.StringUtil
import org.junit.Rule
import spock.lang.Specification
import spock.lang.Unroll

class IndexWithDescendingColumnSnapshotIntegrationTest extends Specification {
    @Rule
    public DatabaseTestSystem mssqlDb = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("mssql")

    @Unroll
    def "Index with a descending column is snapshot correctly"() {
        when:
        def connection = mssqlDb.getConnection()
        def db = DatabaseFactory.instance.findCorrectDatabaseImplementation(new JdbcConnection(connection))
        db.execute([
                new RawSqlStatement(
                        "CREATE TABLE tbl_Preferences (fld_EmployeeID INTEGER NOT NULL, " +
                                "fld_JobClass CHAR(4) NOT NULL, " +
                                "fld_Mon VARCHAR(25) NULL, " +
                                "fld_Tue VARCHAR(25) NULL, " +
                                "fld_Wed VARCHAR(25) NULL, " +
                                "fld_Thu VARCHAR(25) NULL, " +
                                "fld_Fri VARCHAR(25) NULL, " +
                                "fld_Sat VARCHAR(25) NULL, " +
                                "fld_Sun VARCHAR(25) NULL)"
                ),
                new RawSqlStatement(
                        "CREATE INDEX IX_temp_tbl_Preferences_Plain " +
                                "ON tbl_Preferences(" +
                                "fld_Fri," +
                                "fld_Thu," +
                                "fld_Wed DESC)"
                )
        ] as SqlStatement[], null)
        String snapshotFile = StringUtil.randomIdentifer(10) + "-snapshot.json"
        String changelogFile = StringUtil.randomIdentifer(10) + "-changelog.json"

        Map<String, Object> scopeValues = new HashMap<>()
        def resourceAccessor = new SearchPathResourceAccessor(".", Scope.getCurrentScope().getResourceAccessor())
        scopeValues.put(Scope.Attr.resourceAccessor.name(), resourceAccessor)
        def diffResults
        DatabaseSnapshot snapshot
        OutputStream outputStream
        Scope.child(scopeValues, new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {

                final CommandScope snapshotScope = new CommandScope("snapshot")
                snapshotScope.addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, db)
                snapshotScope.addArgumentValue(SnapshotCommandStep.SNAPSHOT_FORMAT_ARG, "json")
                outputStream = new FileOutputStream(snapshotFile)
                snapshotScope.setOutput(outputStream)
                def results = snapshotScope.execute()
                snapshot = results.getResult("snapshot") as DatabaseSnapshot

                //
                // Generate a changelog
                //
                String offlineUrl = "offline:mssql?snapshot=" + snapshotFile
                CommandScope generateScope = new CommandScope(GenerateChangelogCommandStep.COMMAND_NAME)
                generateScope.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, offlineUrl)
                generateScope.addArgumentValue(GenerateChangelogCommandStep.CHANGELOG_FILE_ARG, changelogFile)
                generateScope.execute()

                //
                // Execute diff
                //
                final Database targetDatabase =
                        DatabaseFactory.instance.openDatabase(offlineUrl, null, null, null, resourceAccessor)
                final CommandScope diffScope = new CommandScope("diff")
                diffScope.addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DATABASE_ARG, db)
                diffScope.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, offlineUrl)
                diffScope.addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, targetDatabase)
                diffScope.addArgumentValue(PreCompareCommandStep.SNAPSHOT_TYPES_ARG.getName(), new Class[0])
                diffResults = diffScope.execute()
            }
        })

        then:
        noExceptionThrown()

        snapshot != null
        DatabaseObject index = snapshot.get(new Index("IX_temp_tbl_preferences_Plain"))
        index != null
        index.getColumns().size() == 3
        index.getColumns().get(2).getName() == "fld_Wed"
        index.getColumns().get(2).getDescending()
        index.getColumns().get(1).getName() == "fld_Thu"
        !index.getColumns().get(1).getDescending()

        diffResults != null
        DiffResult diffResult = diffResults.getResult("diffResult") as DiffResult
        diffResult.getMissingObjects().size() == 0
        diffResult.getUnexpectedObjects().size() == 0
        diffResult.getChangedObjects().size() == 0

        cleanup:
        db.execute([
                new RawSqlStatement(
                        "DROP INDEX IX_temp_tbl_Preferences_Plain ON tbl_Preferences"
                ),
                new RawSqlStatement(
                        "DROP TABLE tbl_Preferences"
                )
        ] as SqlStatement[], null)
        mssqlDb.getConnection().close()
        db.close()
        if (outputStream != null) {
            outputStream.close()
        }
        File f = new File(snapshotFile)
        if (f.exists()) {
            f.delete()
        }
        f = new File(changelogFile)
        if (f.exists()) {
            f.delete()
        }
    }

    @Unroll
    def "Index with a descending column in older format is handled correctly"() {
        when:

        String snapshotFile = "oldSnapshotWithDescendingIndex.json"
        final CommandScope snapshotScope = new CommandScope("snapshot")
        String offlineUrl = "offline:mssql?snapshot=" + snapshotFile
        snapshotScope.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, offlineUrl)
        snapshotScope.addArgumentValue(SnapshotCommandStep.SNAPSHOT_FORMAT_ARG, "json")
        OutputStream outputStream = new ByteArrayOutputStream()
        snapshotScope.setOutput(outputStream)
        def results = snapshotScope.execute()
        DatabaseSnapshot snapshot = results.getResult("snapshot") as DatabaseSnapshot
        DatabaseObject index = snapshot.get(new Index("IX_temp_tbl_preferences_Plain"))

        //
        // Generate a changelog
        //
        String changelogFile = StringUtil.randomIdentifer(10) + "-changelog.json"
        final CommandScope generateChangelogScope = new CommandScope("generateChangelog")
        generateChangelogScope.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, offlineUrl)
        generateChangelogScope.addArgumentValue(GenerateChangelogCommandStep.CHANGELOG_FILE_ARG, changelogFile)
        generateChangelogScope.execute()

        ResourceAccessor resourceAccessor = new SearchPathResourceAccessor(".")
        DatabaseChangeLog changelog = new JsonChangeLogParser().parse(changelogFile, new ChangeLogParameters(), resourceAccessor)
        def changeSets = changelog.getChangeSets()
        ChangeSet indexChangeSet = changeSets.get(1)
        CreateIndexChange createIndexChange = indexChangeSet.getChanges().get(0)

        then:
        noExceptionThrown()

        snapshot != null
        index != null
        index.getColumns().size() == 3
        index.getColumns().get(2).getName() == "fld_Wed"
        index.getColumns().get(2).getDescending()
        index.getColumns().get(1).getName() == "fld_Thu"
        !index.getColumns().get(1).getDescending()

        changelog != null
        indexChangeSet.getChanges().size() == 1
        createIndexChange != null
        createIndexChange.getColumns().size() == 3
        createIndexChange.getColumns().get(2).getDescending()

        cleanup:
        File f = new File(changelogFile)
        if (f.exists()) {
            f.delete()
        }

        CommandUtil.runDropAll(mssqlDb)
        mssqlDb.getConnection().close()
    }
}
