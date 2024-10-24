package liquibase.command.core

import liquibase.Scope
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.util.FileUtil

import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class GenerateChangeLogPostgresIntegrationTest extends Specification {
    @Shared
    private DatabaseTestSystem db = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("postgresql")

    def "Should generate SQL changelog incl. NULL-values"() {
        given:
        db.executeSql("""
create table "public"."TEST_WITH_PRESERVATION" (
    a varchar(2),
    b varchar(2),
    c varchar(2)
);
insert into "public"."TEST_WITH_PRESERVATION" (a) values ('AA');
commit;
""")

        when:
        CommandUtil.runGenerateChangelog(db,'target/test-classes/output-with.postgres.sql', true)

        then:
        def outputFile = new File('target/test-classes/output-with.postgres.sql')
        def contents = FileUtil.getContents(outputFile)
        contents.contains("""
INSERT INTO "public"."TEST_WITH_PRESERVATION" ("a", "b", "c") VALUES ('AA', NULL, NULL);
""")

        when:
        CommandUtil.runDropAll(db)

        then:
        noExceptionThrown()

        cleanup:
        CommandUtil.runDropAll(db)
        outputFile.delete()
    }

    def "Should generate SQL changelog excl. NULL-values"() {
        given:
        db.executeSql("""
create table "public"."TEST_WITHOUT_PRESERVATION" (
    a varchar(2),
    b varchar(2),
    c varchar(2)
);
insert into "public"."TEST_WITHOUT_PRESERVATION" (a) values ('AA');
commit;
""")

        when:
        CommandUtil.runGenerateChangelog(db,'target/test-classes/output-without.postgres.sql', false)

        then:
        def outputFile = new File('target/test-classes/output-without.postgres.sql')
        def contents = FileUtil.getContents(outputFile)
        contents.contains("""
INSERT INTO "public"."TEST_WITHOUT_PRESERVATION" ("a", "b", "c") VALUES ('AA', NULL, NULL);
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
