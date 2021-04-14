package liquibase.integrationtest.command

import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.change.core.TagDatabaseChange

CommandTest.define {
    command = ["diff"]
    signature = """
Short Description: Compare two databases
Long Description: Compare two databases
Required Args:
  referenceUrl (String) The JDBC reference database connection URL
  url (String) The JDBC target database connection URL
Optional Args:
  outputFile (String) File for writing the diff report
    Default: null
  username (String) The reference database username
    Default: null
"""
    run {
        arguments = [
            referenceUrl: "offline:postgresql?snapshot=snapshot1.json",
            url: "offline:postgresql?snapshot=snapshot1.json"
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

        expectedResults = [
                statusMessage: "Successfully executed diff",
                statusCode   : 0
        ]
    }
}
