package liquibase.integration

import spock.lang.Specification
import spock.lang.Unroll

class IntegrationDetailsTest extends Specification {

    @Unroll
    def "setParameter redacts credential-bearing keys: key=#key"() {
        given:
        def details = new IntegrationDetails()

        when:
        details.setParameter(key, value)

        then:
        details.parameters[key] == "*****"

        where:
        key                                   | value
        // CLI argument__ prefix
        "argument__password"                  | "s3cret"
        "argument__referencePassword"         | "ref-s3cret"
        "argument__databaseChangeLogPassword" | "dcl-s3cret"
        // Maven maven__ prefix
        "maven__password"                     | "mvn-s3cret"
        "maven__referencePassword"            | "mvn-ref-s3cret"
        // defaults-file defaultsFile__ prefix
        "defaultsFile__password"              | "df-s3cret"
        // No prefix at all
        "password"                            | "raw-s3cret"
        // Other credential tokens
        "argument__passwd"                    | "abbreviated"
        "argument__apiKey"                    | "abcd1234"
        "argument__authToken"                 | "Bearer eyJ..."
        "argument__client_secret"             | "csecret"
        "argument__accessKey"                 | "AKIA..."
        // "credentials" token — catches awsCredentials, credentialsFile, etc.
        "argument__awsCredentials"            | "ASIA-creds-blob"
        // Case variants
        "argument__PASSWORD"                  | "upper-s3cret"
        "argument__Passwd"                    | "mixed-case"
    }

    @Unroll
    def "setParameter sanitizes JDBC URL values regardless of key name: key=#key"() {
        // URL formats here are limited to what JdbcConnection.sanitizeUrl currently covers
        // (query-string `?password=`, property-string `;password=`, userinfo for mysql/mariadb/oracle).
        // Coverage gaps in sanitizeUrl (e.g., PostgreSQL `user:pass@host` userinfo) are tracked
        // separately (see audit ticket on sanitizeUrl regex coverage); this fix automatically
        // benefits from any future improvement to sanitizeUrl.
        given:
        def details = new IntegrationDetails()

        when:
        details.setParameter(key, value)

        then:
        // Sanitized URL still starts with the JDBC scheme but no longer contains the cleartext password.
        details.parameters[key].startsWith("jdbc:")
        !details.parameters[key].contains("p4ss")

        where:
        key                          | value
        // Query-string-style password (postgresql, snowflake, mysql ?-form)
        "argument__url"              | "jdbc:postgresql://host:5432/db?user=u&password=p4ss"
        "argument__referenceUrl"     | "jdbc:snowflake://account.snowflakecomputing.com?warehouse=W&user=u&password=p4ss"
        "maven__url"                 | "jdbc:mysql://localhost:3306/db?user=u&password=p4ss"
        // Property-string-style password (jtds sqlserver, databricks)
        "defaultsFile__url"          | "jdbc:jtds:sqlserver://h:1433/db;user=u;password=p4ss"
        // Userinfo-style (mysql, mariadb covered by sanitizeUrl)
        "argument__connectionString" | "jdbc:mysql://u:p4ss@host:3306/db"
        // Value-side rule catches non-"url" key names whose value is a JDBC URL — proves the rule
        // is triggered by value, not key.
        "any-future-key"             | "jdbc:mariadb://u:p4ss@host:3306/db"
    }

    @Unroll
    def "setParameter passes non-credential, non-URL values through unchanged: key=#key"() {
        given:
        def details = new IntegrationDetails()

        when:
        details.setParameter(key, value)

        then:
        details.parameters[key] == value

        where:
        key                         | value
        "argument__username"        | "victor"
        "argument__databaseUser"    | "dba"
        "argument__changeLogFile"   | "db.changelog.xml"
        "argument__contexts"        | "prod,qa"
        "argument__labels"          | "release-1"
        "maven__driver"             | "org.postgresql.Driver"
        "defaultsFile__rollbackTag" | "v1.0.0"
        // Plain (non-jdbc:) URL is not auto-sanitized — only jdbc: prefix triggers value-side rule.
        "argument__reportPath"      | "https://example.com/report"
    }

    def "setParameter handles null value by storing null (no mask)"() {
        given:
        def details = new IntegrationDetails()

        when:
        details.setParameter("argument__password", null)

        then:
        details.parameters.containsKey("argument__password")
        details.parameters["argument__password"] == null
    }

    def "setParameter handles null key by storing the value under null (no credential check)"() {
        given:
        def details = new IntegrationDetails()

        when:
        details.setParameter(null, "plain-value")

        then:
        details.parameters[null] == "plain-value"
    }

    def "setParameter with null key still applies value-side JDBC URL sanitization"() {
        given:
        def details = new IntegrationDetails()

        when:
        details.setParameter(null, "jdbc:postgresql://host:5432/db?password=p4ss")

        then:
        details.parameters[null].startsWith("jdbc:")
        !details.parameters[null].contains("p4ss")
    }

    def "setParameter overwrites prior values (still redacted)"() {
        given:
        def details = new IntegrationDetails()

        when:
        details.setParameter("argument__password", "first")
        details.setParameter("argument__password", "second")

        then:
        details.parameters.size() == 1
        details.parameters["argument__password"] == "*****"
    }

    def "name field is unaffected by sanitization"() {
        given:
        def details = new IntegrationDetails()

        when:
        details.setName("cli")

        then:
        details.name == "cli"
        details.parameters.isEmpty()
    }

    def "getParameters returns an unmodifiable view that cannot bypass the sanitize boundary"() {
        // CWE-200 defense in depth (per @filipelautert + @coderabbitai review): a future caller
        // that obtains a reference to getParameters() must not be able to write a raw credential
        // into the map and skip setParameter()'s redaction.
        given:
        def details = new IntegrationDetails()
        details.setParameter("argument__username", "regular-user")

        when:
        details.parameters["argument__password"] = "raw-leak-via-getter"

        then:
        thrown(UnsupportedOperationException)
        // The map still holds the pre-attempt state — the failed put did not partially apply.
        details.parameters["argument__username"] == "regular-user"
        !details.parameters.containsKey("argument__password")
    }

    def "getParameters reads still work normally on the unmodifiable view"() {
        // Confirms the unmodifiable wrap does NOT block legitimate read access patterns the
        // existing tests and downstream consumers already rely on.
        given:
        def details = new IntegrationDetails()
        details.setParameter("argument__password", "s3cret")
        details.setParameter("argument__username", "regular-user")

        expect:
        details.parameters["argument__password"] == "*****"
        details.parameters["argument__username"] == "regular-user"
        details.parameters.containsKey("argument__password")
        details.parameters.size() == 2
    }
}
