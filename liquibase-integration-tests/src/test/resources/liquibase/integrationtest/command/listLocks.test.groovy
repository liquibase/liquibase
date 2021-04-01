package liquibase.integrationtest.command

import static liquibase.integrationtest.command.CommandTest.commandTests
import static liquibase.integrationtest.command.CommandTest.run

commandTests(
        run {
            command "listLocks"

            expectedOutput ""

            expectedResults([
                    statusMessage: "Successfully executed listLocks",
                    statusCode   : 0
            ])
        }
)
