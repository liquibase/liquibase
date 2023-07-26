package liquibase.extension.testing.command

import liquibase.exception.CommandValidationException
import liquibase.exception.CommandExecutionException

CommandTests.define {
    command = ["validate"]
    signature = """
Short Description: Validate the changelog for errors
Long Description: NOT SET
Required Args:
  changelogFile (String) The root changelog file
  url (String) The JDBC database connection URL
    OBFUSCATED
Optional Args:
  contextFilter (String) Context string to use for filtering
    Default: null
  defaultCatalogName (String) The default catalog name to use for the database connection
    Default: null
  defaultSchemaName (String) The default schema name to use for the database connection
    Default: null
  driver (String) The JDBC driver class
    Default: null
  driverPropertiesFile (String) The JDBC driver properties file
    Default: null
  labelFilter (String) Label expression to use for filtering
    Default: null
  password (String) Password to use to connect to the database
    Default: null
    OBFUSCATED
  username (String) Username to use to connect to the database
    Default: null
"""

    run "Happy path", {
        arguments = [
                url:        { it.url },
                username:   { it.username },
                password:   { it.password },
                changelogFile: "changelogs/h2/complete/simple.changelog.xml"
        ]

        expectedResults = [
                statusCode   : 0
        ]
    }

    run "Run without a URL throws an exception", {
        arguments = [
                url: ""
        ]
        expectedException = CommandValidationException.class
    }

    run "Run without a changeLogFile throws an exception", {
        arguments = [
                changelogFile: ""
        ]
        expectedException = CommandValidationException.class
    }

    run "Run without any argument throws an exception", {
        arguments = [
                url: "",
                changelogFile: ""
        ]
        expectedException = CommandValidationException.class
    }

    run "Run with changeset having invalid tag throws an exception", {
        arguments = [
                url:        { it.url },
                username:   { it.username },
                password:   { it.password },
                changelogFile: "changelogs/common/invalid.xsd.changelog.xml"
        ]

        expectedException = CommandExecutionException.class
    }
}
