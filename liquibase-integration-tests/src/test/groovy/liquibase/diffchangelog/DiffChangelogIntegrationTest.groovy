package liquibase.diffchangelog

import liquibase.Scope
import liquibase.command.CommandScope
import liquibase.command.core.DiffChangelogCommandStep
import liquibase.command.core.DropAllCommandStep
import liquibase.command.core.UpdateCommandStep
import liquibase.command.core.helpers.DbUrlConnectionCommandStep
import liquibase.command.core.helpers.DiffOutputControlCommandStep
import liquibase.command.core.helpers.PreCompareCommandStep
import liquibase.command.core.helpers.ReferenceDbUrlConnectionCommandStep
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.diff.compare.CompareControl
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.lockservice.LockServiceFactory
import liquibase.resource.SearchPathResourceAccessor
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.structure.core.Sequence
import liquibase.util.FileUtil
import liquibase.util.StringUtil
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class DiffChangelogIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem postgres =
            (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("postgresql")

    def "auto increment on varchar column" () {
        when:
        def updateChangelogFile = "target/test-classes/diffChangelog-test-sequence.sql"
        def changelogfile = StringUtil.randomIdentifer(10) + ".sql"
        def sequenceName = "customer_customer_id_seq"
        def tableName = StringUtil.randomIdentifer(10)
        def sql = """
CREATE SEQUENCE $sequenceName INCREMENT 5 START 100;
CREATE TABLE $tableName ( product_no varchar(20) DEFAULT nextval('$sequenceName'));
"""
        File updateFile = new File(updateChangelogFile)
        updateFile.write(sql.toString())
        Scope.child(Scope.Attr.resourceAccessor.name(), new SearchPathResourceAccessor("."), new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                CommandScope updateCommandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
                updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, postgres.getConnectionUrl())
                updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.USERNAME_ARG, postgres.getUsername())
                updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.PASSWORD_ARG, postgres.getPassword())
                updateCommandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, updateChangelogFile)
                updateCommandScope.execute()
            }
        })
        Database refDatabase = DatabaseFactory.instance.openDatabase(postgres.getConnectionUrl(), postgres.getUsername(), postgres.getPassword(), null, null)
        boolean b = SnapshotGeneratorFactory.instance.has(new Sequence(null, "public", sequenceName), refDatabase)
        assert b : "The sequence was not created on the database"

        Database targetDatabase =
            DatabaseFactory.instance.openDatabase(postgres.getConnectionUrl().replace("lbcat", "lbcat2"), postgres.getUsername(), postgres.getPassword(), null, null)

        CommandScope commandScope = new CommandScope(DiffChangelogCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DATABASE_ARG, refDatabase)
        commandScope.addArgumentValue(DiffChangelogCommandStep.CHANGELOG_FILE_ARG, changelogfile)
        commandScope.addArgumentValue(PreCompareCommandStep.COMPARE_CONTROL_ARG, CompareControl.STANDARD)
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, targetDatabase)
        commandScope.addArgumentValue(DiffOutputControlCommandStep.INCLUDE_SCHEMA_ARG, true)

        then:
        commandScope.execute()
        def generatedChangelog = new File(changelogfile)
        def generatedChangelogContents = FileUtil.getContents(generatedChangelog)
        generatedChangelogContents.contains("""CREATE SEQUENCE  IF NOT EXISTS "public"."${sequenceName.toLowerCase()}";""")
        generatedChangelogContents.contains("""CREATE TABLE "public"."${tableName.toLowerCase()}" ("product_no" VARCHAR(20) DEFAULT 'nextval(''''${sequenceName.toLowerCase()}''''::regclass)');""")

        cleanup:
        try {
            generatedChangelog.delete()
        } catch (Exception ignored) {

        }
        postgres.getConnection().close()
        refDatabase.close()
        targetDatabase.close()
        doDropAll(postgres)
    }

    //@Ignore("This test causes the pipeline to time out.")
    def "should include view comments"() {
        when:
        def updateChangelogFile = "target/test-classes/diffChangelog-test-view-comments.sql"
        def changelogfile = StringUtil.randomIdentifer(10) + ".sql"
        def viewName = StringUtil.randomIdentifer(10)
        def columnName = StringUtil.randomIdentifer(10)
        def viewComment = "some insightful comment"
        def columnComment = "some comment relating to this column"
        def sql = """
CREATE VIEW $viewName AS
    SELECT 'something important' as $columnName;
COMMENT ON VIEW $viewName IS '$viewComment';
COMMENT ON COLUMN $viewName.$columnName IS '$columnComment';
"""
        File updateFile = new File(updateChangelogFile)
        updateFile.write(sql)
        Scope.child(Scope.Attr.resourceAccessor.name(), new SearchPathResourceAccessor("."), new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                CommandScope updateCommandScope = new CommandScope(UpdateCommandStep.COMMAND_NAME)
                updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, postgres.getConnectionUrl())
                updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.USERNAME_ARG, postgres.getUsername())
                updateCommandScope.addArgumentValue(DbUrlConnectionCommandStep.PASSWORD_ARG, postgres.getPassword())
                updateCommandScope.addArgumentValue(UpdateCommandStep.CHANGELOG_FILE_ARG, updateChangelogFile)
                updateCommandScope.execute()
            }
        })
        Database refDatabase = DatabaseFactory.instance.openDatabase(postgres.getConnectionUrl(), postgres.getUsername(), postgres.getPassword(), null, null)

        Database targetDatabase =
                DatabaseFactory.instance.openDatabase(postgres.getConnectionUrl().replace("lbcat", "lbcat2"), postgres.getUsername(), postgres.getPassword(), null, null)

        CommandScope commandScope = new CommandScope(DiffChangelogCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DATABASE_ARG, refDatabase)
        commandScope.addArgumentValue(DiffChangelogCommandStep.CHANGELOG_FILE_ARG, changelogfile)
        commandScope.addArgumentValue(PreCompareCommandStep.COMPARE_CONTROL_ARG, CompareControl.STANDARD)
        commandScope.addArgumentValue(DbUrlConnectionCommandStep.DATABASE_ARG, targetDatabase)
        commandScope.addArgumentValue(DiffOutputControlCommandStep.INCLUDE_SCHEMA_ARG, true)

        then:
        commandScope.execute()
        def generatedChangelog = new File(changelogfile)
        def generatedChangelogContents = FileUtil.getContents(generatedChangelog)
        generatedChangelogContents.contains(viewComment)
        generatedChangelogContents.contains(columnComment)

        cleanup:
        try {
            generatedChangelog.delete()
        } catch (Exception ignored) {

        }
        postgres.getConnection().close()
        refDatabase.close()
        targetDatabase.close()
        doDropAll(postgres)
    }
    void doDropAll(DatabaseTestSystem testSystem) {
        Scope.child(new HashMap<String, Object>(), new Scope.ScopedRunner() {
            @Override
            void run() throws Exception {
                CommandScope dropAllCommandScope = new CommandScope(DropAllCommandStep.COMMAND_NAME)
                dropAllCommandScope.addArgumentValue(DbUrlConnectionCommandStep.URL_ARG, testSystem.getConnectionUrl())
                dropAllCommandScope.addArgumentValue(DbUrlConnectionCommandStep.USERNAME_ARG, testSystem.getUsername())
                dropAllCommandScope.addArgumentValue(DbUrlConnectionCommandStep.PASSWORD_ARG, testSystem.getPassword())
                dropAllCommandScope.execute()
            }
        })
    }
}
