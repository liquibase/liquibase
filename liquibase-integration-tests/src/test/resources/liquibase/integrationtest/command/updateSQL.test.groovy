package liquibase.integrationtest.command


import liquibase.integrationtest.setup.SetupDatabaseChangeLog

import static liquibase.integrationtest.command.CommandTest.commandTests
import static liquibase.integrationtest.command.CommandTest.run

commandTests(
        run {
            command "updateSQL"

            setup new SetupDatabaseChangeLog("changelogs/hsqldb/complete/simple.changelog.xml")

            expectedOutput ""

            expectedResults([
                    statusMessage: "Successfully executed updateSQL",
                    statusCode   : 0
            ])
        }
)
