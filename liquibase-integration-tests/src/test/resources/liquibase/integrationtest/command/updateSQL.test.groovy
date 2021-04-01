package liquibase.integrationtest.command

import liquibase.integrationtest.setup.SetupDatabaseChangeLog

CommandTest.define {
    command = ["updateSQL"]

    run {
        setup new SetupDatabaseChangeLog("changelogs/hsqldb/complete/simple.changelog.xml")

        expectedOutput ""

        expectedResults([
                statusMessage: "Successfully executed updateSQL",
                statusCode   : 0
        ])
    }
}
