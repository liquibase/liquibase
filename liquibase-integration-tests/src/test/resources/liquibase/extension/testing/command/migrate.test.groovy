package liquibase.extension.testing.command

import liquibase.exception.CommandValidationException

CommandTests.define {
    command = ["migrate"]
    signature = """
Short Description: Deploys changes from the changelog file that have not yet been deployed
Long Description: NOT SET
Required Args:
  changelogFile (String) The root changelog
  url (String) The JDBC database connection URL
Optional Args:
  contexts (String) Context string to use for filtering which changes to migrate
    Default: null
  defaultCatalogName (String) The default catalog name to use for the database connection
    Default: null
  defaultSchemaName (String) The default schema name to use for the database connection
    Default: null
  labels (String) Label expression to use for filtering which changes to migrate
    Default: null
  password (String) Password to use to connect to the database
    Default: null
    OBFUSCATED
  username (String) Username to use to connect to the database
    Default: null
"""
    run "Happy path", {
        arguments = [
                changelogFile: "changelogs/hsqldb/complete/simple.changelog.xml",
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
}
