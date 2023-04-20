package liquibase.hub

import liquibase.Scope
import spock.lang.Specification
import spock.lang.Unroll

class HubConfigurationTest extends Specification {

    @Unroll
    def "setHubUrl cleans up input"() {
        when:
        def output = Scope.child([(HubConfiguration.LIQUIBASE_HUB_URL.key): input], new Scope.ScopedRunnerWithReturn<String>() {
            @Override
            String run() throws Exception {
                return HubConfiguration.LIQUIBASE_HUB_URL.getCurrentValue()
            }
        })

        then:
        output == expected

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

    @Unroll
    def "obfuscate api key"() {
        expect:
        Scope.child([(HubConfiguration.LIQUIBASE_HUB_API_KEY.key): input], { ->
            HubConfiguration.LIQUIBASE_HUB_API_KEY.getCurrentValueObfuscated()
        } as Scope.ScopedRunnerWithReturn) == expected

        where:
        input   | expected
        null    | null
        "a"     | "a"
        "ab"    | "ab"
        "abc"   | "abc"
        "abcd"  | "abcd"
        "abcde" | "abcd****"
        "abcdf" | "abcd****"
    }

}
