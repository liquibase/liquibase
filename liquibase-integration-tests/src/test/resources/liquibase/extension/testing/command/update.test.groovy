package liquibase.extension.testing.command

import liquibase.exception.CommandValidationException

import java.util.regex.Pattern

CommandTests.define {
    command = ["update"]
    signature = """
Short Description: Deploy any changes in the changelog file that have not been deployed
Long Description: NOT SET
Required Args:
  changelogFile (String) The root changelog
  url (String) The JDBC database connection URL
Optional Args:
  contexts (String) Changeset contexts to match
    Default: null
  defaultCatalogName (String) The default catalog name to use for the database connection
    Default: null
  defaultSchemaName (String) The default schema name to use for the database connection
    Default: null
  labels (String) Changeset labels to match
    Default: null
  password (String) Password to use to connect to the database
    Default: null
    OBFUSCATED
  username (String) Username to use to connect to the database
    Default: null
"""

    run "Happy path with a simple changelog", {
        arguments = [
                changelogFile: "changelogs/hsqldb/complete/simple.changelog.xml"
        ]

        expectedResults = [
                statusCode   : 0
        ]

        expectedDatabaseContent = [
                "txt": [Pattern.compile(".*liquibase.structure.core.Table:.*ADDRESS.*", Pattern.MULTILINE|Pattern.DOTALL),
                        Pattern.compile(".*liquibase.structure.core.Table:.*ADDRESS.*columns:.*CITY.*", Pattern.MULTILINE|Pattern.DOTALL)]
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
