package liquibase.integrationtest.command

CommandTest.define {
    command = ["releaseLocks"]

    run {
        expectedOutput ""

        expectedResults([
                statusMessage: "Successfully executed releaseLocks",
                statusCode   : 0
        ])
    }
}
