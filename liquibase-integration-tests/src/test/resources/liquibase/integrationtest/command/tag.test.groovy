package liquibase.integrationtest.command

CommandTest.define {
    command = ["tag"]
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
                statusMessage: "Successfully executed tag",
                statusCode   : 0
        ]
    }
}
