package liquibase.command.core

import liquibase.Scope
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.resource.SearchPathResourceAccessor
import liquibase.util.FileUtil

import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class GenerateSqlChangeLogPostgresIntegrationTest extends Specification {
    @Shared
    private DatabaseTestSystem db = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("postgresql")

    def "Should generate SQL changelog incl. NULL-values"() {
        given:
        def changelogFileName = "target/test-classes/changelogs/pgsql/update.changelog.xml"
        def resourceAccessor = new SearchPathResourceAccessor(".,target/test-classes")
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()) : resourceAccessor
        ]
        Scope.child(scopeSettings, {
            CommandUtil.runUpdate(db, changelogFileName, "generateChangelogWithEmptyTable", null, null)
        } as Scope.ScopedRunner)

        when:
        def outputFileName = 'test/test-classes/output.postgresql.sql'
        CommandUtil.runGenerateChangelog(db, outputFileName, true)
        def outputFile = new File(outputFileName)
        def fileContent = FileUtil.getContents(outputFile)

        then:
        fileContent.contains("""INSERT INTO public.PRESERVATION_TEST (a, b, c) VALUES ('AA', NULL, NULL);""")

        cleanup:
        outputFile.delete()
    }

    def "Should generate SQL changelog excl. NULL-values"() {
        given:
        def changelogFileName = "target/test-classes/changelogs/pgsql/update.changelog.xml"
        def resourceAccessor = new SearchPathResourceAccessor(".,target/test-classes")
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()) : resourceAccessor
        ]
        Scope.child(scopeSettings, {
            CommandUtil.runUpdate(db, changelogFileName, "generateChangelogWithEmptyTable", null, null)
        } as Scope.ScopedRunner)

        when:
        def outputFileName = 'test/test-classes/output.postgresql.sql'
        CommandUtil.runGenerateChangelog(db, outputFileName, false)
        def outputFile = new File(outputFileName)
        def fileContent = FileUtil.getContents(outputFile)

        then:
        fileContent.contains("""INSERT INTO public.PRESERVATION_TEST (a, b, c) VALUES ('AA', NULL, NULL);""")

        cleanup:
        outputFile.delete()
    }
}
