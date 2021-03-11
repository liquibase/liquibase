package liquibase.integrationtest.command

import liquibase.integrationtest.command.LiquibaseCommandTest

[
        new LiquibaseCommandTest.Spec(
                command: ["dropAll"],

                expectedOutput: [
                        "asdf",
                ],

                expectedResults: [:]
        )

] as LiquibaseCommandTest.Spec[]
