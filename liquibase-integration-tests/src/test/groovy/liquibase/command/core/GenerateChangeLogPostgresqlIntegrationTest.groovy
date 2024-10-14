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

@LiquibaseIntegrationTest
class GenerateChangeLogPostgresqlIntegrationTest extends Specification {
    @Shared
    private DatabaseTestSystem db =
            (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("postgresql")

    def "Should export database table TEST by applying includeObjects filter"() {
        given:
        db.executeSql("""
CREATE TABLE "TEST" (
    AFOO VARCHAR(4),
    BFOO VARCHAR(4),
    FOO  VARCHAR(4),
    FOOL VARCHAR(4)
);
INSERT INTO "TEST" (AFOO, BFOO, FOO, FOOL) VALUES ('AFOO', 'BFOO', 'FOO', 'FOOL');
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
        commandScope.addArgumentValue(PreCompareCommandStep.DIFF_TYPES_ARG, 'data')
        commandScope.addArgumentValue(DiffOutputControlCommandStep.INCLUDE_SCHEMA_ARG, true)
        commandScope.addArgumentValue(DiffOutputControlCommandStep.INCLUDE_OBJECTS, "table:TEST, column:(?!foo\$).*") // skip only 'foo'
        OutputStream outputStream = new ByteArrayOutputStream()
        commandScope.setOutput(outputStream)
        commandScope.execute()

        then:
        def outputFile = new File(outputFileName)
        def contents = FileUtil.getContents(outputFile)
        contents.contains("""
INSERT INTO "public"."TEST" ("afoo", "bfoo", "fool") VALUES ('AFOO', 'BFOO', 'FOOL');
"""
)

        when:
        CommandUtil.runDropAll(db)

        then:
        noExceptionThrown()

        cleanup:
        CommandUtil.runDropAll(db)
        outputFile.delete()
    }

    def "Should export database table TEST by applying excludeObjects filter"() {
        given:
        db.executeSql("""
CREATE TABLE "TEST" (
    AFOO VARCHAR(4),
    BFOO VARCHAR(4),
    FOO  VARCHAR(4),
    FOOL VARCHAR(4)
);
INSERT INTO "TEST" (AFOO, BFOO, FOO, FOOL) VALUES ('AFOO', 'BFOO', 'FOO', 'FOOL');
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
        commandScope.addArgumentValue(PreCompareCommandStep.DIFF_TYPES_ARG, 'data')
        commandScope.addArgumentValue(DiffOutputControlCommandStep.INCLUDE_SCHEMA_ARG, true)
        commandScope.addArgumentValue(DiffOutputControlCommandStep.EXCLUDE_OBJECTS, 'column:.foo$') // skip 'afoo' and 'bfoo'
        OutputStream outputStream = new ByteArrayOutputStream()
        commandScope.setOutput(outputStream)
        commandScope.execute()

        then:
        def outputFile = new File(outputFileName)
        def contents = FileUtil.getContents(outputFile)
        contents.contains("""
INSERT INTO "public"."TEST" ("foo", "fool") VALUES ('FOO', 'FOOL');
"""
        )

        when:
        CommandUtil.runDropAll(db)

        then:
        noExceptionThrown()

        cleanup:
        CommandUtil.runDropAll(db)
        outputFile.delete()
    }

    def "Should export full database table TEST excludeObjects filter produces NO columns"() {
        given:
        db.executeSql("""
CREATE TABLE "TEST" (
    AFOO VARCHAR(4),
    BFOO VARCHAR(4),
    FOO  VARCHAR(4),
    FOOL VARCHAR(4)
);
INSERT INTO "TEST" (AFOO, BFOO, FOO, FOOL) VALUES ('AFOO', 'BFOO', 'FOO', 'FOOL');
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
        commandScope.addArgumentValue(PreCompareCommandStep.DIFF_TYPES_ARG, 'data')
        commandScope.addArgumentValue(DiffOutputControlCommandStep.INCLUDE_SCHEMA_ARG, true)
        commandScope.addArgumentValue(DiffOutputControlCommandStep.EXCLUDE_OBJECTS, 'column:.*') // ALL columns are excluded, this is a mistake => ALL columns are taken
        OutputStream outputStream = new ByteArrayOutputStream()
        commandScope.setOutput(outputStream)
        commandScope.execute()

        then:
        def outputFile = new File(outputFileName)
        def contents = FileUtil.getContents(outputFile)
        contents.contains("""
INSERT INTO "public"."TEST" ("afoo", "bfoo", "foo", "fool") VALUES ('AFOO', 'BFOO', 'FOO', 'FOOL');
"""
        )

        when:
        CommandUtil.runDropAll(db)

        then:
        noExceptionThrown()

        cleanup:
        CommandUtil.runDropAll(db)
        outputFile.delete()
    }


    def "Should export full database table TEST when neither excludeObjects not includeObjects are used"() {
        given:
        db.executeSql("""
CREATE TABLE "TEST" (
    AFOO VARCHAR(4),
    BFOO VARCHAR(4),
    FOO  VARCHAR(4),
    FOOL VARCHAR(4)
);
INSERT INTO "TEST" (AFOO, BFOO, FOO, FOOL) VALUES ('AFOO', 'BFOO', 'FOO', 'FOOL');
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
        commandScope.addArgumentValue(PreCompareCommandStep.DIFF_TYPES_ARG, 'data')
        commandScope.addArgumentValue(DiffOutputControlCommandStep.INCLUDE_SCHEMA_ARG, true)
        OutputStream outputStream = new ByteArrayOutputStream()
        commandScope.setOutput(outputStream)
        commandScope.execute()

        then:
        def outputFile = new File(outputFileName)
        def contents = FileUtil.getContents(outputFile)
        contents.contains("""
INSERT INTO "public"."TEST" ("afoo", "bfoo", "foo", "fool") VALUES ('AFOO', 'BFOO', 'FOO', 'FOOL');
"""
        )

        when:
        CommandUtil.runDropAll(db)

        then:
        noExceptionThrown()

        cleanup:
        CommandUtil.runDropAll(db)
        outputFile.delete()
    }
}
