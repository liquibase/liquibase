package liquibase.configuration


import spock.lang.Specification
import spock.lang.Unroll

class HubConfigurationTest extends Specification {

    @Unroll
    def "setHubUrl cleans up input"() {
        when:
        def config = new HubConfiguration().setLiquibaseHubUrl(input)

        then:
        config.getLiquibaseHubUrl() == expected

        where:
        input                              | expected
        null                               | "https://hub.liquibase.com"
        "https://test.com"                 | "https://test.com"
        "https://hub.liquibase.com"        | "https://hub.liquibase.com"
        "https://localhost:8888"           | "https://localhost:8888"
        "https://test.com:8888"            | "https://test.com:8888"
        "https://test.com:8888/"           | "https://test.com:8888"
        "https://test.com/"                | "https://test.com"
        "https://test.com/other/path"      | "https://test.com"
        "https://test.com:8888/other/path" | "https://test.com:8888"
        "http://localhost"                 | "http://localhost"
        "http://localhost:8080"            | "http://localhost:8080"
    }

    def setGetHubApiKey() {
        when:
        def config = new HubConfiguration().setLiquibaseHubApiKey("this_is_a_hub_key")

        then:
        config.getLiquibaseHubApiKey() == "this_is_a_hub_key"
    }

}
