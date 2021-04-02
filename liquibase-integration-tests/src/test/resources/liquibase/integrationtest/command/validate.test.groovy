package liquibase.integrationtest.command

CommandTest.define {
    command = ["validate"]

    run {
        arguments = [
                changeLogFile: "changelogs/hsqldb/complete/simple.changelog.xml"
        ]

        expectedOutput ""

        expectedResults([
                statusMessage: "Successfully executed validate",
                statusCode   : 0
        ])
    }
}
