package liquibase.hub.core

import liquibase.configuration.HubConfiguration
import liquibase.configuration.LiquibaseConfiguration
import liquibase.hub.model.Connection
import liquibase.hub.model.OperationChangeEvent
import liquibase.hub.model.Project
import spock.lang.Specification
import spock.lang.Unroll

class StandardHubServiceTest extends Specification {

    private static testUuid = UUID.fromString("3b8b6f80-1194-4a70-8cf2-30f33fd0433e")

    def cleanup() {
        LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class).setLiquibaseHubMode("all")
    }

    @Unroll
    def "toSearchString"() {
        expect:
        new StandardHubService().toSearchString(setup) == expected

        where:
        setup                                                                                         | expected
        null                                                                                          | ""
        new Connection(name: "test name")                                                             | 'name:"test name"'
        new Connection(name: "quoted \"test\" data")                                                  | 'name:"quoted \\"test\\" data"'
        new Connection(name: "test name", jdbcUrl: "test://jdbc")                                     | 'jdbcUrl:"test://jdbc" AND name:"test name"'
        new Connection(project: new Project(id: testUuid))                                            | "project.id:\"$testUuid\""
        new Connection(project: new Project(id: testUuid, name: "Test Project"))                      | "project.id:\"$testUuid\""
        new Connection(name: "test name", jdbcUrl: "test://jdbc", project: new Project(id: testUuid)) | 'jdbcUrl:"test://jdbc" AND name:"test name" AND project.id:"3b8b6f80-1194-4a70-8cf2-30f33fd0433e"'
    }

    @Unroll
    def "parseDate"() {
        expect:
        String.valueOf(new StandardHubService().parseDate(date)) == expected

        where:
        date                  | expected
        null                  | "null"
        "2002-03-04"          | "2002-03-04"
        "2002-03-04T05:06:07" | "2002-03-04 05:06:07.0"
    }

    @Unroll
    def "only metadata sent if mode is metadata"() {
        def orgId = UUID.randomUUID()
        def projectId = UUID.randomUUID()
        def operationId = UUID.randomUUID()

        when:
        LiquibaseConfiguration.getInstance().getConfiguration(HubConfiguration.class).setLiquibaseHubMode(mode)

        def mockHttpClient = new MockHttpClient([
                "GET /api/v1/organizations"                                                                              : MockHttpClient.createListResponse([
                        id  : orgId.toString(),
                        name: "Mock Org"
                ]),
                ("POST /api/v1/organizations/$orgId/projects/$projectId/operations/$operationId/change-events" as String): null,
        ])

        def service = new StandardHubService(
                http: mockHttpClient
        )

        def event = new OperationChangeEvent(
                eventType: "update",
                changesetId: "1",
                project: [
                        id: projectId,
                ],
                operation: [
                        id: operationId,
                ],
                changesetAuthor: "tester",
                generatedSql: ["select sql here"],
                changesetBody: "{ body }"
        )
        service.sendOperationChangeEvent(event)

        def seenRequestBody = mockHttpClient.getRequestBody("POST /api/v1/organizations/$orgId/projects/$projectId/operations/$operationId/change-events")

        then:
        seenRequestBody.changesetBody == (mode == "meta" ? null : "{ body }")
        seenRequestBody.generatedSql == (mode == "meta" ? null : ["select sql here"])

        where:
        mode << ["all", "meta"]
    }

}
