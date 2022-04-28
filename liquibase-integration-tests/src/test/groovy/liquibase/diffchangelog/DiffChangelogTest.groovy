package liquibase.diffchangelog

import liquibase.Scope
import liquibase.command.CommandResultsBuilder
import liquibase.command.CommandScope
import liquibase.command.core.InternalDiffChangelogCommandStep
import liquibase.command.core.InternalDiffCommandStep
import liquibase.database.Database
import liquibase.database.DatabaseFactory
import liquibase.diff.compare.CompareControl
import liquibase.diff.output.DiffOutputControl
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.snapshot.SnapshotGeneratorFactory
import liquibase.structure.core.Sequence
import liquibase.util.FileUtil
import liquibase.util.StringUtil
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class DiffChangelogTest extends Specification {

    @Shared
    private DatabaseTestSystem postgres =
            (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("postgresql")

    def "auto increment on varchar column" () {
        when:
        def changelogfile = StringUtil.randomIdentifer(10) + ".sql"
        def sequenceName = StringUtil.randomIdentifer(10)
        def tableName = StringUtil.randomIdentifer(10)
        def sql = """
CREATE SEQUENCE $sequenceName INCREMENT 5 START 100;
CREATE TABLE $tableName ( product_no varchar(20) DEFAULT nextval('$sequenceName'));
"""
        postgres.executeSql(sql)
        postgres.getConnection().commit()

        Database refDatabase = DatabaseFactory.instance.openDatabase(postgres.getConnectionUrl(), postgres.getUsername(), postgres.getPassword(), null, null)
        boolean b = SnapshotGeneratorFactory.instance.has(new Sequence(null, "public", sequenceName), refDatabase)
        assert b : "The sequence was not created on the database"

        Database targetDatabase =
            DatabaseFactory.instance.openDatabase(postgres.getConnectionUrl().replace("lbcat", "lbcat2"), postgres.getUsername(), postgres.getPassword(), null, null)

        CommandScope commandScope = new CommandScope(InternalDiffChangelogCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(InternalDiffCommandStep.REFERENCE_DATABASE_ARG, refDatabase)
        commandScope.addArgumentValue(InternalDiffChangelogCommandStep.CHANGELOG_FILE_ARG, changelogfile)
        commandScope.addArgumentValue(InternalDiffCommandStep.TARGET_DATABASE_ARG, targetDatabase)
        commandScope.addArgumentValue(InternalDiffCommandStep.COMPARE_CONTROL_ARG, CompareControl.STANDARD)
        commandScope.addArgumentValue(InternalDiffChangelogCommandStep.DIFF_OUTPUT_CONTROL_ARG,  new DiffOutputControl())
        OutputStream outputStream = new ByteArrayOutputStream()
        CommandResultsBuilder commandResultsBuilder = new CommandResultsBuilder(commandScope, outputStream)

        then:
        InternalDiffChangelogCommandStep diffChangelogCommandStep = new InternalDiffChangelogCommandStep()
        diffChangelogCommandStep.run(commandResultsBuilder)
        def generatedChangelog = new File(changelogfile)
        def generatedChangelogContents = FileUtil.getContents(generatedChangelog)
        generatedChangelogContents.contains("""CREATE SEQUENCE  IF NOT EXISTS "public"."${sequenceName.toLowerCase()}";""")
        generatedChangelogContents.contains("""CREATE TABLE "public"."${tableName.toLowerCase()}" ("product_no" VARCHAR(20) DEFAULT 'nextval(''''${sequenceName.toLowerCase()}''''::regclass)');""")

        cleanup:
        try {
            generatedChangelog.delete()
        } catch (Exception ignored) {

        }
    }
}
