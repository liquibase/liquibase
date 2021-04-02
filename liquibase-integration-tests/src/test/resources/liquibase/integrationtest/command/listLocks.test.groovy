package liquibase.integrationtest.command

CommandTest.define {
    command = ["listLocks"]

    run {
        expectedResults = [
                statusMessage: "Successfully executed listLocks",
                statusCode   : 0
        ]
    }
}
