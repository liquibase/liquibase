package liquibase.command

import liquibase.Scope
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class UpdateSnowflakeIntegrationTest extends Specification{

    @Shared
    private DatabaseTestSystem snowflake = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("snowflake") as DatabaseTestSystem

    def "happy path update"() {
        when:
        CommandUtil.runUpdate(snowflake,'src/test/resources/changelogs/common/example-changelog.xml')

        then:
        noExceptionThrown()
    }
}
