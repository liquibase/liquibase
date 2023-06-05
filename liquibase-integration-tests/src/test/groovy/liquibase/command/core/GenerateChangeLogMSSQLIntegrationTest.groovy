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
class GenerateChangeLogMSSQLIntegrationTest extends Specification {
    @Shared
    private DatabaseTestSystem mssql = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("mssql")

    def "Should generate table comments, view comments, table column comments, view column comments and be able to use the generated sql changelog"() {
        given:
        CommandUtil.runDropAll(mssql)
        CommandUtil.runUpdate(mssql,'src/test/resources/changelogs/mssql/issues/generate.changelog.table.view.comments.sql')

        when:
        CommandUtil.runGenerateChangelog(mssql,'output.mssql.sql')

        then:
        def outputFile = new File('output.mssql.sql')
        def contents = FileUtil.getContents(outputFile)
        contents.contains("COMMENT 1")
        contents.contains("COMMENT 2")
        contents.contains("COMMENT 3")
        contents.contains("COMMENT 4")
        contents.contains("COMMENT 5")
        contents.contains("COMMENT 6")

        when:
        CommandUtil.runDropAll(mssql)
        CommandUtil.runUpdate(mssql,'output.mssql.sql')

        then:
        noExceptionThrown()

        cleanup:
        CommandUtil.runDropAll(mssql)
        outputFile.delete()
    }

    def "Should generate table comments, view comments, table column comments, view column comments and be able to use the generated xml/json/yml changelog"(String fileType) {
        given:
        CommandUtil.runDropAll(mssql)
        CommandUtil.runUpdate(mssql,'src/test/resources/changelogs/mssql/issues/generate.changelog.table.view.comments.sql')

        when:
        CommandUtil.runGenerateChangelog(mssql,"output.mssql.$fileType")

        then:
        def outputFile = new File("output.mssql.$fileType")
        def contents = FileUtil.getContents(outputFile)
        contents.count('columnParentType') == 2 //Should appear for the two view column comments.

        when:
        CommandUtil.runDropAll(mssql)
        CommandUtil.runUpdate(mssql, "output.mssql.$fileType")

        then:
        noExceptionThrown()

        cleanup:
        CommandUtil.runDropAll(mssql)
        outputFile.delete()

        where:
        fileType << ['xml', 'json', 'yml']
    }
}
