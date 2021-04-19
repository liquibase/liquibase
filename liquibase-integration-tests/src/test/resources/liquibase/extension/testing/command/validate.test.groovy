package liquibase.extension.testing.command

CommandTests.define {
    command = ["validate"]
    signature = """
Short Description: Validate the changelog for errors
Long Description: Validate the changelog for errors
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
                statusMessage: "Successfully executed validate",
                statusCode   : 0
        ]
    }
}
