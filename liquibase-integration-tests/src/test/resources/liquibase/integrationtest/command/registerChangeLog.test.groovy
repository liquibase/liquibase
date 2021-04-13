package liquibase.integrationtest.command


CommandTest.define {
    command = ["registerChangeLog"]
    signature = """
Short Description: MISSING
Long Description: MISSING
Required Args:
  changeLogFile (String) MISSING DESCRIPTION
Optional Args:
  hubProjectId (UUID) MISSING DESCRIPTION
    Default: null
  hubProjectName (String) MISSING DESCRIPTION
    Default: null
"""
    run {
        arguments = [
                hubProjectName   : "Project 1",
                changeLogFile: "changelogs/hsqldb/complete/changelog-test.xml"
        ]
        setup {
            createTempResource "changelogs/hsqldb/complete/rollback.changelog.xml", "changelogs/hsqldb/complete/changelog-test.xml"
        }
        expectedResults = [
                statusMessage: "Successfully executed registerChangeLog",
                statusCode   : 0
        ]
    }
}
