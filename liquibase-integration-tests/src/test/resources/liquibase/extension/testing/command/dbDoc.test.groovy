package liquibase.extension.testing.command

import liquibase.exception.CommandValidationException

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
  defaultCatalogName (String) The default catalog name to use for the database connection
    Default: null
  defaultSchemaName (String) The default schema name to use for the database connection
    Default: null
  driver (String) The JDBC driver class
    Default: null
  driverPropertiesFile (String) The JDBC driver properties file
    Default: null
  password (String) The database password
    Default: null
    OBFUSCATED
  username (String) The database username
    Default: null
"""

    run "Happy path", {
        arguments = [
                url:        { it.url },
                username:   { it.username },
                password:   { it.password },
                outputDirectory: "target/test-classes",
                changelogFile  : "changelogs/hsqldb/complete/simple.changelog.xml",
        ]

        expectedResults = [
                statusCode   : 0
        ]
    }

    run "Run without an outputDirectory should throw an exception",  {
        arguments = [
                changelogFile: "changelogs/hsqldb/complete/rollback.tag.changelog.xml",
                outputDirectory: ""
        ]
        expectedException = CommandValidationException.class
    }

    run "Run without a changeLogFile should throw an exception",  {
        arguments = [
                outputDirectory: "target/test-classes"
        ]
        expectedException = CommandValidationException.class
    }

    run "Run without a URL should throw an exception",  {
        arguments = [
                url          : "",
                changelogFile: "changelogs/hsqldb/complete/rollback.tag.changelog.xml",
                outputDirectory: "version_2.0"
        ]
        expectedException = CommandValidationException.class
    }

    run "Run without any arguments should throw an exception",  {
        arguments = [
                url:  ""
        ]

        expectedException = CommandValidationException.class
    }
}
