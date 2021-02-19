package liquibase.hub

import liquibase.configuration.HubConfiguration
import liquibase.configuration.LiquibaseConfiguration
import liquibase.hub.core.StandardHubService
import liquibase.hub.model.Connection
import spock.lang.Specification

import static org.junit.Assume.assumeTrue

class StandardHubServiceTest extends Specification {

    private StandardHubService hubService

    private Properties integrationTestProperties

    private UUID knownProjectId = UUID.fromString("ce1a237e-e005-4288-a243-4856823a25a6")
    private UUID knownConnectionId = UUID.fromString("d92e6505-cd0f-4e91-bb66-b12e6a285184")

    def setup() {
        if (integrationTestProperties == null) {
            integrationTestProperties = new Properties()
            integrationTestProperties.load((InputStream) Thread.currentThread().getContextClassLoader().getResourceAsStream("liquibase/liquibase.integrationtest.properties"))

            def localFileStream = (InputStream) Thread.currentThread().getContextClassLoader().getResourceAsStream("liquibase/liquibase.integrationtest.local.properties")
            if (localFileStream != null) {
                integrationTestProperties.load(localFileStream)
            }

            def hubApiKey = integrationTestProperties.get("integration.test.hub.apikey")
            def hubUrl = integrationTestProperties.get("integration.test.hub.url")
            HubConfiguration hubConfiguration = LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class)
            hubConfiguration.setLiquibaseHubApiKey(hubApiKey)
            hubConfiguration.setLiquibaseHubUrl(hubUrl)

        }

        hubService = new StandardHubService()
        assumeTrue("Liquibase Hub is not available for testing", hubService.isHubAvailable())
    }

    def getMe() {
        when:
        def hubUser = integrationTestProperties.get("integration.test.hub.userName")
        def me = hubService.getMe()

        then:
        me.id != null
        me.username == hubUser

    }

    def getOrganization() {
        when:
        def hubOrgId = UUID.fromString(integrationTestProperties.get("integration.test.hub.orgId"))
        def org = hubService.getOrganization()

        then:
        org.id == hubOrgId
    }
  /*

    def getProjects() {
        when:
        def projects = hubService.getProjects()

        then:
        projects.size() >= 1
    }

    def "404 errors are thrown as the correct exceptions"() {
        when:
        hubService.getEnvironments(new Environment(name: "invalid name"))

        then:
        def e = thrown(LiquibaseHubObjectNotFoundException)
        e.message.contains("not found")
    }

    def getEnvironment() {
        when:
        def environment = hubService.getEnvironment(knownEnvironmentId)

        then:
        environment.id == knownEnvironmentId
        environment.name == "dooriblon Environment"

    }
*/
    def "getConnection throws exception if 404"() {
        when:
        def connection = new Connection()
        connection.setId(UUID.randomUUID())
        hubService.getConnection(connection, false)

        then:
        def e = thrown(LiquibaseHubObjectNotFoundException)
        e.message.contains("not found")

    }

    def "getConnections can return all connections"() {
        when:
        String jdbcUrl = integrationTestProperties.get("integration.test.oracle.url")
        jdbcUrl = jdbcUrl.replaceAll("//","")
        def hubUrl = integrationTestProperties.get("integration.test.hub.url")
        def connections = hubService.getConnections(null)

        then:
        connections*.id.toString() != null
        connections*.name.toString() contains jdbcUrl
        connections*.jdbcUrl.toString() == "[" + jdbcUrl + "]"
    }

    /*
     @Unroll
     def "getConnections can search"() {
         when:
         def connections = hubService.getConnections(new Connection(search))

         then:
         connections*.name.toString() == expectedName

         where:
         search                                                                                            | expectedName
         [name: "dooriblon Environment"]                                                                   | "[dooriblon Environment]"
         [jdbcUrl: "jdbc:postgresql://localhost:5432/liquibase-hub- f448a409-1c73-421a-a03c-fd2a146e4c0d"] | "[dooriblon Environment]"
     }

     def createEnvironment() {
         setup:
         def randomNumber = new Random().nextInt()


         when:
         def newEnv = hubService.createEnvironment(new Environment(
                 name: "New Env $randomNumber",
                 jdbcUrl: "jdbc://test-$randomNumber",
         ))

         then:
         newEnv.id != null
         newEnv.name == "New Env $randomNumber"
         newEnv.jdbcUrl == "jdbc://test-$randomNumber"
         newEnv.createDate != null
         newEnv.updateDate == null
         newEnv.removeDate == null
     }

     def "setRanChangeSets"() {

         hubService.setRanChangeSets(knownEnvironmentId, [
                 new RanChangeSet("com/example/changelog.xml", "1", "test", CheckSum.parse("1:a"), new Date(), null, ChangeSet.ExecType.EXECUTED, "Test changeset 1", "Comments 1", new ContextExpression(), new Labels(), "123"),
                 new RanChangeSet("com/example/changelog.xml", "2", "other", CheckSum.parse("1:b"), new Date(), null, ChangeSet.ExecType.EXECUTED, "Test changeset 2", "Comments 2", new ContextExpression("a", "b"), new Labels("c", "d"), "123"),
                 new RanChangeSet("com/example/changelog.xml", "3", "test", CheckSum.parse("1:c"), new Date(), null, ChangeSet.ExecType.SKIPPED, "Test changeset 3", "Comments 3", new ContextExpression(), new Labels(), "445"),
         ])
     }
     */
}
