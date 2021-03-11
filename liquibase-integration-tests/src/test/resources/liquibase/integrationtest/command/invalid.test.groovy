package liquibase.integrationtest.command

import liquibase.integrationtest.command.LiquibaseCommandTest

[
        new LiquibaseCommandTest.Spec(
                command: ["invalid"],

                expectedOutput: [
                        "asdf",
                ],

                expectedResults: [:]
        )

] as LiquibaseCommandTest.Spec[]
