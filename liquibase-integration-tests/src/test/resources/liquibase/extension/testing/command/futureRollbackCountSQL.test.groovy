package liquibase.extension.testing.command

CommandTests.define {
    command = ["futureRollbackCountSQL"]
    signature = """
Short Description: Generates SQL to sequentially revert <count> number of changes
Long Description: Generates SQL to sequentially revert <count> number of changes
Required Args:
  changeLogFile (String) The root changelog
  count (Integer) Number of change sets to generate rollback SQL for
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
    run {
        arguments = [
                count        : 1,
                changeLogFile: "changelogs/hsqldb/complete/rollback.changelog.xml",
        ]

        setup {
            runChangelog "changelogs/hsqldb/complete/rollback.changelog.xml"
            rollback 5, "changelogs/hsqldb/complete/rollback.changelog.xml"
        }

        expectedResults = [
                statusMessage: "Successfully executed futureRollbackCountSQL",
                statusCode   : 0
        ]
    }
}
