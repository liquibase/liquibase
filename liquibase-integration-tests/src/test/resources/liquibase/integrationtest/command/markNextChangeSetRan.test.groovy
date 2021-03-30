package liquibase.integrationtest.command

import liquibase.integrationtest.command.CommandTest
import liquibase.integrationtest.setup.SetupDatabaseChangeLog

[
    new CommandTest.Spec(
        command: ["markNextChangeSetRan"],

        setup: [
            new SetupDatabaseChangeLog("changelogs/hsqldb/complete/simple.changelog.xml")
        ],
        expectedOutput: [
            "",
        ],

        expectedResults: [
            statusMessage: "Successfully executed markNextChangeSetRan",
            statusCode: 0
        ]
    )

] as CommandTest.Spec[]
