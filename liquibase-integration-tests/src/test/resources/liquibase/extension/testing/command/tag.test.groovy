package liquibase.extension.testing.command

CommandTests.define {
    command = ["tag"]
    signature = """
Short Description: Mark the current database state with the specified tag
Long Description: Mark the current database state with the specified tag
Required Args:
  tag (String) Tag to add to the database changelog table
  url (String) The JDBC database connection URL
Optional Args:
  password (String) Password to use to connect to the database
    Default: null
  username (String) Username to use to connect to the database
    Default: null
"""

    run {
        arguments = [
                tag: "version_2.0"
        ]

        expectedResults = [
                statusMessage: "Successfully executed tag",
                statusCode   : 0
        ]
    }
}
