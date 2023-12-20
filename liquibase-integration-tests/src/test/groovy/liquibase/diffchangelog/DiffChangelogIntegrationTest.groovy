package liquibase.diffchangelog

import liquibase.Scope
import liquibase.change.core.AddForeignKeyConstraintChange
import liquibase.change.core.DropForeignKeyConstraintChange
import liquibase.changelog.ChangeLogParameters
import liquibase.changelog.ChangeSet
import liquibase.changelog.DatabaseChangeLog
import liquibase.command.CommandScope
import liquibase.command.core.DiffChangelogCommandStep
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.core.helpers.DiffOutputControlCommandStep
import liquibase.command.core.helpers.PreCompareCommandStep
import liquibase.command.core.helpers.ReferenceDbUrlConnectionCommandStep
import liquibase.command.util.CommandUtil
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.diff.compare.CompareControl
import liquibase.exception.CommandExecutionException
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.parser.core.json.JsonChangeLogParser
import liquibase.resource.SearchPathResourceAccessor
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.structure.core.Sequence
import liquibase.util.FileUtil
import liquibase.util.StringUtil
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class DiffChangelogIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem postgres =
            (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("postgresql")

    def "auto increment on varchar column" () {
        when:
        def changelogfile = StringUtil.randomIdentifer(10) + ".sql"
        def sequenceName = "customer_customer_id_seq"
        def tableName = StringUtil.randomIdentifer(10)
        def sql = """
CREATE SEQUENCE $sequenceName INCREMENT 5 START 100;
CREATE TABLE $tableName ( product_no varchar(20) DEFAULT nextval('$sequenceName'));
"""
        def updateChangelogFile = "target/test-classes/diffChangelog-test-sequence.sql"
        File updateFile = new File(updateChangelogFile)
        updateFile.write(sql.toString())
        CommandUtil.runUpdate(postgres, updateChangelogFile)
        Database refDatabase = DatabaseFactory.instance.openDatabase(postgres.getConnectionUrl(), postgres.getUsername(), postgres.getPassword(), null, null)
        boolean b = SnapshotGeneratorFactory.instance.has(new Sequence(null, "public", sequenceName), refDatabase)
        assert b : "The sequence was not created on the database"

        Database targetDatabase =
            DatabaseFactory.instance.openDatabase(postgres.getConnectionUrl().replace("lbcat", "lbcat2"), postgres.getUsername(), postgres.getPassword(), null, null)

        CommandScope commandScope = new CommandScope(DiffChangelogCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DATABASE_ARG, refDatabase)
        commandScope.addArgumentValue(DiffChangelogCommandStep.CHANGELOG_FILE_ARG, changelogfile)
        commandScope.addArgumentValue(PreCompareCommandStep.COMPARE_CONTROL_ARG, CompareControl.STANDARD)
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, targetDatabase)
        commandScope.addArgumentValue(DiffOutputControlCommandStep.INCLUDE_SCHEMA_ARG, true)
        commandScope.addArgumentValue(DiffChangelogCommandStep.LABEL_FILTER_ARG, "newLabels")
        commandScope.addArgumentValue(DiffChangelogCommandStep.CONTEXTS_ARG, "newContexts")

        then:
        commandScope.execute()
        def generatedChangelog = new File(changelogfile)
        def generatedChangelogContents = FileUtil.getContents(generatedChangelog)
        generatedChangelogContents.contains("""CREATE SEQUENCE  IF NOT EXISTS "public"."${sequenceName.toLowerCase()}" AS bigint START WITH 100 INCREMENT BY 5 MINVALUE 1 MAXVALUE 9223372036854775807 CACHE 1;""")
        generatedChangelogContents.contains("""CREATE TABLE "public"."${tableName.toLowerCase()}" ("product_no" VARCHAR(20) DEFAULT 'nextval(''''${sequenceName.toLowerCase()}''''::regclass)');""")
        generatedChangelogContents.contains(" labels: \"newlabels\"")
        generatedChangelogContents.contains(" contextFilter: \"newContexts\"")

        cleanup:
        try {
            generatedChangelog.delete()
        } catch (Exception ignored) {

        }
        postgres.getConnection().close()
        refDatabase.close()
        targetDatabase.close()
        CommandUtil.runDropAll(postgres)
    }

    def "FKs which are different but have same name" () {
        when:
        def diffChangelogFile = "target/test-classes/diffChangelog.json"
        def url = "offline:postgresql?snapshot=target/test-classes/target.json"
        Database targetDatabase =
                DatabaseFactory.getInstance().openDatabase(url, null, null, null, new SearchPathResourceAccessor("."))

        def refUrl = "offline:postgresql?snapshot=target/test-classes/reference.json"
        Database refDatabase =
                DatabaseFactory.getInstance().openDatabase(refUrl, null, null, null, new SearchPathResourceAccessor("."))

        CommandScope commandScope = new CommandScope(DiffChangelogCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DATABASE_ARG, refDatabase)
        commandScope.addArgumentValue(DiffChangelogCommandStep.CHANGELOG_FILE_ARG, diffChangelogFile)
        commandScope.addArgumentValue(PreCompareCommandStep.COMPARE_CONTROL_ARG, CompareControl.STANDARD)
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, targetDatabase)
        commandScope.addArgumentValue(DiffOutputControlCommandStep.INCLUDE_SCHEMA_ARG, true)
        commandScope.execute()
        def generatedChangelog = new File(diffChangelogFile)
        DatabaseChangeLog changelog =
                new JsonChangeLogParser().parse(diffChangelogFile, new ChangeLogParameters(), new SearchPathResourceAccessor("."))
        List<ChangeSet> changeSets = changelog.getChangeSets()

        then:
        DropForeignKeyConstraintChange dropFK = changeSets.get(0).getChanges().get(0) as DropForeignKeyConstraintChange
        AddForeignKeyConstraintChange addFK = changeSets.get(3).getChanges().get(0) as AddForeignKeyConstraintChange
        dropFK.getConstraintName() == addFK.getConstraintName()

        cleanup:
        try {
            generatedChangelog.delete()
        } catch (Exception ignored) {

        }
        postgres.getConnection().close()
        refDatabase.close()
        targetDatabase.close()
    }

    def "should include view comments"() {
        when:
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
        def updateChangelogFile = "target/test-classes/diffChangelog-test-view-comments.sql"
        File updateFile = new File(updateChangelogFile)
        updateFile.write(sql.toString())
        CommandUtil.runUpdate(postgres, updateChangelogFile)
        Database refDatabase = DatabaseFactory.instance.openDatabase(postgres.getConnectionUrl(), postgres.getUsername(), postgres.getPassword(), null, null)

        Database targetDatabase =
                DatabaseFactory.instance.openDatabase(postgres.getConnectionUrl().replace("lbcat", "lbcat2"), postgres.getUsername(), postgres.getPassword(), null, null)

        CommandScope commandScope = new CommandScope(DiffChangelogCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DATABASE_ARG, refDatabase)
        commandScope.addArgumentValue(DiffChangelogCommandStep.CHANGELOG_FILE_ARG, changelogfile)
        commandScope.addArgumentValue(PreCompareCommandStep.COMPARE_CONTROL_ARG, CompareControl.STANDARD)
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, targetDatabase)
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
        CommandUtil.runDropAll(postgres)
    }

    def "Ensure diff-changelog set runOnChange and replaceIfExists properties correctly for a created view changeset"() {
        given:
        CommandUtil.runUpdate(postgres, "changelogs/mysql/complete/createtable.and.view.changelog.xml", null, null, null)
        def outputChangelogFile = String.format("diffChangelogFile-%s-output.xml", StringUtil.randomIdentifer(10))
        Database refDatabase = DatabaseFactory.instance.openDatabase(postgres.getConnectionUrl(), postgres.getUsername(), postgres.getPassword(), null, null)

        Database targetDatabase =
                DatabaseFactory.instance.openDatabase(postgres.getConnectionUrl().replace("lbcat", "lbcat2"), postgres.getUsername(), postgres.getPassword(), null, null)

        when:
        CommandScope commandScope = new CommandScope(DiffChangelogCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, targetDatabase)
        commandScope.addArgumentValue(DiffChangelogCommandStep.CHANGELOG_FILE_ARG, outputChangelogFile)
        commandScope.addArgumentValue(DiffChangelogCommandStep.REPLACE_IF_EXISTS_TYPES_ARG, "createView")
        commandScope.addArgumentValue(DiffChangelogCommandStep.RUN_ON_CHANGE_TYPES_ARG, "createView")
        commandScope.addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DATABASE_ARG, refDatabase)
        commandScope.execute()

        then:

        def outputFile = new File(outputChangelogFile)
        def outputContent = FileUtil.getContents(outputFile)
        outputContent.contains(" runOnChange=\"true\">")
        outputContent.contains(" replaceIfExists=\"true\"")

        cleanup:
        outputFile.delete()
        refDatabase.close()
        targetDatabase.close()
        CommandUtil.runDropAll(postgres)
        postgres.getConnection().close()
    }

    def "Ensure diff-changelog with SQL output format contains 'OR REPLACE' instruction for a view when USE_OR_REPLACE_OPTION is set as true"() {
        given:
        def outputChangelogFile = String.format("diffChangelogFile-%s-output.postgresql.sql", StringUtil.randomIdentifer(10))
        Database refDatabase =
                DatabaseFactory.instance.openDatabase(postgres.getConnectionUrl(), postgres.getUsername(), postgres.getPassword(), null, null)
        Database targetDatabase =
                DatabaseFactory.instance.openDatabase(postgres.getConnectionUrl().replace("lbcat", "lbcat2"), postgres.getUsername(), postgres.getPassword(), null, null)

        postgres.executeSql("""create table foo(               
                id numeric not null primary key, 
                some_json json null)""")
        postgres.executeSql("CREATE VIEW fooview AS Select * from foo;")

        when:
        runDiffToChangelogWithUseOrReplaceCommandArgument(targetDatabase, refDatabase, outputChangelogFile, true)
        def outputFile = new File(outputChangelogFile)
        def contents = FileUtil.getContents(outputFile)

        then:
        contents.contains("CREATE OR REPLACE VIEW \"fooview\"")

        cleanup:
        outputFile.delete()
        refDatabase.close()
        targetDatabase.close()
        CommandUtil.runDropAll(postgres)
        postgres.getConnection().close()
    }

    def "Ensure diff-changelog with SQL output format does NOT contain 'OR REPLACE' instruction for a view when USE_OR_REPLACE_OPTION is set as false"() {
        given:
        def outputChangelogFile = String.format("diffChangelogFile-%s-output.postgresql.sql", StringUtil.randomIdentifer(10))
        Database refDatabase =
                DatabaseFactory.instance.openDatabase(postgres.getConnectionUrl(), postgres.getUsername(), postgres.getPassword(), null, null)
        Database targetDatabase =
                DatabaseFactory.instance.openDatabase(postgres.getConnectionUrl().replace("lbcat", "lbcat2"), postgres.getUsername(), postgres.getPassword(), null, null)

        postgres.executeSql("""create table foo(               
                id numeric not null primary key, 
                some_json json null)""")
        postgres.executeSql("CREATE VIEW fooview AS Select * from foo;")

        when:
        runDiffToChangelogWithUseOrReplaceCommandArgument(targetDatabase, refDatabase, outputChangelogFile, false)
        def outputFile = new File(outputChangelogFile)
        def contents = FileUtil.getContents(outputFile)

        then:
        !contents.contains("CREATE OR REPLACE VIEW \"fooview\"")
        contents.contains("CREATE VIEW \"fooview\"")

        cleanup:
        outputFile.delete()
        refDatabase.close()
        targetDatabase.close()
        CommandUtil.runDropAll(postgres)
        postgres.getConnection().close()
    }

    static void runDiffToChangelogWithUseOrReplaceCommandArgument(Database targetDatabase, Database referenceDatabase,
                                                                  String outputFile, boolean useOrReplaceOption) throws CommandExecutionException {
        CommandScope commandScope = new CommandScope(DiffChangelogCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.DATABASE_ARG, targetDatabase)
        commandScope.addArgumentValue(DiffChangelogCommandStep.CHANGELOG_FILE_ARG, outputFile)
        commandScope.addArgumentValue(DiffChangelogCommandStep.USE_OR_REPLACE_OPTION, useOrReplaceOption)
        commandScope.addArgumentValue(ReferenceDbUrlConnectionCommandStep.REFERENCE_DATABASE_ARG, referenceDatabase)
        commandScope.execute()
    }
}
