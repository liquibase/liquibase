package liquibase.integrationtest.command

CommandTest.define {
    command = ["rollbackToDate"]

    run {
        arguments = [
                date         : "2021-03-25T09:00:00",
                changeLogFile: "changelogs/hsqldb/complete/rollback.changelog.xml",
        ]

        setup {
            runChangelog "changelogs/hsqldb/complete/rollback.changelog.xml"
        }

        expectedResults = [
                statusMessage: "Successfully executed rollbackToDate",
                statusCode   : 0
        ]
    }
}
