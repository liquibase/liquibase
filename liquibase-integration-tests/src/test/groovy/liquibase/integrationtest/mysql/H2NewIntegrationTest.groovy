package liquibase.integrationtest.mysql

import liquibase.extension.testing.testsystem.TestSystem
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import spock.lang.Shared
import spock.lang.Specification

import java.sql.Connection

@LiquibaseIntegrationTest
class H2NewIntegrationTest extends Specification {

    @Shared
    TestSystem h2 = TestEnvironmentFactory.getTestSystem("h2");

    def "run test"() {
        when:
        Connection connection = h2.openConnection();
        def statement = connection.prepareStatement("select * from test")
        def rs = statement.executeQuery()

        then:
        rs.next()
    }
}
