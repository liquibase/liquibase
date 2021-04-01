package liquibase.integrationtest.command

CommandTest.define {
    run {
        command = ["listLocks"]

        expectedOutput ""

        expectedResults([
                statusMessage: "Successfully executed listLocks",
                statusCode   : 0
        ])
    }
}
