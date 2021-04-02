package liquibase.integrationtest.command

CommandTest.define {
    command = ["updateCount"]

    run {
        arguments = [
                count        : 1,
                changeLogFile: "changelogs/hsqldb/complete/simple.changelog.xml",
        ]

        expectedResults = [
                statusMessage: "Successfully executed updateCount",
                statusCode   : 0
        ]
    }
}
