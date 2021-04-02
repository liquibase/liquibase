package liquibase.integrationtest.command

CommandTest.define {
    command = ["update"]

    run {
        arguments = [
                changeLogFile: "changelogs/hsqldb/complete/simple.changelog.xml"
        ]

        expectedOutput ""

        expectedResults([
                statusMessage: "Successfully executed update",
                statusCode   : 0
        ])
    }
}
