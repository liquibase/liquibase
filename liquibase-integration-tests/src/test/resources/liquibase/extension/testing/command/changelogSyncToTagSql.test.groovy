package liquibase.extension.testing.command

import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.change.core.TagDatabaseChange
import liquibase.exception.CommandValidationException

CommandTests.define {
    command = ["changelogSyncToTagSql"]
    signature = """
Short Description: Output the raw SQL used by Liquibase when running changelogSyncToTag
Long Description: NOT SET
Required Args:
  changelogFile (String) The root changelog file
  tag (String) Tag ID to execute changelogSync to
  url (String) The JDBC database connection URL
Optional Args:
  contexts (String) Changeset contexts to match
    Default: null
  labels (String) Changeset labels to match
    Default: null
  password (String) The database password
    Default: null
  username (String) The database username
    Default: null
"""

    run "Happy path", {
        arguments = [
                tag            : "version_2.0",
                "changelogFile": "changelogs/hsqldb/complete/rollback.tag.changelog.xml"
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
                statusCode   : 0
        ]
    }

    run "Run without any arguments should throw an exception",  {
        arguments = [
                url: ""
        ]
        expectedException = CommandValidationException.class
    }

    run "Run without a changeLogFile should throw an exception",  {
        arguments = [
                tag          : "version_2.0",
        ]
        expectedException = CommandValidationException.class
    }

    run "Run without a tag should throw an exception",  {
        arguments = [
                changelogFile: "changelogs/hsqldb/complete/rollback.tag.changelog.xml",
        ]
        expectedException = CommandValidationException.class
    }

    run "Run without a URL should throw an exception",  {
        arguments = [
                url          : "",
                changelogFile: "changelogs/hsqldb/complete/rollback.tag.changelog.xml",
                tag          : "version_2.0"
        ]
        expectedException = CommandValidationException.class
    }
}