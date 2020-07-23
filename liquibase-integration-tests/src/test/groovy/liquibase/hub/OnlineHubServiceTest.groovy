package liquibase.hub

import liquibase.configuration.HubConfiguration
import liquibase.configuration.LiquibaseConfiguration
import liquibase.hub.core.OnlineHubService
import liquibase.hub.model.Environment
import spock.lang.Specification
import spock.lang.Unroll

import static org.junit.Assume.assumeTrue

class OnlineHubServiceTest extends Specification {

    private OnlineHubService hubService

    private Properties integrationTestProperties
    private boolean hubAvailable

    private UUID exampleProjectId = UUID.fromString("ce1a237e-e005-4288-a243-4856823a25a6")

    def setup() {
        hubService = new OnlineHubService()

        if (integrationTestProperties == null) {
            integrationTestProperties = new Properties()
            integrationTestProperties.load((InputStream) Thread.currentThread().getContextClassLoader().getResourceAsStream("liquibase/liquibase.integrationtest.properties"))
            integrationTestProperties.load((InputStream) Thread.currentThread().getContextClassLoader().getResourceAsStream("liquibase/liquibase.integrationtest.local.properties"))

            def hubApiKey = integrationTestProperties.get("integration.test.hub.apikey")
            def hubUrl = integrationTestProperties.get("integration.test.hub.url")
            HubConfiguration hubConfiguration = LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class)
            hubConfiguration.setLiquibaseHubApiKey(hubApiKey)
            hubConfiguration.setLiquibaseHubUrl(hubUrl)

            try {
                def me = hubService.getMe()
                hubAvailable = true
            } catch (LiquibaseHubException e) {
                println "Hub is not available: $e.message"

                hubAvailable = false
            }

        }

        assumeTrue("Liquibase Hub is not available for testing", hubAvailable)
    }

    def getMe() {
        when:
        def me = hubService.getMe()

        then:
        me.id != null
        me.username == "ruslan"

    }

    def getOrganization() {
        when:
        def org = hubService.getOrganization()

        then:
        org.id != null
        org.name == "ruslan's Personal Organization"

    }

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

    def "getEnvironments can return all environments"() {
        when:
        def environments = hubService.getEnvironments(null)

        then:
        environments*.id.toString() == "[d92e6505-cd0f-4e91-bb66-b12e6a285184]"
        environments*.name.toString() == "[dooriblon Environment]"
        environments*.jdbcUrl.toString() == "[jdbc:postgresql://localhost:5432/liquibase-hub- f448a409-1c73-421a-a03c-fd2a146e4c0d]"
    }

    @Unroll
    def "getEnvironments can search"() {
        when:
        def environments = hubService.getEnvironments(new Environment(search))

        then:
        environments*.name.toString() == expectedName

        where:
        search                                                                                            | expectedName
        [name: "dooriblon Environment"]                                                                   | "[dooriblon Environment]"
        [jdbcUrl: "jdbc:postgresql://localhost:5432/liquibase-hub- f448a409-1c73-421a-a03c-fd2a146e4c0d"] | "[dooriblon Environment]"
    }

    def createEnvironment() {
        setup:
        def randomNumber = new Random().nextInt()


        when:
        def newEnv = hubService.createEnvironment(exampleProjectId, new Environment(
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
}
