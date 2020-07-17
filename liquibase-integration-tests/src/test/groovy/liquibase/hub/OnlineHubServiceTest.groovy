package liquibase.hub

import liquibase.configuration.HubConfiguration
import liquibase.configuration.LiquibaseConfiguration
import liquibase.hub.core.OnlineHubService
import liquibase.hub.model.HubUser
import spock.lang.Specification

import static org.junit.Assume.assumeTrue

class OnlineHubServiceTest extends Specification {

    private OnlineHubService hubService

    private Properties integrationTestProperties
    private boolean hubAvailable

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
        me.id.toString() == "0c58fc42-7d81-4b92-8a00-0ac4d5784d5e"
        me.username == "wwillard7800"

    }

    def getOrganization() {
        when:
        def org = hubService.getOrganization()

        then:
        org.id.toString() == "bec54f77-c255-4566-aee1-66d80e4a9f41"
        org.name == "wwillard7800's Personal Organization"

    }

    def getProjects() {
        when:
        def projects = hubService.getProjects()

        then:
        projects.size() >=1
    }
}
