package liquibase.extension.testing.command

CommandTests.define {
    command = ["futureRollbackSQL"]
    signature = """
Short Description: Generate the raw SQL needed to rollback undeployed changes
Long Description: Generate the raw SQL needed to rollback undeployed changes
Required Args:
  url (String) The JDBC Database connection URL
Optional Args:
  changeLogFile (String) The root changelog
    Default: null
  contexts (String) Changeset contexts to match
    Default: null
  labels (String) Changeset labels to match
    Default: null
  password (String) Password to use to connect to the database
    Default: null
  username (String) Username to use to connect to the database
    Default: null
"""

    run {
        arguments = [
                changeLogFile: "changelogs/hsqldb/complete/rollback.changelog.xml",
        ]

        setup {
            runChangelog "changelogs/hsqldb/complete/rollback.changelog.xml"
            rollback 5, "changelogs/hsqldb/complete/rollback.changelog.xml"

        }

        expectedResults = [
                statusMessage: "Successfully executed futureRollbackSQL",
                statusCode   : 0
        ]
    }
}
