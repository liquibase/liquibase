package liquibase.integrationtest.command

CommandTest.define {
    command = ["markNextChangeSetRanSQL"]

    run {

        arguments = [
                "changeLogFile": "changelogs/hsqldb/complete/simple.changelog.xml"
        ]

        expectedResults = [
                statusMessage: "Successfully executed markNextChangeSetRanSQL",
                statusCode   : 0
        ]
    }
}
