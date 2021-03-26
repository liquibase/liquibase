package liquibase.integrationtest.command

import liquibase.integrationtest.command.CommandTest
import liquibase.integrationtest.setup.SetupDatabaseChangeLog

[
    new CommandTest.Spec(
        command: ["updateToTagSQL"],

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
            statusMessage: "Successfully executed updateToTagSQL",
            statusCode: 0
        ]
    )

] as CommandTest.Spec[]
