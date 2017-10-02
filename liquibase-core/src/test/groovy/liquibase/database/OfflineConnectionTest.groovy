package liquibase.database

import liquibase.resource.ResourceAccessor
import liquibase.sdk.database.MockDatabase
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

    void cleanup() {
    }

    def "IsCorrectDatabaseImplementation"() {
        expect:
        offlineConnection.isCorrectDatabaseImplementation(new MockDatabase())
    }

    def "Attached"() {
        expect:
        offlineConnection.attached new MockDatabase()
    }

    def "CreateChangeLogHistoryService"() {
    }

    def "GetSnapshot"() {
    }

    def "SetSnapshot"() {
    }

    def "Close"() {
    }

    def "Commit"() {
    }

    def "GetAutoCommit"() {
    }

    def "SetAutoCommit"() {
    }

    def "GetCatalog"() {
    }

    def "GetSchema"() {
    }

    def "NativeSQL"() {
    }

    def "Rollback"() {
    }

    def "GetDatabaseProductName"() {
    }

    def "GetDatabaseProductVersion"() {
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

    def "SetConnectionUserName"() {
    }

    def "IsClosed"() {
    }

    def "GetSendsStringParametersAsUnicode"() {
    }

    def "SetSendsStringParametersAsUnicode"() {
    }

    def "IsCaseSensitive"() {
    }

    def "SetCaseSensitive"() {
    }
}
