package liquibase.integrationtest.command

CommandTest.define {
    command = ["tagExists"]
    signature = """
Short Description: MISSING
Long Description: MISSING
Required Args:
  url (String) MISSING DESCRIPTION
Optional Args:
  tag (String) MISSING DESCRIPTION
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
