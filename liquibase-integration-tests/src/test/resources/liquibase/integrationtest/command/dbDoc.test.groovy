package liquibase.integrationtest.command


import liquibase.integrationtest.setup.SetupDatabaseChangeLog

import static liquibase.integrationtest.command.CommandTest.commandTests
import static liquibase.integrationtest.command.CommandTest.run

commandTests(
        run {
            command "dbDoc"

            setup new SetupDatabaseChangeLog("changelogs/hsqldb/complete/simple.changelog.xml")

            expectedOutput ""

            arguments([
                    outputDirectory: "target/test-classes"
            ])

            expectedResults([
                    statusMessage: "Successfully executed dbDoc",
                    statusCode   : 0
            ])
        }
)
