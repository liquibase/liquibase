package liquibase.integrationtest.command

CommandTest.define {
    command = ["migrateSQL"]

    run {
        arguments = [
                changeLogFile: "changelogs/hsqldb/complete/simple.changelog.xml",
        ]

        expectedResults = [
                statusMessage: "Successfully executed migrateSQL",
                statusCode   : 0
        ]
    }
}
