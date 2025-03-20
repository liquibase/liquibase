package liquibase.analytics

import fi.iki.elonen.NanoHTTPD
import liquibase.Scope
import liquibase.analytics.configuration.AnalyticsConfigurationFactory
import liquibase.analytics.configuration.LiquibaseRemoteAnalyticsConfiguration
import org.springframework.test.util.TestSocketUtils

import java.util.concurrent.TimeUnit


class TestAnalyticsWebserver extends NanoHTTPD {

    public static final int port = TestSocketUtils.findAvailableTcpPort()
    public static final String writeKey = "not-needed-for-tests"
    public static String postBody = null

    public TestAnalyticsWebserver() throws IOException {
        super(port)
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, true);
        // Clear the cached analytics config info that was loaded when the drop all command step executed automatically during test setup
        AnalyticsConfigurationFactory analyticsConfigurationFactory = Scope.getCurrentScope().getSingleton(AnalyticsConfigurationFactory.class);
        LiquibaseRemoteAnalyticsConfiguration analyticsConfiguration = ((LiquibaseRemoteAnalyticsConfiguration) analyticsConfigurationFactory.getPlugin());
        analyticsConfiguration.remoteAnalyticsConfiguration.clearCache()
    }

    @Override
    public Response serve(IHTTPSession session) {
        if (session.getUri().equals("/config-analytics.yaml") && session.getMethod() == Method.GET) {
            return newFixedLengthResponse("""
timeoutMs: ${TimeUnit.SECONDS.toMillis(60)}
endpointData: http://localhost:$port/v1/batch
sendOss: true
sendPro: true
writeKey: ${writeKey}
anotherProperty: whatever
extensions:
- manifestName: Liquibase MongoDB Commercial Extension
  displayName: ext_mongoDb_commercial
- manifestName: Liquibase DynamoDB Commercial Extension
  displayName: ext_dynamoDb_commercial
- manifestName: Checks Extension
  displayName: ext_checks
- manifestName: AWS Secrets Manager Extension
  displayName: ext_awsSecrets
- manifestName: S3 Remote Accessor Extension
  displayName: ext_awsS3
- manifestName: HashiCorp Vault Extension
  displayName: ext_hashicorpVault
- manifestName: Liquibase BigQuery Commercial Extension
  displayName: ext_bigQuery_commercial
- manifestName: Liquibase Commercial Databricks Extension
  displayName: ext_databricks_commercial
- manifestName: Liquibase Extension Google BigQuery support
  displayName: ext_bigQueryOss
- manifestName: Liquibase MongoDB Extension
  displayName: ext_mongoDbOss
""")
        }
        if (session.getUri().equals("/v1/batch") && session.getMethod() == Method.POST) {
            // This is an intentional way of obtaining the POST request body.
            // See https://stackoverflow.com/a/23171590 for more details.
            Map<String, String> files = new HashMap<String, String>();
            session.parseBody(files);
            postBody = files.get("postData")
            return newFixedLengthResponse("""
{
  "success": true
}
""")
        }
        throw new RuntimeException("Another request was made to the server, and it shouldn't have been.")
    }
}
