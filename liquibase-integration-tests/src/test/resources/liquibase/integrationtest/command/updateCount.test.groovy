package liquibase.integrationtest.command

import liquibase.integrationtest.setup.SetupDatabaseChangeLog

CommandTest.define {
    run {
        command = ["updateCount"]

        arguments = [
                count: 1
        ]


        setup new SetupDatabaseChangeLog("changelogs/hsqldb/complete/simple.changelog.xml")

        expectedOutput ""

        expectedResults([
                statusMessage: "Successfully executed updateCount",
                statusCode   : 0
        ])
    }
}
