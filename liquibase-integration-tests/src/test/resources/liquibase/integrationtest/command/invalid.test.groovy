package liquibase.integrationtest.command

import liquibase.integrationtest.command.CommandTest
[
        new CommandTest.Spec(
                command: ["invalid"],

                expectedOutput: [
                        "",
                ],

                expectedResults: [:],
                expectedException: liquibase.exception.CommandExecutionException
        )

] as CommandTest.Spec[]
