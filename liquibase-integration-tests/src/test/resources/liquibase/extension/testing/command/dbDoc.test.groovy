package liquibase.extension.testing.command

CommandTests.define {
    command = ["dbDoc"]

    signature = """
Short Description: Generates JavaDoc documentation for the existing database and changelogs
Long Description: Generates JavaDoc documentation for the existing database and changelogs
Required Args:
  changeLogFile (String) The root changelog
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
                changeLogFile  : "changelogs/hsqldb/complete/simple.changelog.xml",
        ]

        expectedResults = [
                statusMessage: "Successfully executed dbDoc",
                statusCode   : 0
        ]
    }
}
