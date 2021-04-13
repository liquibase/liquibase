package liquibase.integrationtest.command

import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.change.core.TagDatabaseChange

CommandTest.define {
    command = ["diff"]
    signature = """
Short Description: MISSING
Long Description: MISSING
Required Args:
  referenceUrl (String) MISSING DESCRIPTION
  url (String) MISSING DESCRIPTION
Optional Args:
  password (String) MISSING DESCRIPTION
    Default: null
  referencePassword (String) MISSING DESCRIPTION
    Default: null
  referenceUsername (String) MISSING DESCRIPTION
    Default: null
  username (String) MISSING DESCRIPTION
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
