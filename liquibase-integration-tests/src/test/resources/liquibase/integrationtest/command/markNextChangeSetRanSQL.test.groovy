package liquibase.integrationtest.command

CommandTest.define {
    command = ["markNextChangeSetRanSQL"]
    signature = """
Short Description: Writes the SQL used to mark the next change you apply as executed in your database
Long Description: Marks the SQL used to mark the next change you apply as executed in your database
Required Args:
  url (String) Database URL to generate a changelog for
Optional Args:
  changeLogFile (String) File to write changelog to
    Default: null
  outputFile (String) File for writing the SQL
    Default: null
  password (String) Password to use to connect to the database
    Default: null
  username (String) Username to use to connect to the database
    Default: null
"""

    run {

        arguments = [
                "changeLogFile": "changelogs/hsqldb/complete/simple.changelog.xml"
        ]

        expectedResults = [
                statusMessage: "Successfully executed markNextChangeSetRanSQL",
                statusCode   : 0
        ]
    }
}
