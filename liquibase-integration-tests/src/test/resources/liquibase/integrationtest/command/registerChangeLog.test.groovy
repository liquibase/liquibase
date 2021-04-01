package liquibase.integrationtest.command

import liquibase.integrationtest.setup.SetupDatabaseChangeLogFile

import static liquibase.integrationtest.command.CommandTest.commandTests
import static liquibase.integrationtest.command.CommandTest.run

commandTests(
    run {
        command "registerChangeLog"

        setup new SetupDatabaseChangeLogFile("changelogs/hsqldb/complete/simple.changelog.xml")

        needDatabaseChangeLog true
        expectedOutput ""

        arguments hubProjectName: "Project 1"

        expectedResults([
                statusMessage: "Successfully executed registerChangeLog",
                statusCode: 0
        ])
    }
)