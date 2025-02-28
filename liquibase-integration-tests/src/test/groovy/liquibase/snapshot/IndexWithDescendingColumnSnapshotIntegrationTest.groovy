package liquibase.snapshot

import liquibase.GlobalConfiguration
import liquibase.Scope
import liquibase.change.core.CreateIndexChange
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.command.CommandScope
import liquibase.command.core.GenerateChangelogCommandStep
import liquibase.command.core.SnapshotCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.core.helpers.PreCompareCommandStep
import liquibase.command.core.helpers.ReferenceDbUrlConnectionCommandStep
import liquibase.command.util.CommandUtil
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.database.jvm.JdbcConnection
import liquibase.diff.DiffResult
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.parser.SnapshotParser
import liquibase.parser.SnapshotParserFactory
import liquibase.parser.core.json.JsonChangeLogParser
import liquibase.resource.ResourceAccessor
import liquibase.resource.SearchPathResourceAccessor
import liquibase.statement.SqlStatement
import liquibase.statement.core.RawParameterizedSqlStatement
import liquibase.structure.DatabaseObject
import liquibase.structure.DatabaseObjectCollection
import liquibase.structure.core.Column
import liquibase.structure.core.Index
import liquibase.util.StringUtil
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

@LiquibaseIntegrationTest
class IndexWithDescendingColumnSnapshotIntegrationTest extends Specification {
    @Shared
    public DatabaseTestSystem mssqlDb = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("mssql")

    @Unroll
    def "Index with a descending column is snapshot correctly"(boolean snapshotRelation) {
        when:
        def connection = mssqlDb.getConnection()
        def db = DatabaseFactory.instance.findCorrectDatabaseImplementation(new JdbcConnection(connection))
        db.execute([
                new RawParameterizedSqlStatement(
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
                new RawParameterizedSqlStatement(
                        "CREATE INDEX IX_temp_tbl_Preferences_Plain " +
                                "ON tbl_Preferences(" +
                                "fld_Fri," +
                                "fld_Thu," +
                                "fld_Wed DESC)"
                )
        ] as SqlStatement[], null)
        String snapshotFile = StringUtil.randomIdentifier(10) + "-snapshot.json"
        String changelogFile = StringUtil.randomIdentifier(10) + "-changelog.json"

        Map<String, Object> scopeValues = new HashMap<>()
        def resourceAccessor = new SearchPathResourceAccessor(".", Scope.getCurrentScope().getResourceAccessor())
        scopeValues.put(Scope.Attr.resourceAccessor.name(), resourceAccessor)
        scopeValues.put(GlobalConfiguration.INCLUDE_RELATIONS_FOR_COMPUTED_COLUMNS.getKey(), snapshotRelation)
        def diffResults
        DatabaseSnapshot snapshot
        OutputStream outputStream
        Scope.child(scopeValues, new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {

                final CommandScope snapshotScope = new CommandScope("snapshot")
                snapshotScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, db)
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
                generateScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, offlineUrl)
                generateScope.addArgumentValue(GenerateChangelogCommandStep.CHANGELOG_FILE_ARG, changelogFile)
                generateScope.execute()

                //
                // Execute diff
                //
                final Database targetDatabase =
                        DatabaseFactory.instance.openDatabase(offlineUrl, null, null, null, resourceAccessor)
                final CommandScope diffScope = new CommandScope("diff")
                diffScope.addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DATABASE_ARG, db)
                diffScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, offlineUrl)
                diffScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, targetDatabase)
                diffScope.addArgumentValue(PreCompareCommandStep.SNAPSHOT_TYPES_ARG.getName(), new Class[0])
                diffResults = diffScope.execute()
            }
        })
        SnapshotParser parser = SnapshotParserFactory.getInstance().getParser(snapshotFile, resourceAccessor)
        DatabaseSnapshot snapshotFromFile = parser.parse(snapshotFile, resourceAccessor)
        DatabaseObjectCollection referencedObjects = snapshotFromFile.getSerializableFieldValue("referencedObjects") as DatabaseObjectCollection
        Set<Column> columns = referencedObjects.toMap().get(Column.class)

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

        columns.forEach({ column ->
            if (snapshotRelation) {
                assert column.getAttribute("relation", String.class) != null;
            } else {
                if (column.getDescending()) {
                    assert column.getAttribute("relation", String.class) == null;
                } else {
                    assert column.getAttribute("relation", String.class) != null;
                }
            }
        })

        cleanup:
        db.execute([
                new RawParameterizedSqlStatement(
                        "DROP INDEX IX_temp_tbl_Preferences_Plain ON tbl_Preferences"
                ),
                new RawParameterizedSqlStatement(
                        "DROP TABLE tbl_Preferences"
                )
        ] as SqlStatement[], null)
        db.close()
        mssqlDb.getConnection().close()
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

        where:
        snapshotRelation |_
        true             |_
        false            |_
    }

    @Unroll
    def "Index with a descending column in older format is handled correctly"() {
        when:

        String snapshotFile = "oldSnapshotWithDescendingIndex.json"
        final CommandScope snapshotScope = new CommandScope("snapshot")
        String offlineUrl = "offline:mssql?snapshot=" + snapshotFile
        snapshotScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, offlineUrl)
        snapshotScope.addArgumentValue(SnapshotCommandStep.SNAPSHOT_FORMAT_ARG, "json")
        OutputStream outputStream = new ByteArrayOutputStream()
        snapshotScope.setOutput(outputStream)
        def results = snapshotScope.execute()
        DatabaseSnapshot snapshot = results.getResult("snapshot") as DatabaseSnapshot
        DatabaseObject index = snapshot.get(new Index("IX_temp_tbl_preferences_Plain"))

        //
        // Generate a changelog
        //
        String changelogFile = StringUtil.randomIdentifier(10) + "-changelog.json"
        final CommandScope generateChangelogScope = new CommandScope("generateChangelog")
        generateChangelogScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, offlineUrl)
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
