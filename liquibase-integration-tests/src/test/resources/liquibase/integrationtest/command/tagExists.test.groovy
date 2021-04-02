package liquibase.integrationtest.command

CommandTest.define {
    command = ["tagExists"]

    run {
        arguments = [
                tag          : "version_2.0",
                changeLogFile: "changelogs/hsqldb/complete/simple.tag.changelog.xml",
        ]

        expectedOutput ""

        expectedResults([
                statusMessage: "Successfully executed tagExists",
                statusCode   : 0
        ])
    }
}
