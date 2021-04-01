package liquibase.integrationtest.command

import liquibase.integrationtest.setup.SetupDatabaseChangeLog

CommandTest.define {
    run {
        command = ["markNextChangeSetRanSQL"]

        setup new SetupDatabaseChangeLog("changelogs/hsqldb/complete/simple.changelog.xml")

        expectedOutput ""

        expectedResults([
                statusMessage: "Successfully executed markNextChangeSetRanSQL",
                statusCode   : 0
        ])
    }
}
