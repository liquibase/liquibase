package liquibase.integrationtest.command

import static liquibase.integrationtest.command.CommandTest.commandTests
import static liquibase.integrationtest.command.CommandTest.run

commandTests(
        run {
            command "releaseLocks"

            expectedOutput ""

            expectedResults([
                    statusMessage: "Successfully executed releaseLocks",
                    statusCode   : 0
            ])
        }
)
