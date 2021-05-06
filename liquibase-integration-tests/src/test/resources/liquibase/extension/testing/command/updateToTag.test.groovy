package liquibase.extension.testing.command

CommandTests.define {
    command = ["updateToTag"]
    signature = """
Short Description: Deploy changes from the changelog file to the specified tag
Long Description: NOT SET
Required Args:
  tag (String) The tag to update to
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
  username (String) Username to use to connect to the database
    Default: null
"""

    run {
        arguments = [
                tag          : "version_2.0",
                changelogFile: "changelogs/hsqldb/complete/simple.tag.changelog.xml",
        ]


        expectedResults = [
                statusCode   : 0
        ]
    }
}
