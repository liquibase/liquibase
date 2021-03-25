import liquibase.integrationtest.command.CommandTest

[
        new CommandTest.Spec(
                command: ["invalid"],

                expectedOutput: [
                        "asdf",
                ],

                expectedResults: [:],
                expectedException: liquibase.exception.CommandExecutionException
        )

] as CommandTest.Spec[]
