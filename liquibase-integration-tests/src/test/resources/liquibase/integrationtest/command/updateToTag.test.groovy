package liquibase.integrationtest.command

import liquibase.integrationtest.setup.SetupDatabaseChangeLog

CommandTest.define {
    command = ["updateToTag"]

    run {
        arguments = [
                tag: "version_2.0"
        ]

        setup new SetupDatabaseChangeLog("changelogs/hsqldb/complete/simple.tag.changelog.xml")

        expectedOutput ""

        expectedResults([
                statusMessage: "Successfully executed updateToTag",
                statusCode   : 0
        ])
    }
}
