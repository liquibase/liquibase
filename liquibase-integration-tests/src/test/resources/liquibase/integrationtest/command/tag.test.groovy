package liquibase.integrationtest.command

import liquibase.integrationtest.setup.SetupDatabaseChangeLog

import static liquibase.integrationtest.command.CommandTest.commandTests
import static liquibase.integrationtest.command.CommandTest.run

commandTests(
        run {
            command "tag"

            setup new SetupDatabaseChangeLog("changelogs/hsqldb/complete/simple.tag.changelog.xml")

            arguments([
                    tag: "version_2.0"
            ])
            expectedOutput ""

            expectedResults([
                    statusMessage: "Successfully executed tag",
                    statusCode   : 0
            ])
        },
)
