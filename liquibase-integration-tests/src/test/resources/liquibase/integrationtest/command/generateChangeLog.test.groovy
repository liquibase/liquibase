package liquibase.integrationtest.command

import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.change.core.TagDatabaseChange
import liquibase.integrationtest.setup.SetupCustomDiffArgs
import liquibase.integrationtest.setup.SetupDatabaseChangeLogFile
import liquibase.integrationtest.setup.SetupDatabaseStructure

CommandTest.define {
    command = ["generateChangeLog"]

    run {
        arguments = [
                tag: "version_2.0"
        ]

        setup(
                new SetupDatabaseStructure([
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
                new SetupDatabaseChangeLogFile(null))
        customSetup(
                new SetupCustomDiffArgs()
        )

        expectedOutput ""
        expectedResults([
                statusMessage: "Successfully executed generateChangeLog",
                statusCode   : 0
        ])
    }
}
