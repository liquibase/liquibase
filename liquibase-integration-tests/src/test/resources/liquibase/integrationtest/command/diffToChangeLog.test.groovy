package liquibase.integrationtest.command

import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.change.core.TagDatabaseChange
import liquibase.integrationtest.setup.SetupCustomDiffArgs

CommandTest.define {
    command = ["diffChangeLog"]
    run {
        arguments = [
                tag: "version_2.0"
        ]

        setup {
            database = [
                            new CreateTableChange(
                                    tableName: "FirstTable",
                                    columns: [
                                            ColumnConfig.fromName("FirstColumn")
                                                    .setType("VARCHAR(255)")
                                    ]
                            ),
                            new CreateTableChange(
                                    tableName: "SecondTable",
                                    columns: [
                                            ColumnConfig.fromName("SecondColumn")
                                                    .setType("VARCHAR(255)")
                                    ]
                            ),
                            new TagDatabaseChange(
                                    tag: "version_2.0"
                            ),
                            new CreateTableChange(
                                    tableName: "liquibaseRunInfo",
                                    columns: [
                                            ColumnConfig.fromName("timesRan")
                                                    .setType("INT")
                                    ]
                            ),
            ]
        }

        customSetup(
                new SetupCustomDiffArgs()
        )
        expectedOutput ""
        expectedResults([
                statusMessage: "Successfully executed diffChangeLog",
                statusCode   : 0
        ])
    }
}
