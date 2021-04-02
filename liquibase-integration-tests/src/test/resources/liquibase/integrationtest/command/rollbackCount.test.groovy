package liquibase.integrationtest.command

CommandTest.define {
    command = ["rollbackCount"]

    run {
        arguments = [
                count        : 1,
                changeLogFile: "changelogs/hsqldb/complete/rollback.changelog.xml"
        ]

        setup {
            runChangelog "changelogs/hsqldb/complete/rollback.changelog.xml"
        }

        expectedOutput ""

        expectedResults([
                statusMessage: "Successfully executed rollbackCount",
                statusCode   : 0
        ])
    }
}
