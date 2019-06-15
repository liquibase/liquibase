package liquibase.database

import liquibase.resource.ResourceAccessor
import liquibase.sdk.database.MockDatabase
import liquibase.test.JUnitResourceAccessor
import liquibase.test.TestContext
import spock.lang.Specification

class OfflineConnectionTest extends Specification {

    OfflineConnection offlineConnection = null
    ResourceAccessor resourceAccessor

    void setup() {
        resourceAccessor = TestContext.getInstance().getTestResourceAccessor()

        String offlineUrl = "offline:mock?version=1.20&productName=SuperDuperDatabase&catalog=startCatalog" +
                "&caseSensitive=true&changeLogFile=liquibase/database/simpleChangeLog.xml" +
                "&sendsStringParametersAsUnicode=true"

        offlineConnection = (OfflineConnection) DatabaseFactory.getInstance().openConnection(offlineUrl,
                "superuser", "superpass", null, resourceAccessor)
    }

    def "constructor parses query parameters correctly"() {
        when:
        def connection = new OfflineConnection(url, new JUnitResourceAccessor());

        then:
        connection.databaseParams == expectedParams


        where:
        url                      | expectedParams
        "offline:oracle?a=b"     | ["a": "b"]
        "offline:oracle?a=b&c=d" | ["a": "b", "c": "d"]
        "offline:oracle?space=b%20c&d=e" | ["d": "e", "space": "b c"]
        "offline:oracle?eq=b%20%3D%20c&d=e" | ["d": "e", "eq": "b = c"]
        "offline:oracle?fancy%20%3D%20key=b%20%3D%20c&d=e" | ["d": "e", "fancy = key": "b = c"]

    }

    def "IsCorrectDatabaseImplementation"() {
        expect:
        offlineConnection.isCorrectDatabaseImplementation(new MockDatabase())
    }

    def "Attached"() {
        expect:
        offlineConnection.attached new MockDatabase()
    }

    def "GetDatabaseMajorVersion"() {
        when:
        def version = offlineConnection.getDatabaseMajorVersion()

        then:
        1 == version

    }

    def "SetDatabaseMajorVersion"() {
        when:
        offlineConnection.setDatabaseMajorVersion(5)
        def version = offlineConnection.getDatabaseMajorVersion()

        then:
        5 == version
    }

    def "SetProductVersion"() {
        when:
        offlineConnection.setProductVersion("95")
        def version = offlineConnection.getDatabaseProductVersion()

        then:
        "95" == version
    }

    def "SetProductName"() {
        when:
        offlineConnection.setProductName("First Star RDBMS")
        def prodName = offlineConnection.getDatabaseProductName()

        then:
        "First Star RDBMS" == prodName
    }

    def "GetDatabaseMinorVersion"() {
    }

    def "SetDatabaseMinorVersion"() {
        when:
        offlineConnection.setDatabaseMajorVersion(2)
        def version = offlineConnection.getDatabaseMajorVersion()

        then:
        2 == version
    }

    def "GetURL"() {
    }

    def "GetConnectionUserName"() {
        when:
        def username = offlineConnection.getConnectionUserName()

        then:
        "superuser" == username
    }
}
