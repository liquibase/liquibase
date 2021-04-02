package liquibase.integrationtest.command

CommandTest.define {
    command = ["updateSQL"]

    run {
        arguments = [
                changeLogFile: "changelogs/hsqldb/complete/simple.changelog.xml"
        ]

        expectedResults = [
                statusMessage: "Successfully executed updateSQL",
                statusCode   : 0
        ]
    }
}
