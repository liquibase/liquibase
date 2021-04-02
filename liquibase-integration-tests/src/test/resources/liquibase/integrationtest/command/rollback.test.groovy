package liquibase.integrationtest.command

CommandTest.define {
    command = ["rollback"]

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
                statusMessage: "Successfully executed rollback",
                statusCode   : 0
        ])
    }
}
