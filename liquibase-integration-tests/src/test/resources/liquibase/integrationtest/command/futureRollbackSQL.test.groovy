package liquibase.integrationtest.command

CommandTest.define {
    command = ["futureRollbackSQL"]

    run {
        arguments = [
                count        : 1,
                changeLogFile: "changelogs/hsqldb/complete/rollback.changelog.xml",
        ]

        setup {
            runChangelog "changelogs/hsqldb/complete/rollback.changelog.xml"
            rollback 5, "changelogs/hsqldb/complete/rollback.changelog.xml"

        }

        expectedOutput ""
        expectedResults([
                statusMessage: "Successfully executed futureRollbackSQL",
                statusCode   : 0
        ])
    }
}
