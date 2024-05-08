package liquibase.command.core

import liquibase.Scope
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.util.FileUtil
import static org.junit.Assert.fail
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class MssqlIntegrationTest extends Specification {
    @Shared
    private DatabaseTestSystem mssql = (DatabaseTestSystem) Scope.getCurrentScope().getSingleton(TestSystemFactory.class).getTestSystem("mssql")

    def "Should not fail with merge statement"() {
        given:
        CommandUtil.runDropAll(mssql)
        when:
        CommandUtil.runUpdate(mssql,'src/test/resources/changelogs/mssql/issues/merge.statement.changelog.sql')
        then:
        noExceptionThrown()
        cleanup:
        CommandUtil.runDropAll(mssql)
    }

    def "verify store procedure does not get default endDelimiter added when a given delimiter is specified"() {
        given:
        CommandUtil.runUpdate(mssql,'src/test/resources/changelogs/mssql/issues/create.procedure.changelog.sql')

        def sProcedureName = "TestProcedure"
        def getStoreProcedureContentQuery = String.format("""
        SELECT ISNULL(smsp.definition, ssmsp.definition) AS [SPBody]
        FROM sys.all_objects AS sp LEFT OUTER JOIN sys.sql_modules AS smsp ON smsp.object_id = sp.object_id
        LEFT OUTER JOIN sys.system_sql_modules AS ssmsp ON ssmsp.object_id = sp.object_id
        WHERE (sp.type = N'P' OR sp.type = N'RF' OR sp.type='PC')and(sp.name=N'%s' and SCHEMA_NAME(sp.schema_id)=N'dbo')
        """, sProcedureName)

        when:
        def resultSet = mssql.getConnection().prepareStatement(getStoreProcedureContentQuery).executeQuery()

        then:
        def changelogContent = FileUtil.getContents(new File('src/test/resources/changelogs/mssql/issues/create.procedure.changelog.sql'))
        if(resultSet.next()) {
            def deployedSPContent = resultSet.getString("SPBody")
            changelogContent.contains(deployedSPContent.split(String.format("procedure dbo.%s", sProcedureName))[1])
        } else {
            fail(String.format("There is not procedure stored in the DB with name %s", sProcedureName))
        }
    }

    def "Should not fail with execution of create procedures with begin-end blocks"() {
        when:
        CommandUtil.runUpdate(mssql,'src/test/resources/changelogs/mssql/issues/begin.examples.changelog.xml')
        then:
        noExceptionThrown()
    }
}
