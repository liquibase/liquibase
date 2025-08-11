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

import java.nio.file.Files
import java.nio.file.Path

@LiquibaseIntegrationTest
class GenerateChangeLogEmptyIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem db = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("postgresql")

    def "Should generate changelog file with empty table"() {
        given:
        def changelogFileName = "target/test-classes/changelogs/update.changelog.sql"
        def resourceAccessor = new SearchPathResourceAccessor(".,target/test-classes")
        def scopeSettings = [
                (Scope.Attr.resourceAccessor.name()) : resourceAccessor
        ]
        Scope.child(scopeSettings, {
            CommandUtil.runUpdate(db, changelogFileName, "generateChangelogWithEmptyTable", null, null)
        } as Scope.ScopedRunner)

        when:
        def outputFileName = 'test/test-classes/output.postgresql.sql'
        CommandUtil.runGenerateChangelog(db, outputFileName, "tables, data")
        def outputFile = new File(outputFileName)
        def fileContent = FileUtil.getContents(outputFile)

        then:
        fileContent.containsIgnoreCase("create table \"generate_changelog_test_sql\"")

        cleanup:
        outputFile.delete()
    }

    //TODO: uncomment this test once we can find the reason why DAT-18821 is happening. Same for above assertion
    // and changeset from update.changelog.sql
//    def "Should generate changelog file with non-empty table"() {
//        given:
//        def changelogFileName = "target/test-classes/changelogs/update.changelog.sql"
//        def resourceAccessor = new SearchPathResourceAccessor(".,target/test-classes")
//        def scopeSettings = [
//                (Scope.Attr.resourceAccessor.name()) : resourceAccessor
//        ]
//        Scope.child(scopeSettings, {
//            CommandUtil.runUpdate(db, changelogFileName, "both", null, null)
//        } as Scope.ScopedRunner)
//
//        when:
//        def outputFileName = 'test/test-classes/output2.postgresql.sql'
//        CommandUtil.runGenerateChangelog(db, outputFileName, "tables, data")
//        def outputFile = new File(outputFileName)
//        def fileContent = FileUtil.getContents(outputFile)
//
//        then:
//        fileContent.containsIgnoreCase("CREATE TABLE \"generate_changelog_test_sql\"")
//        fileContent.containsIgnoreCase("INSERT INTO \"generate_changelog_test_sql\"")
//
//        cleanup:
//        outputFile.delete()
//    }

    def "Should NOT generate changelog file from an empty DB"() {
        given:
        def outputFileName = 'test/test-classes/emptyOutput.postgresql.sql'

        when:
        CommandUtil.runGenerateChangelog(db, outputFileName)

        then:
        !Files.exists(Path.of(outputFileName))
    }
}
