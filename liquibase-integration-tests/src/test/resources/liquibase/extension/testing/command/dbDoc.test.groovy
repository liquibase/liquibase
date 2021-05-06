package liquibase.extension.testing.command

CommandTests.define {
    command = ["dbDoc"]

    signature = """
Short Description: Generates JavaDoc documentation for the existing database and changelogs
Long Description: NOT SET
Required Args:
  changelogFile (String) The root changelog
  outputDirectory (String) The directory where the documentation is generated
  url (String) The JDBC database connection URL
Optional Args:
  password (String) The database password
    Default: null
  username (String) The database username
    Default: null
"""

    run {
        arguments = [
                outputDirectory: "target/test-classes",
                changelogFile  : "changelogs/hsqldb/complete/simple.changelog.xml",
        ]

        expectedResults = [
                statusCode   : 0
        ]
    }
}
