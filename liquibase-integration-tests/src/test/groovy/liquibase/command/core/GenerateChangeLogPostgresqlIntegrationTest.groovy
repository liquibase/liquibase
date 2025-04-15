package liquibase.command.core

import liquibase.Scope
import liquibase.command.CommandScope
import liquibase.command.core.helpers.DbUrlConnectionArgumentsCommandStep
import liquibase.command.core.helpers.DiffOutputControlCommandStep
import liquibase.command.core.helpers.PreCompareCommandStep
import liquibase.command.util.CommandUtil
import liquibase.exception.CommandExecutionException
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.util.FileUtil
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class GenerateChangeLogPostgresqlIntegrationTest extends Specification {
    // Postgresql has case-insensitive table / column names: 'TEST.foo' == 'tEsT.FoO' == 'test.FOO'

    @Shared
    private DatabaseTestSystem db = (DatabaseTestSystem) Scope.getCurrentScope()
            .getSingleton(TestSystemFactory.class)
            .getTestSystem("postgresql")

    private void callGenerateChangeLog(
            String outputFileName,
            String excludeObjects,
            String includeObjects)
    {
        CommandScope commandScope = new CommandScope(GenerateChangelogCommandStep.COMMAND_NAME)
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.URL_ARG, db.getConnectionUrl())
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.USERNAME_ARG, db.getUsername())
        commandScope.addArgumentValue(DbUrlConnectionArgumentsCommandStep.PASSWORD_ARG, db.getPassword())
        commandScope.addArgumentValue(GenerateChangelogCommandStep.OVERWRITE_OUTPUT_FILE_ARG, true)
        commandScope.addArgumentValue(GenerateChangelogCommandStep.CHANGELOG_FILE_ARG, outputFileName)
        commandScope.addArgumentValue(PreCompareCommandStep.DIFF_TYPES_ARG, 'data')
        commandScope.addArgumentValue(DiffOutputControlCommandStep.INCLUDE_SCHEMA_ARG, true)
        if (null != excludeObjects) {
            commandScope.addArgumentValue(DiffOutputControlCommandStep.EXCLUDE_OBJECTS, excludeObjects)
        }
        if (null != includeObjects) {
            commandScope.addArgumentValue(DiffOutputControlCommandStep.INCLUDE_OBJECTS, includeObjects)
        }
        commandScope.execute()
    }

    def "Should export full database table TEST when neither excludeObjects nor includeObjects are used"() {
        given:
        db.executeSql("""
CREATE TABLE "public"."TEST" (
    AFOO VARCHAR(4),
    BFOO VARCHAR(4),
    FOO  VARCHAR(4),
    FOOL VARCHAR(4)
);
INSERT INTO "public"."TEST" (AFOO, BFOO, FOO, FOOL) VALUES ('AFOO', 'BFOO', 'FOO', 'FOOL');
COMMIT;
""")

        when:
        def outputFileName = 'test/test-classes/output.postgresql.sql'
        callGenerateChangeLog (outputFileName, null, null)

        then:
        def outputFile = new File(outputFileName)
        outputFile.exists()
        def contents = FileUtil.getContents(outputFile)
        contents.contains("""
INSERT INTO "public"."TEST" ("afoo", "bfoo", "foo", "fool") VALUES ('AFOO', 'BFOO', 'FOO', 'FOOL');
"""
        )

        cleanup:
        outputFile.delete()
    }

    def "Should export database table TEST by applying includeObjects filter"() {
        given:
        db.executeSql("""
CREATE TABLE "public"."TEST" (
    AFOO VARCHAR(4),
    BFOO VARCHAR(4),
    FOO  VARCHAR(4),
    FOOL VARCHAR(4)
);
INSERT INTO "public"."TEST" (AFOO, BFOO, FOO, FOOL) VALUES ('AFOO', 'BFOO', 'FOO', 'FOOL');
COMMIT;
""")

        when:
        def outputFileName = 'test/test-classes/output.postgresql.sql'
        callGenerateChangeLog (outputFileName, null, 'table:TEST, column:(?!foo\$).*') // skip only 'foo'

        then:
        def outputFile = new File(outputFileName)
        def contents = FileUtil.getContents(outputFile)
        contents.contains("""
INSERT INTO "public"."TEST" ("afoo", "bfoo", "fool") VALUES ('AFOO', 'BFOO', 'FOOL');
"""
        )

        cleanup:
        outputFile.delete()
    }

    def "Should export database table TEST by applying excludeObjects filter out some columns"() {
        given:
        db.executeSql("""
CREATE TABLE "public"."TEST" (
    AFOO VARCHAR(4),
    BFOO VARCHAR(4),
    FOO  VARCHAR(4),
    FOOL VARCHAR(4)
);
INSERT INTO "public"."TEST" (AFOO, BFOO, FOO, FOOL) VALUES ('AFOO', 'BFOO', 'FOO', 'FOOL');
COMMIT;
""")

        when:
        def outputFileName = 'test/test-classes/output.postgresql.sql'
        callGenerateChangeLog (outputFileName, 'column:^.foo$',  null) // skip 'afoo' and 'bfoo'

        then:
        def outputFile = new File(outputFileName)
        def contents = FileUtil.getContents(outputFile)
        contents.contains("""
INSERT INTO "public"."TEST" ("foo", "fool") VALUES ('FOO', 'FOOL');
"""
        )

        cleanup:
        outputFile.delete()
    }

    def "Should export table TEST when excludeObjects filters case-insensitively"() {
        given:
        db.executeSql("""
CREATE TABLE "public"."TEST" (
    AFOO VARCHAR(4),
    BFOO VARCHAR(4),
    FOO  VARCHAR(4),
    FOOL VARCHAR(4)
);
INSERT INTO "public"."TEST" (AFOO, BFOO, FOO, FOOL) VALUES ('AFOO', 'BFOO', 'FOO', 'FOOL');
COMMIT;
""")

        when:
        def outputFileName = 'test/test-classes/output.postgresql.sql'
        callGenerateChangeLog (outputFileName, 'column:^.FoO$',  null) // skip 'afoo' and 'bfoo'

        then:
        def outputFile = new File(outputFileName)
        def contents = FileUtil.getContents(outputFile)
        contents.contains("""
INSERT INTO "public"."TEST" ("foo", "fool") VALUES ('FOO', 'FOOL');
"""
        )

        cleanup:
        outputFile.delete()
    }

    def "Should export table TEST when excludeObjects filters case-sensitively"() {
        // Quoted columns is a form of case-sensitive columns.
        given:
        db.executeSql("""
CREATE TABLE "public"."TEST" (
    "AFOO" VARCHAR(4),
    "BFOO" VARCHAR(4),
    "FOO"  VARCHAR(4),
    "FOOL" VARCHAR(4)
);
INSERT INTO "public"."TEST" ("AFOO", "BFOO", "FOO", "FOOL") VALUES ('AFOO', 'BFOO', 'FOO', 'FOOL');
COMMIT;
""")

        when:
        def outputFileName = 'test/test-classes/output.postgresql.sql'
        callGenerateChangeLog (outputFileName, 'column:^.FoO$',  null) // skip 'afoo' and 'bfoo'

        then:
        def outputFile = new File(outputFileName)
        def contents = FileUtil.getContents(outputFile)
        contents.contains("""
INSERT INTO "public"."TEST" ("FOO", "FOOL") VALUES ('FOO', 'FOOL');
"""
        )

        cleanup:
        outputFile.delete()
    }

    def "Should throw an Exception when excludeObjects filter produces NO columns"() {
        given:
        db.executeSql("""
CREATE TABLE "public"."TEST" (
    AFOO VARCHAR(4),
    BFOO VARCHAR(4),
    FOO  VARCHAR(4),
    FOOL VARCHAR(4)
);
INSERT INTO "public"."TEST" (AFOO, BFOO, FOO, FOOL) VALUES ('AFOO', 'BFOO', 'FOO', 'FOOL');
COMMIT;
""")

        when:
        def outputFileName = 'test/test-classes/output.postgresql.sql'
        callGenerateChangeLog (outputFileName, 'column:^.*',  null) // ALL columns are excluded, this is a mistake

        then:
        def e = thrown(CommandExecutionException)
        e.message.contains("No columns matched with excludeObjects 'column:^.*' / includeObjects 'null'")
    }

    def "Should export table TEST w/ neither excludeObjects nor includeObjects"() {
        // Quoted columns is a form of case-sensitive columns.
        given:
        db.executeSql("""
CREATE TABLE "public"."TEST" (
    "AFOO" VARCHAR(4),
    "BFOO" VARCHAR(4),
    "FOO"  VARCHAR(4),
    "FOOL" VARCHAR(4)
);
INSERT INTO "public"."TEST" ("AFOO", "BFOO", "FOO", "FOOL") VALUES ('AFOO', 'BFOO', 'FOO', 'FOOL');
COMMIT;
""")

        when:
        def outputFileName = 'test/test-classes/output.postgresql.sql'
        callGenerateChangeLog (outputFileName, null,null) // no filtering, capture all columns

        then:
        def outputFile = new File(outputFileName)
        def contents = FileUtil.getContents(outputFile)
        contents.contains("""
INSERT INTO "public"."TEST" ("AFOO", "BFOO", "FOO", "FOOL") VALUES ('AFOO', 'BFOO', 'FOO', 'FOOL');
"""
        )

        cleanup:
        outputFile.delete()
    }

    def "Should NOT include ID columns of table PERSON"() {
        given:
        CommandUtil.runUpdate(db, "src/test/resources/changelogs/pgsql/update/create-PERSON-and-SECONDARY.sql")

        when:
        def outputFileName = 'test/test-classes/output.postgresql.sql'
        callGenerateChangeLog (outputFileName, null, "table:PERSON, column:(?!ID).*\$")

        then:
        def outputFile = new File(outputFileName)
        def contents = FileUtil.getContents(outputFile)
        contents.contains("""
INSERT INTO "public"."PERSON" ("FIRSTNAME", "LASTNAME", "STATE") VALUES ('John', 'Kennedy', 'DC');
INSERT INTO "public"."PERSON" ("FIRSTNAME", "LASTNAME", "STATE") VALUES ('Jacqueline', 'Kennedy', 'DC');
"""
        )

        cleanup:
        outputFile.delete()
    }

    def "Should ONLY include ID columns of table PERSON"() {
        given:
        CommandUtil.runUpdate(db, "src/test/resources/changelogs/pgsql/update/create-PERSON-and-SECONDARY.sql")

        when:
        def outputFileName = 'test/test-classes/output.postgresql.sql'
        callGenerateChangeLog (outputFileName, null, "table:PERSON, column:ID")

        then:
        def outputFile = new File(outputFileName)
        def contents = FileUtil.getContents(outputFile)
        contents.contains("""
INSERT INTO "public"."PERSON" ("ID") VALUES (1);
INSERT INTO "public"."PERSON" ("ID") VALUES (2);
"""
        )

        cleanup:
        outputFile.delete()
    }

    def "Should exclude table PERSON, as well as column ID"() {
        given:
        CommandUtil.runUpdate(db, "src/test/resources/changelogs/pgsql/update/create-PERSON-and-SECONDARY.sql")

        when:
        def outputFileName = 'test/test-classes/output.postgresql.sql'
        callGenerateChangeLog (outputFileName, "table:PERSON, column:ID", null)

        then:
        def outputFile = new File(outputFileName)
        def contents = FileUtil.getContents(outputFile)
        contents.contains("""
INSERT INTO "public"."SECONDARY" ("ADDRESS", "COUNTRY", "REGION") VALUES ('1600 Pennsylvania Avenue', 'United States', 'NA');
INSERT INTO "public"."SECONDARY" ("ADDRESS", "COUNTRY", "REGION") VALUES ('280 Mulberry Street', 'United States', 'NA');
"""
        )

        cleanup:
        outputFile.delete()
    }

    def "Should exclude table PERSON, but not column ID"() {
        given:
        CommandUtil.runUpdate(db, "src/test/resources/changelogs/pgsql/update/create-PERSON-and-SECONDARY.sql")

        when:
        def outputFileName = 'test/test-classes/output.postgresql.sql'
        callGenerateChangeLog (outputFileName, "table:PERSON, column:(?!ID.*\$)", null)
        // Here the column ID is only excluded from the table "PERSON", but not from the table "SECONDARY".
        // It has no effect on "SECONDARY", because the filtering is a logical AND condition.

        then:
        def outputFile = new File(outputFileName)
        def contents = FileUtil.getContents(outputFile)
        contents.contains("""
INSERT INTO "public"."SECONDARY" ("ID", "ADDRESS", "COUNTRY", "REGION") VALUES (1, '1600 Pennsylvania Avenue', 'United States', 'NA');
INSERT INTO "public"."SECONDARY" ("ID", "ADDRESS", "COUNTRY", "REGION") VALUES (2, '280 Mulberry Street', 'United States', 'NA');
"""
        )

        cleanup:
        outputFile.delete()
    }

    def "Should include column ID"() {
        given:
        CommandUtil.runUpdate(db, "src/test/resources/changelogs/pgsql/update/create-PERSON-and-SECONDARY.sql")

        when:
        def outputFileName = 'test/test-classes/output.postgresql.sql'
        callGenerateChangeLog (outputFileName, null, "table:.*, column:ID")
        // Here the column ID is only excluded from the table "PERSON", but not from the table "SECONDARY".
        // It has no effect on "SECONDARY", because the filtering is a logical AND condition.

        then:
        def outputFile = new File(outputFileName)
        def contents = FileUtil.getContents(outputFile)
        contents.contains("""
INSERT INTO "public"."PERSON" ("ID") VALUES (1);
INSERT INTO "public"."PERSON" ("ID") VALUES (2);
"""
        )
        contents.contains("""
INSERT INTO "public"."SECONDARY" ("ID") VALUES (1);
INSERT INTO "public"."SECONDARY" ("ID") VALUES (2);
"""
        )

        cleanup:
        outputFile.delete()
    }

    def "Should exclude column ID"() {
        given:
        CommandUtil.runUpdate(db, "src/test/resources/changelogs/pgsql/update/create-PERSON-and-SECONDARY.sql")

        when:
        def outputFileName = 'test/test-classes/output.postgresql.sql'
        callGenerateChangeLog (outputFileName, null, "table:.*, column:(?!ID).*\$")

        then:
        def outputFile = new File(outputFileName)
        def contents = FileUtil.getContents(outputFile)
        contents.contains("""
INSERT INTO "public"."PERSON" ("FIRSTNAME", "LASTNAME", "STATE") VALUES ('John', 'Kennedy', 'DC');
INSERT INTO "public"."PERSON" ("FIRSTNAME", "LASTNAME", "STATE") VALUES ('Jacqueline', 'Kennedy', 'DC');
"""
        )
        contents.contains("""
INSERT INTO "public"."SECONDARY" ("ADDRESS", "COUNTRY", "REGION") VALUES ('1600 Pennsylvania Avenue', 'United States', 'NA');
INSERT INTO "public"."SECONDARY" ("ADDRESS", "COUNTRY", "REGION") VALUES ('280 Mulberry Street', 'United States', 'NA');
"""
        )

        cleanup:
        outputFile.delete()
    }
}
