package liquibase.extension.testing.command

CommandTests.define {
    command = ["releaseLocks"]
    signature = """
Short Description: Remove the Liquibase lock record from the DATABASECHANGELOG table
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
        expectedResults = [
                statusMessage: "Successfully executed releaseLocks",
                statusCode   : 0
        ]
    }
}
