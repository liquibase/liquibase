package liquibase.integrationtest.command

import liquibase.integrationtest.command.CommandTest
import liquibase.integrationtest.setup.SetupDatabaseChangeLog

[
    new CommandTest.Spec(
        command: ["updateCount"],

        setup: [
            new SetupDatabaseChangeLog("changelogs/hsqldb/complete/simple.changelog.xml")
        ],
        arguments: [
            count: 1
        ],
        expectedOutput: [
            "",
        ],
        expectedResults: [
            statusMessage: "Successfully executed updateCount",
            statusCode: 0
        ]
    )

] as CommandTest.Spec[]
