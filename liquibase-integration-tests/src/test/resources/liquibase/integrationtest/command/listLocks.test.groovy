package liquibase.integrationtest.command

CommandTest.define {
    command = ["listLocks"]
    signature = """
Short Description: List the hostname, IP address, and timestamp of the Liquibase lock record
Long Description: List the hostname, IP address, and timestamp of the Liquibase lock record
Required Args:
  url (String) Database URL to generate a changelog for
Optional Args:
  changeLogFile (String) File to write changelog to
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
