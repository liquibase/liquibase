package liquibase.integrationtest.command

CommandTest.define {
    command = ["updateCount"]
    signature = """
Short Description: MISSING
Long Description: MISSING
Required Args:
  changeLogFile (String) MISSING DESCRIPTION
  url (String) MISSING DESCRIPTION
Optional Args:
  contexts (String) MISSING DESCRIPTION
    Default: null
  count (Integer) MISSING DESCRIPTION
    Default: null
  labels (String) MISSING DESCRIPTION
    Default: null
"""

    run {
        arguments = [
                count        : 1,
                changeLogFile: "changelogs/hsqldb/complete/simple.changelog.xml",
        ]

        expectedResults = [
                statusMessage: "Successfully executed updateCount",
                statusCode   : 0
        ]
    }
}
