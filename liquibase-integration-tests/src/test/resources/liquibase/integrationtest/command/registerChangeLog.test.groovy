package liquibase.integrationtest.command


CommandTest.define {
    command = ["registerChangeLog"]

    run {
        arguments = [
                hubProjectName   : "Project 1",
                changelogFileName: "changelogs/hsqldb/complete/simple.changelog.xml"
        ]

        expectedOutput ""

        expectedResults([
                statusMessage: "Successfully executed registerChangeLog",
                statusCode   : 0
        ])
    }
}
