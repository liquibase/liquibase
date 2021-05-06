package liquibase.extension.testing.command

CommandTests.define {
    command = ["status"]
    signature = """
Short Description: Generate a list of pending changesets
Long Description: NOT SET
Required Args:
  changelogFile (String) The root changelog
  url (String) The JDBC database connection URL
Optional Args:
  contexts (String) Changeset contexts to match
    Default: null
  labels (String) Changeset labels to match
    Default: null
  password (String) Password to use to connect to the database
    Default: null
  username (String) Username to use to connect to the database
    Default: null
  verbose (String) Verbose flag
    Default: null
"""

    run {
        arguments = [
                verbose      : "true",
                changelogFile: "changelogs/hsqldb/complete/rollback.tag.plus.changelog.xml"
        ]

        setup {
            runChangelog "changelogs/hsqldb/complete/rollback.tag.plus.changelog.xml", "init"
        }

        expectedResults = [
                statusCode   : 0
        ]
    }
}
