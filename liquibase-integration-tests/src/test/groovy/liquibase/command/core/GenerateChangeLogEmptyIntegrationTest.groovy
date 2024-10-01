package liquibase.command.core

import liquibase.Scope
import liquibase.command.CommandScope
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.core.helpers.DiffOutputControlCommandStep
import liquibase.command.core.helpers.PreCompareCommandStep
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.util.FileUtil

import spock.lang.Shared
import spock.lang.Specification

import java.nio.file.Files
import java.nio.file.Path

@LiquibaseIntegrationTest
class GenerateChangeLogEmptyIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem db = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("postgresql")

    def "Should not generate changelog file with empty table"() {
        given:
        db.executeSql("create table \"TEST\" (b VARCHAR(10));")

        when:
        def outputFileName = 'test/test-classes/output.postgresql.sql'
        CommandScope commandScope = new CommandScope(GenerateChangelogCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, db.getConnectionUrl())
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, db.getUsername())
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, db.getPassword())
        commandScope.addArgumentValue(GenerateChangelogCommandStep.OVERWRITE_OUTPUT_FILE_ARG, true)
        commandScope.addArgumentValue(GenerateChangelogCommandStep.CHANGELOG_FILE_ARG, outputFileName)
        commandScope.addArgumentValue(PreCompareCommandStep.DIFF_TYPES_ARG, "data")
        commandScope.addArgumentValue(DiffOutputControlCommandStep.INCLUDE_SCHEMA_ARG, true)
        OutputStream outputStream = new ByteArrayOutputStream()
        commandScope.setOutput(outputStream)
        commandScope.execute()

        then:
        !Files.exists(Path.of(outputFileName))

        when:
        CommandUtil.runDropAll(db)

        then:
        noExceptionThrown()

        cleanup:
        CommandUtil.runDropAll(db)
    }

    def "Should generate changelog file with non-empty table"() {
        given:
        db.executeSql("""
create table "TEST" (
  b VARCHAR(10)
);
INSERT INTO "TEST" (b) VALUES ('Geronimo!');
COMMIT;
""")

        when:
        def outputFileName = 'test/test-classes/output.postgresql.sql'
        CommandScope commandScope = new CommandScope(GenerateChangelogCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, db.getConnectionUrl())
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, db.getUsername())
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, db.getPassword())
        commandScope.addArgumentValue(GenerateChangelogCommandStep.OVERWRITE_OUTPUT_FILE_ARG, true)
        commandScope.addArgumentValue(GenerateChangelogCommandStep.CHANGELOG_FILE_ARG, outputFileName)
        commandScope.addArgumentValue(PreCompareCommandStep.DIFF_TYPES_ARG, "data")
        commandScope.addArgumentValue(DiffOutputControlCommandStep.INCLUDE_SCHEMA_ARG, true)
        OutputStream outputStream = new ByteArrayOutputStream()
        commandScope.setOutput(outputStream)
        commandScope.execute()

        then:
        def outputFile = new File(outputFileName)
        Files.exists(Path.of(outputFileName))
        def contents = FileUtil.getContents(outputFile)
        contents.contains("""
INSERT INTO "public"."TEST" ("b") VALUES ('Geronimo!');
""")

        when:
        CommandUtil.runDropAll(db)

        then:
        noExceptionThrown()

        cleanup:
        CommandUtil.runDropAll(db)
        outputFile.delete()
    }
}
