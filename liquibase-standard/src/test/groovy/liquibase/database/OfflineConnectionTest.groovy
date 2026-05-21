package liquibase.database

import liquibase.exception.UnexpectedLiquibaseException
import liquibase.resource.ResourceAccessor
import liquibase.database.core.MockDatabase
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

    def "SetConnectionUserName"() {
        when:
        offlineConnection.setConnectionUserName("")

        then:
        offlineConnection.getConnectionUserName() == null

    }

    def "GetConnectionUserName"() {
        when:
        def username = offlineConnection.getConnectionUserName()

        then:
        "superuser" == username
    }

    def "parse-error exception does not leak credentials from a mistyped JDBC URL"() {
        // CWE-209 regression: a user pasting a real JDBC URL where an offline: URL
        // was expected must not see the embedded password echoed back into the
        // exception message (which flows into MDC, log appenders, and the CLI
        // error UI). The constructor routes the input through
        // JdbcConnection.sanitizeUrl before embedding it.
        when:
        new OfflineConnection("jdbc:mysql://liquibaseuser:SuperSecretPwd123@host:3306/db", new JUnitResourceAccessor())

        then:
        UnexpectedLiquibaseException e = thrown()
        e.message.contains("Could not parse offline url")
        !e.message.contains("SuperSecretPwd123")
    }

    def "snapshot-parse exception references the snapshot file, not the full URL"() {
        // CWE-209 regression: offline URLs accept arbitrary key=value params. The
        // snapshot-parse failure path now reports the snapshot file path and the
        // database short name (both already parsed and operationally useful) rather
        // than the raw URL, which can have credentials co-located with snapshot=.
        // ".qqq" guarantees SnapshotParserFactory.getParser throws
        // UnknownFormatException -> LiquibaseException -> line 115's catch clause.
        when:
        new OfflineConnection("offline:postgresql?snapshot=secret-snapshot.qqq&password=SuperSecretPwd123", new JUnitResourceAccessor())

        then:
        UnexpectedLiquibaseException e = thrown()
        e.message.contains("Cannot parse snapshot")
        e.message.contains("secret-snapshot.qqq")
        e.message.contains("postgresql")
        !e.message.contains("SuperSecretPwd123")
    }
}
