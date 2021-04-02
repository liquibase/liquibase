package liquibase.integrationtest.command

CommandTest.define {
    command = ["deactivateChangeLog"]
    run {

        arguments = [
                changeLogFile: "changelogs/hsqldb/complete/simple.changelog.with.id.xml",
        ]

        expectedOutput ""

        expectedResults([
                statusMessage: "Successfully executed deactivateChangeLog",
                statusCode   : 0
        ])
    }
}
