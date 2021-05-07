package liquibase.extension.testing.command

import liquibase.exception.CommandValidationException

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

    run "Happy path", {
        expectedResults = [
                statusCode   : 0
        ]
    }

    run "Run without a URL throws an exception", {
        arguments = [
                url: ""
        ]
        expectedException = CommandValidationException.class
    }
}
