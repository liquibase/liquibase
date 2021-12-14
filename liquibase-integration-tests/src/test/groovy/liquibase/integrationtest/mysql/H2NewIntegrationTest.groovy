package liquibase.integrationtest.mysql

import liquibase.extension.testing.environment.TestEnvironment
import liquibase.extension.testing.environment.TestEnvironmentFactory
import liquibase.extension.testing.environment.spock.LiquibaseIntegrationTest
import spock.lang.Shared
import spock.lang.Specification

import java.sql.Connection

@LiquibaseIntegrationTest
class H2NewIntegrationTest extends Specification {

    @Shared
    TestEnvironment h2 = TestEnvironmentFactory.getEnvironment("h2");

    def "run test"() {
        when:
        Connection connection = h2.openConnection();
        def statement = connection.prepareStatement("select * from test")
        def rs = statement.executeQuery()

        then:
        rs.next()
    }
}
