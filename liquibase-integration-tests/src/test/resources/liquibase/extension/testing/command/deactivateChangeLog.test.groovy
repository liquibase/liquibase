package liquibase.extension.testing.command

CommandTests.define {
    command = ["deactivateChangeLog"]
    signature = """
Short Description: Removes the changelogID from your changelog so it stops sending reports to Liquibase Hub
Long Description: Removes the changelogID from your changelog so it stops sending reports to Liquibase Hub
Required Args:
  changeLogFile (String) The root changelog
  liquibaseHubApiKey (String) The Liquibase Hub API key
Optional Args:
  NONE
"""

    run {

        arguments = [
                changeLogFile: "changelogs/hsqldb/complete/simple.changelog.with.id-test.xml",
                liquibaseHubApiKey: "${UUID.randomUUID().toString()}"
        ]
        setup {
            createTempResource "changelogs/hsqldb/complete/simple.changelog.with.id.xml", "changelogs/hsqldb/complete/simple.changelog.with.id-test.xml"
        }
        expectedResults = [
                statusMessage: "Successfully executed deactivateChangeLog",
                statusCode   : 0
        ]
    }
}
