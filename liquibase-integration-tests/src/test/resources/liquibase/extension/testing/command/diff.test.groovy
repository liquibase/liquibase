package liquibase.extension.testing.command

import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.change.core.TagDatabaseChange

import java.util.function.Function

CommandTests.define {
    command = ["diff"]
    signature = """
Short Description: Compare two databases
Long Description: Compare two databases
Required Args:
  referenceUrl (String) The JDBC reference database connection URL
  url (String) The JDBC target database connection URL
Optional Args:
  format (String) Option to create JSON output
    Default: null
  password (String) The target database password
    Default: null
  referencePassword (String) The reference database password
    Default: null
  referenceUsername (String) The reference database username
    Default: null
  username (String) The target database username
    Default: null
"""

    run {
        arguments = [
                referenceUrl: { it.url },
                url         : { it.url }
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
