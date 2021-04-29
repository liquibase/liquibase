package liquibase.extension.testing.command

CommandTests.define {
    command = ["syncHub"]
    signature = """
Short Description: Synchronize the local DatabaseChangeLog table with Liquibase Hub
Long Description: NOT SET
Required Args:
  url (String) The JDBC database connection URL
Optional Args:
  changelogFile (String) The root changelog
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
                changelogFile: "changelogs/hsqldb/complete/rollback.tag.changelog.xml",
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
