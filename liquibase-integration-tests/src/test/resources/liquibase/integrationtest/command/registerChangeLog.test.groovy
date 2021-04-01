package liquibase.integrationtest.command

import liquibase.integrationtest.setup.SetupDatabaseChangeLogFile

CommandTest.define {
    command = ["registerChangeLog"]

    run {
        arguments = [
                hubProjectName: "Project 1"
        ]

        setup new SetupDatabaseChangeLogFile("changelogs/hsqldb/complete/simple.changelog.xml")

        needDatabaseChangeLog true
        expectedOutput ""

        expectedResults([
                statusMessage: "Successfully executed registerChangeLog",
                statusCode   : 0
        ])
    }
}
