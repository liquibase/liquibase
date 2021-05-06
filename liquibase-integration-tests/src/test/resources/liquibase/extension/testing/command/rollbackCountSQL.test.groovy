package liquibase.extension.testing.command

CommandTests.define {
    command = ["rollbackCountSQL"]
    signature = """
Short Description: Generate the SQL to rollback the specified number of changes
Long Description: Generate the SQL to rollback the specified number of changes
Required Args:
  count (Integer) The number of changes to rollback
  url (String) The JDBC database connection URL
Optional Args:
  changeLogFile (String) The root changelog
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
                changeLogFile: "changelogs/hsqldb/complete/rollback.changelog.xml",
        ]


        setup {
            runChangelog "changelogs/hsqldb/complete/rollback.changelog.xml"
        }

        expectedResults = [
                statusMessage: "Successfully executed rollbackCountSQL",
                statusCode   : 0
        ]
    }
}
