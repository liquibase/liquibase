package liquibase.integrationtest.command

CommandTest.define {
    command = ["listLocks"]

    run {
        expectedOutput ""

        expectedResults([
                statusMessage: "Successfully executed listLocks",
                statusCode   : 0
        ])
    }
}
