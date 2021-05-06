package liquibase.extension.testing.command

CommandTests.define {
    command = ["rollbackCountSql"]
    signature = """
Short Description: Generate the SQL to rollback the specified number of changes
Long Description: NOT SET
Required Args:
  count (Integer) The number of changes to rollback
  url (String) The JDBC database connection URL
Optional Args:
  changelogFile (String) The root changelog
    Default: null
  contexts (String) Changeset contexts to match
    Default: null
  labels (String) Changeset labels to match
    Default: null
  password (String) Password to use to connect to the database
    Default: null
  rollbackScript (String) Rollback script to execute
    Default: null
  username (String) Username to use to connect to the database
    Default: null
"""

    run {
        arguments = [
                count        : 1,
                changelogFile: "changelogs/hsqldb/complete/rollback.changelog.xml",
        ]


        setup {
            runChangelog "changelogs/hsqldb/complete/rollback.changelog.xml"
        }

        expectedResults = [
                statusCode   : 0
        ]
    }
}
