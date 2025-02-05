package liquibase.groovyDbTest.h2

import liquibase.Scope
import liquibase.changelog.ChangeLogParameters
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import spock.lang.Shared
import spock.lang.Specification

@LiquibaseIntegrationTest
class H2IntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem db = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("h2") as DatabaseTestSystem

    def "run update command for a yaml changelog file"() throws Exception {
        given:
        def changeLogFile = "changelogs/yaml/common.tests.changelog.yaml"

        when:
        //run again to test changelog testing logic
        def changeLogParameters = new ChangeLogParameters(db.getDatabaseFromFactory())
        changeLogParameters.set("loginuser", db.getUsername())
        CommandUtil.runUpdateWithParameters(db, changeLogFile, changeLogParameters)

        then:
        noExceptionThrown()
    }
}
