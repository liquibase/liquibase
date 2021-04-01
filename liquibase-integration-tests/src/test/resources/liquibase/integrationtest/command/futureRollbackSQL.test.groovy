package liquibase.integrationtest.command

import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.integrationtest.setup.SetupChangeLogSync
import liquibase.integrationtest.setup.SetupDatabaseChangeLog
import liquibase.integrationtest.setup.SetupDatabaseStructure

CommandTest.define {
    command = ["futureRollbackSQL"]

    run {
        arguments = [
                count: 1
        ]

        setup SetupDatabaseStructure.create(
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
        ),
                new SetupDatabaseChangeLog("changelogs/hsqldb/complete/rollback.changelog.xml"),
                new SetupChangeLogSync("changelogs/hsqldb/complete/rollback.changelog.xml")

        expectedOutput ""
        expectedResults([
                statusMessage: "Successfully executed futureRollbackSQL",
                statusCode   : 0
        ])
    }
}
