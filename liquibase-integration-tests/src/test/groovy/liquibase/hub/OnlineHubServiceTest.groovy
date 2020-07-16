package liquibase.hub

import liquibase.hub.core.OnlineHubService
import liquibase.hub.model.HubUser
import spock.lang.Specification

import static org.junit.Assume.assumeTrue

class OnlineHubServiceTest extends Specification {

    private OnlineHubService hubService

    private Properties integrationTestProperties
    private boolean hubAvailable;

    def setup() {
        hubService = new OnlineHubService()

        if (integrationTestProperties == null) {
            integrationTestProperties = new Properties();
            integrationTestProperties.load((InputStream) Thread.currentThread().getContextClassLoader().getResourceAsStream("liquibase/liquibase.integrationtest.properties"));
            integrationTestProperties.load((InputStream) Thread.currentThread().getContextClassLoader().getResourceAsStream("liquibase/liquibase.integrationtest.local.properties"));

            def hubUrl = integrationTestProperties.get("integration.test.hub.url")

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
        me.id.toString() == "x-y-z"
        me.name == "x"

    }

    def getOrganization() {
        when:
        def org = hubService.getOrganization()

        then:
        org.id.toString() == "x-y-z"
        org.name == "x"

    }

    def getProjects() {
        when:
        def projects = hubService.getProjects(UUID.randomUUID())

        then:
        projects.size() == 2
    }
}
