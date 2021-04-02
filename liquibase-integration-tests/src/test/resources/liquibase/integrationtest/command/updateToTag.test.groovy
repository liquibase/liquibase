package liquibase.integrationtest.command

CommandTest.define {
    command = ["updateToTag"]

    run {
        arguments = [
                tag          : "version_2.0",
                changeLogFile: "changelogs/hsqldb/complete/simple.tag.changelog.xml",
        ]


        expectedResults = [
                statusMessage: "Successfully executed updateToTag",
                statusCode   : 0
        ]
    }
}
