package liquibase.extension.testing.command

CommandTests.define {
    command = ["tagExists"]
    signature = """
Short Description: MISSING
Long Description: MISSING
Required Args:
  tag (String) Tag to check
  url (String) The JDBC database connection URL
Optional Args:
  password (String) Password to use to connect to the database
    Default: null
  username (String) Username to use to connect to the database
    Default: null
"""

    run {
        arguments = [
                tag: "version_2.0",
        ]

        expectedResults = [
                statusMessage: "Successfully executed tagExists",
                statusCode   : 0
        ]
    }
}
