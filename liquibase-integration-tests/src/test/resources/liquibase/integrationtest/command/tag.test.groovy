package liquibase.integrationtest.command

import liquibase.integrationtest.command.CommandTest
import liquibase.integrationtest.setup.SetupDatabaseChangeLog

[
    new CommandTest.Spec(
        command: ["tag"],

        setup: [
            new SetupDatabaseChangeLog("changelogs/hsqldb/complete/simple.tag.changelog.xml")
        ],
        arguments: [
            tag: "version_2.0"
        ],
        expectedOutput: [
            "",
        ],
        expectedResults: [
            statusMessage: "Successfully executed tag",
            statusCode: 0
        ]
    )

] as CommandTest.Spec[]
