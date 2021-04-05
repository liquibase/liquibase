package liquibase.integrationtest.command

CommandTest.define {
    command = ["validate"]
    signature = """
Short Description: MISSING
Long Description: MISSING
Required Args:
  changeLogFile (String) MISSING DESCRIPTION
  url (String) MISSING DESCRIPTION
Optional Args:
  NONE
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
