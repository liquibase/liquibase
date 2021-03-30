package liquibase.integrationtest.command

import liquibase.integrationtest.command.CommandTest
import liquibase.integrationtest.setup.SetupDatabaseChangeLog

[
    new CommandTest.Spec(
        command: ["validate"],

        setup: [
            new SetupDatabaseChangeLog("changelogs/hsqldb/complete/simple.changelog.xml")
        ],
        expectedOutput: [
            "",
        ],

        expectedResults: [
            statusMessage: "Successfully executed validate",
            statusCode: 0
        ]
    )

] as CommandTest.Spec[]
