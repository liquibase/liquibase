package liquibase.analytics

import liquibase.Scope
import liquibase.analytics.configuration.AnalyticsArgs
import liquibase.command.util.CommandUtil
import liquibase.extension.testing.testsystem.DatabaseTestSystem
import liquibase.extension.testing.testsystem.TestSystemFactory
import liquibase.extension.testing.testsystem.spock.LiquibaseIntegrationTest
import liquibase.util.LiquibaseUtil
import liquibase.util.SystemUtil
import org.yaml.snakeyaml.Yaml
import spock.lang.IgnoreIf
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.TimeUnit

@LiquibaseIntegrationTest
class AnalyticsIntegrationTest extends Specification {

    @Shared
    private DatabaseTestSystem h2 = Scope.currentScope.getSingleton(TestSystemFactory).getTestSystem("h2") as DatabaseTestSystem

    /**
     * This test starts up a simple webserver that runs in a separate thread from the test thread, and handles the two
     * API calls made by the analytics code.
     * Because the webserver thread is separate from the test thread, when debugging the analytics code, you'll likely
     * need to right click on your breakpoint and change "suspend" from "all" to "thread". If you suspend all of the
     * threads, the webserver won't respond to the calls made by the API. Additionally, the analytics API calls
     * themselves occur in a different thread, which is done so that the thread can be killed if the calls exceed
     * a preset amount of time. That timeout was increased to 1 minute for this test, but if your debugging takes
     * longer than a minute, the test will unexpectedly die.
     */
    def "test sending analytics happy path"() {
        setup:
        TestAnalyticsWebserver simpleWebserver = new TestAnalyticsWebserver()

        when:
        executeCommandWithAnalytics(simpleWebserver, () -> {
            CommandUtil.runDropAll(h2)
        } as Scope.ScopedRunner)


        def body = simpleWebserver.postBody
        then:
        body != null

        Yaml yaml = new Yaml()
        def loadedBody = yaml.loadAs(body, Map<String, ?>)

        loadedBody.get("context") == "null"
        loadedBody.get("writeKey") == simpleWebserver.writeKey
        def batch = loadedBody.get("batch")
        batch instanceof ArrayList
        batch.size() == 1
        def trackEvent = batch.get(0)
        trackEvent instanceof LinkedHashMap
        UUID.fromString(trackEvent.get("anonymousId")) // is a UUID
        trackEvent.get("context") == "null"
        trackEvent.get("event") == "liquibase-command-executed"
        UUID.fromString(trackEvent.get("messageId")) // is a UUID
        trackEvent.get("type") == "track"
        trackEvent.get("userId") == "null" // no pro license available
        def properties = trackEvent.get("properties")
        properties.get("chlog_formattedSql") == 0
        properties.get("chlog_json") == 0
        properties.get("chlog_sql") == 0
        properties.get("chlog_xml") == 0
        properties.get("chlog_yaml") == 0
        properties.get("command") == "dropAll"
        properties.get("databasePlatform") == h2.getDatabaseFromFactory().getDatabaseProductName()
        properties.get("databaseVersion") == h2.getDatabaseFromFactory().getDatabaseProductVersion()
        properties.get("dbclhEnabled") == false
        properties.get("exceptionClass") == "null"
        properties.get("ext_awsS3") == "null"
        properties.get("ext_awsSecrets") == "null"
        properties.get("ext_bigQuery_commercial") == "null"
        properties.get("ext_bigQueryOss") == "null"
        properties.get("ext_checks") == "null"
        properties.get("ext_databricks_commercial") == "null"
        properties.get("ext_dynamoDb_commercial") == "null"
        properties.get("ext_hashicorpVault") == "null"
        properties.get("ext_mongoDb_commercial") == "null"
        properties.get("ext_mongoDbOss") == "null"
        properties.get("javaVersion") == SystemUtil.getJavaVersion()
        properties.get("liquibaseInterface") == "JavaAPI"
        properties.get("liquibaseVersion") == LiquibaseUtil.getBuildVersionInfo()
        properties.get("operationOutcome") == "success"
        properties.get("os") == System.getProperty("os.name")
        properties.get("osArch") == System.getProperty("os.arch")
        properties.get("osVersion") == System.getProperty("os.version")
        properties.get("reportsEnabled") == false
        properties.get("structuredLogsEnabled") == false
        properties.get("isDocker") == false
        properties.get("isLiquibaseDocker") == false
        properties.get("isAwsLiquibaseDocker") == false
        properties.get("isCi") != null
        properties.get("isGithubActions") != null
        properties.get("isIO") == false

        cleanup:
        simpleWebserver.stop()
    }

    @IgnoreIf({ !System.getenv("GITHUB_ACTIONS") })
    def "test only in GitHub Actions"() {
        setup:
        TestAnalyticsWebserver simpleWebserver = new TestAnalyticsWebserver()

        when:
        executeCommandWithAnalytics(simpleWebserver, () -> {
            CommandUtil.runDropAll(h2)
        } as Scope.ScopedRunner)


        def body = simpleWebserver.postBody
        then:
        Yaml yaml = new Yaml()
        def loadedBody = yaml.loadAs(body, Map<String, ?>)
        def batch = loadedBody.get("batch")
        def trackEvent = batch.get(0)
        def properties = trackEvent.get("properties")
        properties.get("isCi") == true
        properties.get("isGithubActions") == true

        cleanup:
        simpleWebserver.stop()
    }

    static void executeCommandWithAnalytics(TestAnalyticsWebserver simpleWebserver, Scope.ScopedRunner scopedRunner) {
        Map<String, ?> scopeVars = new HashMap<>()
        scopeVars.put(AnalyticsArgs.CONFIG_ENDPOINT_URL.getKey(), "http://localhost:" + simpleWebserver.getListeningPort() + "/config-analytics.yaml")
        scopeVars.put(AnalyticsArgs.CONFIG_ENDPOINT_TIMEOUT_MILLIS.getKey(), TimeUnit.SECONDS.toMillis(60)) // to allow for debugging, otherwise the thread gets killed fast
        scopeVars.put(AnalyticsArgs.DEV_OVERRIDE.getKey(), true)
        scopeVars.put(AnalyticsArgs.ENABLED.getKey(), true)
        scopeVars.put(Scope.Attr.maxAnalyticsCacheSize.name(), 1)
        Scope.child(scopeVars, scopedRunner)
    }
}
