package liquibase.integrationtest.command

CommandTest.define {
    command = ["unexpectedChangeSets"]

    run {
        arguments = [
                verbose      : "true",
                changeLogFile: "changelogs/hsqldb/complete/unexpected.tag.changelog.xml",
        ]

        setup {
            syncChangelog "changelogs/hsqldb/complete/rollback.tag.changelog.xml"
        }

        expectedOutput ""

        expectedResults([
                statusMessage: "Successfully executed unexpectedChangeSets",
                statusCode   : 0
        ])
    }
}
