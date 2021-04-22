package liquibase.extension.testing.command

CommandTests.define {
    command = ["syncHub"]
    signature = """
Short Description: Synchronize the local DatabaseChangeLog table with Liquibase Hub
Long Description: Synchronize the local DatabaseChangeLog table with Liquibase Hub
Required Args:
  liquibaseHubApiKey (String) Liquibase Hub API key for connecting to Liquibase Hub
  url (String) The JDBC database connection URL
Optional Args:
  changeLogFile (String) The root changelog
    Default: null
  hubConnectionId (String) Liquibase Hub Connection ID to sync
    Default: null
  hubProjectId (String) Liquibase Hub Project ID to sync
    Default: null
  password (String) Password to use to connect to the database
    Default: null
  username (String) Username to use to connect to the database
    Default: null
"""
    run {
        arguments = [
                changeLogFile: "changelogs/hsqldb/complete/rollback.tag.changelog.xml",
                liquibaseHubApiKey: "${UUID.randomUUID().toString()}"
        ]

        setup {
            runChangelog "changelogs/hsqldb/complete/rollback.tag.changelog.xml"
        }

        expectedResults = [
                statusMessage: "Successfully executed syncHub",
                statusCode   : 0
        ]
    }
}
