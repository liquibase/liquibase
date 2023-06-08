package liquibase.command.core

import liquibase.Scope
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.core.DB2TestSystem
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.util.FileUtil
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class GenerateChangeLogDb2IntegrationTest extends Specification {
    @Shared
    private DB2TestSystem db2 = (DB2TestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("db2")

    def "Should generate view comments and be able to use the generated sql changelog"() {
//        given:
//        CommandUtil.runUpdate(db2,'changelogs/db2/issues/view.comments.sql')

        when:
        CommandUtil.runGenerateChangelog(db2.getUrl(), db2.getUsername(), db2.getPassword(),'output.db2.sql')

        then:
        def outputFile = new File('output.db2.sql')
        def contents = FileUtil.getContents(outputFile)
        contents.contains("COMMENT ON TABLE SOME_VIEW IS 'THIS IS A COMMENT ON SOME_VIEW VIEW. THIS VIEW COMMENT SHOULD BE CAPTURED BY GenerateChangeLog.'")

//        when:
//        CommandUtil.runDropAll(db2)
//        CommandUtil.runUpdate(db2,'output.mssql.sql')

//        then:
//        noExceptionThrown()
//
//        cleanup:
//        CommandUtil.runDropAll(db2)
//        outputFile.delete()
    }
}
