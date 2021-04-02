package liquibase.integrationtest.command

CommandTest.define {
    command = ["migrate"]

    run {
        arguments = [
                changeLogFile: "changelogs/hsqldb/complete/simple.changelog.xml",
        ]

        expectedOutput ""

        expectedResults([
                statusMessage: "Successfully executed migrate",
                statusCode   : 0
        ])
    }
}
