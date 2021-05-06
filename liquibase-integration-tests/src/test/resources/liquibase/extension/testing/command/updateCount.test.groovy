package liquibase.extension.testing.command

CommandTests.define {
    command = ["updateCount"]
    signature = """
Short Description: Deploy the specified number of changes from the changelog file
Long Description: Deploy the specified number of changes from the changelog file
Required Args:
  count (Integer) The number of changes in the changelog to deploy
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
                count        : 1,
                changeLogFile: "changelogs/hsqldb/complete/simple.changelog.xml",
        ]

        expectedResults = [
                statusMessage: "Successfully executed updateCount",
                statusCode   : 0
        ]
    }
}
