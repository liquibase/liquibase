package liquibase.hub.core


import liquibase.hub.model.Connection
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
        new Connection(name: "test name")                                                         | 'name:"test name"'
        new Connection(name: "quoted \"test\" data")                                              | 'name:"quoted \\"test\\" data"'
        new Connection(name: "test name", jdbcUrl: "test://jdbc")                                 | 'jdbcUrl:"test://jdbc" AND name:"test name"'
        new Connection(project: new Project(id: testUuid))                                            | "project.id:\"$testUuid\""
        new Connection(project: new Project(id: testUuid, name: "Test Project"))                      | "project.id:\"$testUuid\""
        new Connection(name: "test name", jdbcUrl: "test://jdbc", project: new Project(id: testUuid)) | 'jdbcUrl:"test://jdbc" AND name:"test name" AND project.id:"3b8b6f80-1194-4a70-8cf2-30f33fd0433e"'

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
