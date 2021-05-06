package liquibase.extension.testing.command

CommandTests.define {
    command = ["markNextChangesetRan"]
    signature = """
Short Description: Marks the next change you apply as executed in your database
Long Description: NOT SET
Required Args:
  url (String) The JDBC database connection URL
Optional Args:
  changelogFile (String) The root changelog
    Default: null
  password (String) Password to use to connect to the database
    Default: null
  username (String) Username to use to connect to the database
    Default: null
"""

    run {
        arguments = [
                changelogFile: "changelogs/hsqldb/complete/simple.changelog.xml"
        ]

        expectedResults = [
                statusCode   : 0
        ]
    }
}
