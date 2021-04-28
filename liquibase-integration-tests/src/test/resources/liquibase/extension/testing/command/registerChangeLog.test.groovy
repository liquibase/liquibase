package liquibase.extension.testing.command


CommandTests.define {
    command = ["registerChangeLog"]
    signature = """
Short Description: Register the changelog with a Liquibase Hub project
Long Description: Register the changelog with a Liquibase Hub project
Required Args:
  changeLogFile (String) The root changelog
Optional Args:
  hubProjectId (UUID) The Hub project ID
    Default: null
  hubProjectName (String) The Hub project name
    Default: null
"""
    run {
        arguments = [
                hubProjectName   : "Project 1",
                changeLogFile: "changelogs/hsqldb/complete/changelog-test.xml",
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
