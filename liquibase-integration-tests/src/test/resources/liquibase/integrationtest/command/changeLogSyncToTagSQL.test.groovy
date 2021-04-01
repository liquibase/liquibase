package liquibase.integrationtest.command


import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.change.core.TagDatabaseChange
import liquibase.integrationtest.setup.SetupDatabaseChangeLog
import liquibase.integrationtest.setup.SetupDatabaseStructure

import static liquibase.integrationtest.command.CommandTest.commandTests
import static liquibase.integrationtest.command.CommandTest.run

commandTests(
        run {
            command "changeLogSyncToTagSQL"

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
                    [
                            new TagDatabaseChange(
                                    tag: "version_2.0"
                            )
                    ] as SetupDatabaseStructure.Entry,
                    [
                            new CreateTableChange(
                                    tableName: "liquibaseRunInfo",
                                    columns: [
                                            ColumnConfig.fromName("timesRan")
                                                    .setType("INT")
                                    ]
                            )
                    ] as SetupDatabaseStructure.Entry
            ]),
                    new SetupDatabaseChangeLog("changelogs/hsqldb/complete/rollback.tag.changelog.xml")

            expectedOutput ""

            arguments([
                    tag: "version_2.0"
            ])
            expectedResults([
                    statusMessage: "Successfully executed changeLogSyncToTagSQL",
                    statusCode   : 0
            ])
        }
)
