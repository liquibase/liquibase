package liquibase.integrationtest.command

CommandTest.define {
    command = ["dbDoc"]

    signature = """
Short Description: MISSING
Long Description: MISSING
Required Args:
  changeLogFile (String) MISSING DESCRIPTION
  outputDirectory (String) MISSING DESCRIPTION
  url (String) MISSING DESCRIPTION
Optional Args:
  NONE
"""

    run {
        arguments = [
                outputDirectory: "target/test-classes",
                changeLogFile  : "changelogs/hsqldb/complete/simple.changelog.xml",
        ]

        expectedResults = [
                statusMessage: "Successfully executed dbDoc",
                statusCode   : 0
        ]
    }
}
