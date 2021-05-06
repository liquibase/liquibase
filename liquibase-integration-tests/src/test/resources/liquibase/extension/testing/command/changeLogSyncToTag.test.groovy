package liquibase.extension.testing.command

import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.change.core.TagDatabaseChange

CommandTests.define {
    command = ["changeLogSyncToTag"]
    signature = """
Short Description: Marks all undeployed changesets as executed, up to a tag
Long Description: Marks all undeployed changesets as executed, up to a tag
Required Args:
  changeLogFile (String) The root changelog file
  url (String) The JDBC database connection URL
Optional Args:
  contexts (String) Changeset contexts to match
    Default: null
  labels (String) Changeset labels to match
    Default: null
  password (String) The database password
    Default: null
  tag (String) Tag ID to execute changeLogSync to
    Default: null
  username (String) The database username
    Default: null
"""

    run {
        arguments = [
                tag          : "version_2.0",
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
                statusMessage: "Successfully executed changeLogSyncToTag",
                statusCode   : 0
        ]
    }
}
