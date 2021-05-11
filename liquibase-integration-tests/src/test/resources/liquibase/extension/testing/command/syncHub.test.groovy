package liquibase.extension.testing.command

import liquibase.exception.CommandValidationException

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
  defaultCatalogName (String) The default catalog name to use for the database connection
    Default: null
  defaultSchemaName (String) The default schema name to use for the database connection
    Default: null
  hubConnectionId (UUID) Used to identify the specific Connection in which to record or extract data at Liquibase Hub. Available in your Liquibase Hub Project at https://hub.liquibase.com.
    Default: null
  hubProjectId (UUID) Used to identify the specific Project in which to record or extract data at Liquibase Hub. Available in your Liquibase Hub account at https://hub.liquibase.com.
    Default: null
  password (String) Password to use to connect to the database
    Default: null
    OBFUSCATED
  username (String) Username to use to connect to the database
    Default: null
"""
    run "Happy path", {
        arguments = [
                changelogFile: "changelogs/hsqldb/complete/rollback.tag.changelog.xml",
        ]

        setup {
            runChangelog "changelogs/hsqldb/complete/rollback.tag.changelog.xml"
        }

        expectedResults = [
                statusCode   : 0
        ]
    }

    run "Run without any arguments should throw an exception",  {
        arguments = [
                url: ""
        ]
        expectedException = CommandValidationException.class
    }
}
