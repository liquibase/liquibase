package liquibase.integrationtest.command

import liquibase.exception.CommandExecutionException
import liquibase.integrationtest.command.LiquibaseCommandTest

[
        new LiquibaseCommandTest.Spec(
                command: ["invalid"],

                expectedOutput: [
                        "asdf",
                ],

                expectedResults: [:],
                expectedException: liquibase.exception.CommandExecutionException
        )

] as LiquibaseCommandTest.Spec[]
