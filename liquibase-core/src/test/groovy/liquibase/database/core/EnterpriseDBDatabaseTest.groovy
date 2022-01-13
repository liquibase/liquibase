package liquibase.database.core

import liquibase.database.MockDatabaseConnection
import spock.lang.Specification
import spock.lang.Unroll

class EnterpriseDBDatabaseTest extends Specification {

    @Unroll
    def "isCorrectDatabaseImplementation"() {
        when:
        def returnUrl = url

        def conn = new MockDatabaseConnection() {
            @Override
            String getURL() {
                return returnUrl
            }
        }


        then:
        assert new EnterpriseDBDatabase().isCorrectDatabaseImplementation(conn) == expected

        where:
        url                            | expected
        "jdbc:edb:localhost"           | true
        "jdbc:postgres:localhost:5444" | true
        "jdbc:postgres:localhost:5432" | false
        "jdbc:postgresql:localhost"    | false
        "jdbc:db2:localhost/thedb"     | false
    }
}
