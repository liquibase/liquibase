package liquibase.extension.testing.command

CommandTests.define {
    command = ["unexpectedChangesets"]
    signature = """
Short Description: Generate a list of changesets that have been executed but are not in the current changelog
Long Description: NOT SET
Required Args:
  url (String) The JDBC database connection URL
  verbose (String) Verbose flag
Optional Args:
  changelogFile (String) The root changelog
    Default: null
  contexts (String) Changeset contexts to match
    Default: null
  password (String) Password to use to connect to the database
    Default: null
  username (String) Username to use to connect to the database
    Default: null
"""

    run {
        arguments = [
                verbose      : "true",
                changelogFile: "changelogs/hsqldb/complete/unexpected.tag.changelog.xml",
        ]

        setup {
            syncChangelog "changelogs/hsqldb/complete/rollback.tag.changelog.xml"
        }

        expectedResults = [
                statusMessage: "Successfully executed unexpectedChangesets",
                statusCode   : 0
        ]
    }
}
