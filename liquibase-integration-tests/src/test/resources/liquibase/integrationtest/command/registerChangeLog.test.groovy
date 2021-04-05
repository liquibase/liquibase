package liquibase.integrationtest.command


CommandTest.define {
    command = ["registerChangeLog"]
    signature = """
Short Description: MISSING
Long Description: MISSING
Required Args:
  changeLog (DatabaseChangeLog) MISSING DESCRIPTION
  changeLogFile (String) MISSING DESCRIPTION
Optional Args:
  hubChangeLog (HubChangeLog) MISSING DESCRIPTION
    Default: null
  hubProjectId (UUID) MISSING DESCRIPTION
    Default: null
  hubProjectName (String) MISSING DESCRIPTION
    Default: null
"""

    run {
        arguments = [
                hubProjectName: "Project 1",
                changeLog     : "changelogs/hsqldb/complete/simple.changelog.xml"
        ]

        expectedResults = [
                statusMessage: "Successfully executed registerChangeLog",
                statusCode   : 0
        ]
    }
}
