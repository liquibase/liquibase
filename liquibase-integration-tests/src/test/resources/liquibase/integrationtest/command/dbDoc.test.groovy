package liquibase.integrationtest.command

import liquibase.integrationtest.command.CommandTest
import liquibase.integrationtest.setup.SetupDatabaseChangeLog

[
    new CommandTest.Spec(
        command: ["dbDoc"],

        setup: [
            new SetupDatabaseChangeLog("changelogs/hsqldb/complete/simple.changelog.xml")
        ],
        expectedOutput: [
            "",
        ],
        arguments: [
            outputDirectory: "target/test-classes"
        ],
        expectedResults: [
            statusMessage: "Successfully executed dbDoc",
            statusCode: 0
        ]
    )

] as CommandTest.Spec[]
