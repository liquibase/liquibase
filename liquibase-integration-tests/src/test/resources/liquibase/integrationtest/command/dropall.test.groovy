package liquibase.integrationtest.command

import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.integrationtest.setup.SetupDatabaseStructure

CommandTest.define {
    command = ["dropAll"]

    run {

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
        )

        expectedOutput ""

        expectedResults([
                statusCode: 0
        ])
    }
}
