package liquibase.integrationtest.command


import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.integrationtest.setup.SetupChangeLogSync
import liquibase.integrationtest.setup.SetupDatabaseChangeLog
import liquibase.integrationtest.setup.SetupDatabaseStructure

import static liquibase.integrationtest.command.CommandTest.commandTests
import static liquibase.integrationtest.command.CommandTest.run

commandTests(
        run {
            command "rollbackToDate"

            setup new SetupDatabaseStructure([
                    [
                            new CreateTableChange(
                                    tableName: "FirstTable",
                                    columns: [
                                            ColumnConfig.fromName("FirstColumn")
                                                    .setType("VARCHAR(255)")
                                    ]
                            )
                    ] as SetupDatabaseStructure.Entry,
                    [
                            new CreateTableChange(
                                    tableName: "SecondTable",
                                    columns: [
                                            ColumnConfig.fromName("SecondColumn")
                                                    .setType("VARCHAR(255)")
                                    ]
                            )
                    ] as SetupDatabaseStructure.Entry,
            ]),
                    new SetupDatabaseChangeLog("changelogs/hsqldb/complete/rollback.changelog.xml"),
                    new SetupChangeLogSync("changelogs/hsqldb/complete/rollback.changelog.xml")

            arguments([
                    date: "2021-03-25T09:00:00"
            ])

            expectedOutput ""

            expectedResults([
                    statusMessage: "Successfully executed rollbackToDate",
                    statusCode   : 0
            ])
        }
)
