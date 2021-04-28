package liquibase.extension.testing.command

import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.change.core.TagDatabaseChange

CommandTests.define {
    command = ["changeLogSyncSQL"]
    signature = """
Short Description: Output the raw SQL used by Liquibase when running changeLogSync
Long Description: Output the raw SQL used by Liquibase when running changeLogSync
Required Args:
  changeLogFile (String) The root changelog file
  url (String) The JDBC database connection URL
Optional Args:
  contexts (String) Context string to use for filtering which changes to mark as executed
    Default: null
  labels (String) Label expression to use for filtering which changes to mark as executed
    Default: null
  password (String) The database password
    Default: null
  username (String) The database username
    Default: null
"""

    run {
        arguments = [
                changeLogFile: "changelogs/hsqldb/complete/rollback.tag.changelog.xml"
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
                statusMessage: "Successfully executed changeLogSyncSQL",
                statusCode   : 0
        ]
    }
}
