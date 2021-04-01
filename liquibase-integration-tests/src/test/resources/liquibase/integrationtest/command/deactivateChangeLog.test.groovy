package liquibase.integrationtest.command


import liquibase.integrationtest.setup.SetupDatabaseChangeLog

import static liquibase.integrationtest.command.CommandTest.commandTests
import static liquibase.integrationtest.command.CommandTest.run

commandTests(
    run {
            command "deactivateChangeLog"

            setup(new SetupDatabaseChangeLog("changelogs/hsqldb/complete/simple.changelog.with.id.xml"))

            needDatabaseChangeLog true

            expectedOutput ""

            expectedResults([
                    statusMessage: "Successfully executed deactivateChangeLog",
                    statusCode   : 0
            ])
    }
)