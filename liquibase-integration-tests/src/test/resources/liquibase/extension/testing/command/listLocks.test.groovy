package liquibase.extension.testing.command

CommandTests.define {
    command = ["listLocks"]
    signature = """
Short Description: List the hostname, IP address, and timestamp of the Liquibase lock record
Long Description: List the hostname, IP address, and timestamp of the Liquibase lock record
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
        expectedResults = [
                statusMessage: "Successfully executed listLocks",
                statusCode   : 0
        ]
    }
}
