package liquibase.command.core

import liquibase.Scope
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class MssqlMergeIntegrationTest extends Specification {
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
}
