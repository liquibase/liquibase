package liquibase.integrationtest.command

CommandTest.define {
    command = ["releaseLocks"]
    signature = """
Short Description: MISSING
Long Description: MISSING
Required Args:
  url (String) MISSING DESCRIPTION
Optional Args:
  NONE
"""

    run {
        expectedResults = [
                statusMessage: "Successfully executed releaseLocks",
                statusCode   : 0
        ]
    }
}
