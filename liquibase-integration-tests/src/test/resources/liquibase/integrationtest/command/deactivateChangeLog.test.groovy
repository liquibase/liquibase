package liquibase.integrationtest.command

CommandTest.define {
    command = ["deactivateChangeLog"]
    signature = """
Short Description: MISSING
Long Description: MISSING
Required Args:
  changeLog (DatabaseChangeLog) MISSING DESCRIPTION
  changeLogFile (String) MISSING DESCRIPTION
Optional Args:
  NONE
"""

    run {

        arguments = [
                changeLogFile: "changelogs/hsqldb/complete/simple.changelog.with.id.xml",
        ]

        expectedResults = [
                statusMessage: "Successfully executed deactivateChangeLog",
                statusCode   : 0
        ]
    }
}
