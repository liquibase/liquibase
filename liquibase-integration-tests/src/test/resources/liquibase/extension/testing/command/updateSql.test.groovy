package liquibase.extension.testing.command

CommandTests.define {
    command = ["updateSql"]
    signature = """
Short Description: Generate the SQL to deploy changes in the changelog which have not been deployed
Long Description: NOT SET
Required Args:
  url (String) The JDBC database connection URL
Optional Args:
  changelogFile (String) The root changelog
    Default: null
  contexts (String) Changeset contexts to match
    Default: null
  labels (String) Changeset labels to match
    Default: null
  password (String) Password to use to connect to the database
    Default: null
  username (String) Username to use to connect to the database
    Default: null
"""

    run {
        arguments = [
                changelogFile: "changelogs/hsqldb/complete/simple.changelog.xml"
        ]

        expectedOutput = [
                """
-- *********************************************************************
-- Update Database Script
-- *********************************************************************
"""
        ]

        expectedResults = [
                statusCode   : 0
        ]
    }
}
