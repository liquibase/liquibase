package liquibase.integrationtest.command

CommandTest.define {
    command = ["dbDoc"]

    run {
        arguments = [
                outputDirectory: "target/test-classes",
                changeLogFile  : "changelogs/hsqldb/complete/simple.changelog.xml",
        ]

        expectedOutput ""

        expectedResults([
                statusMessage: "Successfully executed dbDoc",
                statusCode   : 0
        ])
    }
}
