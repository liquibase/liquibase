package liquibase.extension.testing.command

CommandTests.define {
    command = ["updateToTag"]
    signature = """
Short Description: Deploy changes from the changelog file to the specified tag
Long Description: Deploy changes from the changelog file to the specified tag
Required Args:
  tag (String) The tag to update to
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
  username (String) Username to use to connect to the database
    Default: null
"""

    run {
        arguments = [
                tag          : "version_2.0",
                changeLogFile: "changelogs/hsqldb/complete/simple.tag.changelog.xml",
        ]


        expectedResults = [
                statusMessage: "Successfully executed updateToTag",
                statusCode   : 0
        ]
    }
}
