package liquibase.command.update

import liquibase.Scope
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.util.StringUtil
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class CreateProcedurePostgresTest extends Specification {

    @Shared
    private DatabaseTestSystem postgres = (DatabaseTestSystem) Scope.getCurrentScope()
            .getSingleton(TestSystemFactory.class)
            .getTestSystem("postgresql")

    def "can create procedures on Postgres"() {
        when:
        CommandUtil.runUpdate(postgres,"src/test/resources/changelogs/pgsql/update/createProcedureReplaceIfExists.xml")

        then:
        noExceptionThrown()
    }
}
