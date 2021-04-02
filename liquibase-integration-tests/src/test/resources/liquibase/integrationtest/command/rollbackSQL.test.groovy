package liquibase.integrationtest.command

CommandTest.define {
    command = ["rollbackSQL"]

    run {
        arguments = [
                tag          : "version_2.0",
                changeLogFile: "changelogs/hsqldb/complete/rollback.tag.changelog.xml",
        ]

        setup {
            runChangelog "changelogs/hsqldb/complete/rollback.tag.changelog.xml"
        }

        expectedOutput ""

        expectedResults([
                statusMessage: "Successfully executed rollbackSQL",
                statusCode   : 0
        ])
    }
}
