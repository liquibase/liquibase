package liquibase.integrationtest.command

CommandTest.define {
    command = ["releaseLocks"]

    run {
        expectedResults = [
                statusMessage: "Successfully executed releaseLocks",
                statusCode   : 0
        ]
    }
}
