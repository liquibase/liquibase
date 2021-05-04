package liquibase.extension.testing.command

CommandTests.define {
    command = ["status"]
    signature = """
Short Description: Generate a list of pending changesets
Long Description: Generate a list of pending changesets
Required Args:
  changeLogFile (String) The root changelog
  url (String) The JDBC database connection URL
  verbose (String) Verbose flag
Optional Args:
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
                verbose      : "true",
                changeLogFile: "changelogs/hsqldb/complete/rollback.tag.plus.changelog.xml"
        ]

        setup {
            runChangelog "changelogs/hsqldb/complete/rollback.tag.plus.changelog.xml", "init"
        }

        expectedResults = [
                statusMessage: "Successfully executed status",
                statusCode   : 0
        ]
    }
}
