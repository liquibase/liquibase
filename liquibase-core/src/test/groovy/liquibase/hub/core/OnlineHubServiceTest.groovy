package liquibase.hub.core

import liquibase.hub.model.Environment
import spock.lang.Specification
import spock.lang.Unroll

class OnlineHubServiceTest extends Specification {

    @Unroll
    def "toSearchString"() {
        expect:
        new OnlineHubService().toSearchString(setup) == expected

        where:
        setup                                                      | expected
        null                                                       | ""
        new Environment(name: "test name")                         | 'name:"test name"'
        new Environment(name: "quoted \"test\" data")              | 'name:"quoted \\"test\\" data"'
        new Environment(name: "test name", jdbcUrl: "test://jdbc") | 'jdbcUrl:"test://jdbc" AND name:"test name"'

    }

}
