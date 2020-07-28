package liquibase.hub.core

import liquibase.hub.model.Environment
import liquibase.hub.model.Project
import spock.lang.Specification
import spock.lang.Unroll

class OnlineHubServiceTest extends Specification {

    private static testUuid = UUID.fromString("3b8b6f80-1194-4a70-8cf2-30f33fd0433e")

    @Unroll
    def "toSearchString"() {
        expect:
        new OnlineHubService().toSearchString(setup) == expected

        where:
        setup                                                                                      | expected
        null                                                                                       | ""
        new Environment(name: "test name")                                                         | 'name:"test name"'
        new Environment(name: "quoted \"test\" data")                                              | 'name:"quoted \\"test\\" data"'
        new Environment(name: "test name", jdbcUrl: "test://jdbc")                                 | 'jdbcUrl:"test://jdbc" AND name:"test name"'
        new Environment(prj: new Project(id: testUuid))                                            | "prj.id:\"$testUuid\""
        new Environment(prj: new Project(id: testUuid, name: "Test Project"))                      | "prj.id:\"$testUuid\""
        new Environment(name: "test name", jdbcUrl: "test://jdbc", prj: new Project(id: testUuid)) | 'jdbcUrl:"test://jdbc" AND name:"test name" AND prj.id:"3b8b6f80-1194-4a70-8cf2-30f33fd0433e"'

    }

    @Unroll
    def "parseDate"() {
        expect:
        String.valueOf(new OnlineHubService().parseDate(date)) == expected

        where:
        date                  | expected
        null                  | "null"
        "2002-03-04"          | "2002-03-04"
        "2002-03-04T05:06:07" | "2002-03-04 05:06:07.0"
    }

}
