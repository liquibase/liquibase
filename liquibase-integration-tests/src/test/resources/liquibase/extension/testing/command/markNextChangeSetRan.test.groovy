package liquibase.extension.testing.command

CommandTests.define {
    command = ["markNextChangeSetRan"]
    signature = """
Short Description: Marks the next change you apply as executed in your database
Long Description: Marks the next change you apply as executed in your database
Required Args:
  url (String) The JDBC database connection URL
Optional Args:
  changeLogFile (String) The root changelog
    Default: null
  password (String) Password to use to connect to the database
    Default: null
  username (String) Username to use to connect to the database
    Default: null
"""

    run {
        arguments = [
                changeLogFile: "changelogs/hsqldb/complete/simple.changelog.xml"
        ]

        expectedResults = [
                statusMessage: "Successfully executed markNextChangeSetRan",
                statusCode   : 0
        ]
    }
}
