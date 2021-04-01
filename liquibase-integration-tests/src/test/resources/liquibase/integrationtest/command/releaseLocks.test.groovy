package liquibase.integrationtest.command

CommandTest.define {
    run {
        command = ["releaseLocks"]

        expectedOutput ""

        expectedResults([
                statusMessage: "Successfully executed releaseLocks",
                statusCode   : 0
        ])
    }
}
