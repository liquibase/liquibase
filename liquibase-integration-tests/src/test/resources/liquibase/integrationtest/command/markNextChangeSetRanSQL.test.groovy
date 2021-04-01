package liquibase.integrationtest.command


import liquibase.integrationtest.setup.SetupDatabaseChangeLog

import static liquibase.integrationtest.command.CommandTest.commandTests
import static liquibase.integrationtest.command.CommandTest.run

commandTests(
        run {
            command "markNextChangeSetRanSQL"

            setup new SetupDatabaseChangeLog("changelogs/hsqldb/complete/simple.changelog.xml")

            expectedOutput ""

            expectedResults([
                    statusMessage: "Successfully executed markNextChangeSetRanSQL",
                    statusCode   : 0
            ])
        }
)
