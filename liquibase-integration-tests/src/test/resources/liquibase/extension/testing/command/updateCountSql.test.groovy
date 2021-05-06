package liquibase.extension.testing.command

CommandTests.define {
    command = ["updateCountSql"]
    signature = """
Short Description: Generate the SQL to deploy the specified number of changes
Long Description: NOT SET
Required Args:
  count (Integer) The number of changes to generate SQL for
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
                count        : 1,
                changelogFile: "changelogs/hsqldb/complete/simple.changelog.xml",
        ]

        expectedResults = [
                statusCode   : 0
        ]
    }
}
