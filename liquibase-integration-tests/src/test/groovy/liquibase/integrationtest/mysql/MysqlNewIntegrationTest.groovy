package liquibase.integrationtest.mysql

import liquibase.extension.testing.environment.TestEnvironment
import liquibase.extension.testing.environment.TestEnvironmentFactory
import liquibase.extension.testing.environment.spock.LiquibaseIntegrationTest
import spock.lang.Shared
import spock.lang.Specification

import java.sql.Connection

@LiquibaseIntegrationTest
class MysqlNewIntegrationTest extends Specification {

    @Shared
    TestEnvironment mysql = TestEnvironmentFactory.getEnvironment("mysql");

    def "run test"() {
        when:
        Connection connection = mysql.openConnection();
        def statement = connection.prepareStatement("select * from test")
        def rs = statement.executeQuery()

        then:
        rs.next()
    }
}
