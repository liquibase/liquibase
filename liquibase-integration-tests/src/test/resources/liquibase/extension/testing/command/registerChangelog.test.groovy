package liquibase.extension.testing.command


CommandTests.define {
    command = ["registerChangelog"]
    signature = """
Short Description: Register the changelog with a Liquibase Hub project
Long Description: NOT SET
Required Args:
  changelogFile (String) The root changelog
Optional Args:
  hubProjectId (UUID) The Hub project ID
    Default: null
  hubProjectName (String) The Hub project name
    Default: null
"""
    run {
        arguments = [
                hubProjectName   : "Project 1",
                changelogFile: "changelogs/hsqldb/complete/changelog-test.xml",
        ]
        setup {
            createTempResource "changelogs/hsqldb/complete/rollback.changelog.xml", "changelogs/hsqldb/complete/changelog-test.xml"
        }
        expectedResults = [
                statusCode   : 0
        ]
    }
}
