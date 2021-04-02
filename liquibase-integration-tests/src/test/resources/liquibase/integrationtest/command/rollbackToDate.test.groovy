package liquibase.integrationtest.command

import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.integrationtest.setup.SetupChangeLogSync

CommandTest.define {
    command = ["rollbackToDate"]

    run {
        arguments = [
                date         : "2021-03-25T09:00:00",
                changeLogFile: "changelogs/hsqldb/complete/rollback.changelog.xml",
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
            ]

            add new SetupChangeLogSync("changelogs/hsqldb/complete/rollback.changelog.xml")
        }

        expectedOutput ""

        expectedResults([
                statusMessage: "Successfully executed rollbackToDate",
                statusCode   : 0
        ])
    }
}
