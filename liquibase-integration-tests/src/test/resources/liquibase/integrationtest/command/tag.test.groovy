package liquibase.integrationtest.command

import liquibase.integrationtest.setup.SetupDatabaseChangeLog

CommandTest.define {
    command = ["tag"]

    run {
        arguments = [
                tag: "version_2.0"
        ]

        setup new SetupDatabaseChangeLog("changelogs/hsqldb/complete/simple.tag.changelog.xml")

        expectedOutput ""

        expectedResults([
                statusMessage: "Successfully executed tag",
                statusCode   : 0
        ])
    }
}
