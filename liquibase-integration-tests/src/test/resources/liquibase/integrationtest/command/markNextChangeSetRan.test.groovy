package liquibase.integrationtest.command

CommandTest.define {
    command = ["markNextChangeSetRan"]

    run {
        arguments = [
                changeLogFile: "changelogs/hsqldb/complete/simple.changelog.xml"
        ]

        expectedOutput ""

        expectedResults([
                statusMessage: "Successfully executed markNextChangeSetRan",
                statusCode   : 0
        ])
    }
}
