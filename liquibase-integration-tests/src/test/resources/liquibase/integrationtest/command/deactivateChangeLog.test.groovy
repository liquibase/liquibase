package liquibase.integrationtest.command

import liquibase.integrationtest.setup.SetupDatabaseChangeLog

CommandTest.define {
    command = ["deactivateChangeLog"]
    run {

        setup(new SetupDatabaseChangeLog("changelogs/hsqldb/complete/simple.changelog.with.id.xml"))

        needDatabaseChangeLog true

        expectedOutput ""

        expectedResults([
                statusMessage: "Successfully executed deactivateChangeLog",
                statusCode   : 0
        ])
    }
}
