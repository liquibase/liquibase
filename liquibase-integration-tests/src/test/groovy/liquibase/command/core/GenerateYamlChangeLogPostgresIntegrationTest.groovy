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
class GenerateYamlChangeLogPostgresIntegrationTest extends Specification {
    @Shared
    private DatabaseTestSystem db = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("postgresql")

    def "Should generate YAML changelog incl. NULL-values"() {
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
        def outputFileName = 'target/test-classes/output.postgresql.yaml'
        CommandUtil.runGenerateChangelog(db, outputFileName, true)

        then:
        def outputFile = new File(outputFileName)
        def fileContent = FileUtil.getContents(outputFile)
        fileContent.contains("""
    changes:
    - insert:
        columns:
        - column:
            name: a
            value: AA
        - column:
            name: b
            value: null
        - column:
            name: c
            value: null
        schemaName: public
        tableName: preservation_test
""")

        cleanup:
        outputFile.delete()
    }

    def "Should generate YAML changelog excl. NULL-values"() {
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
        def outputFileName = 'target/test-classes/output.postgresql.yaml'
        CommandUtil.runGenerateChangelog(db, outputFileName, false)

        then:
        def outputFile = new File(outputFileName)
        def fileContent = FileUtil.getContents(outputFile)
        fileContent.contains("""
    changes:
    - insert:
        columns:
        - column:
            name: a
            value: AA
        schemaName: public
        tableName: preservation_test
""")

        cleanup:
        outputFile.delete()
    }
}
