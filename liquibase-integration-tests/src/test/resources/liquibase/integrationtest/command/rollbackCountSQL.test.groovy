package liquibase.integrationtest.command

CommandTest.define {
    command = ["rollbackCountSQL"]

    run {
        arguments = [
                count        : 1,
                changeLogFile: "changelogs/hsqldb/complete/rollback.changelog.xml",
        ]


        setup {
            runChangelog "changelogs/hsqldb/complete/rollback.changelog.xml"
        }

        expectedResults = [
                statusMessage: "Successfully executed rollbackCountSQL",
                statusCode   : 0
        ]
    }
}
