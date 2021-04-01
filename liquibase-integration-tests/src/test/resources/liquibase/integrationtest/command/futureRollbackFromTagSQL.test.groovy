package liquibase.integrationtest.command

import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.integrationtest.setup.SetupChangeLogSync
import liquibase.integrationtest.setup.SetupDatabaseChangeLog
import liquibase.integrationtest.setup.SetupDatabaseStructure

CommandTest.define {
    command = ["futureRollbackFromTagSQL"]

    run {
        arguments = [
                tag: "version_2.0"
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
                new SetupDatabaseChangeLog("changelogs/hsqldb/complete/rollback.tag.changelog.xml"),
                new SetupChangeLogSync("changelogs/hsqldb/complete/rollback.tag.changelog.xml")

        expectedOutput ""

        expectedResults([
                statusMessage: "Successfully executed futureRollbackFromTagSQL",
                statusCode   : 0
        ])
    }
}
