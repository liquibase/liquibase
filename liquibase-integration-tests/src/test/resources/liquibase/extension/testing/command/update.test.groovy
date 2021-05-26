package liquibase.extension.testing.command

import liquibase.exception.CommandValidationException

import java.util.regex.Pattern

CommandTests.define {
    command = ["update"]
    signature = """
Short Description: Deploy any changes in the changelog file that have not been deployed
Long Description: Deploy any changes in the changelog file that have not been deployed
Required Args:
  changeLogFile (String) The root changelog
  url (String) The JDBC database connection URL
Optional Args:
  contexts (String) Changeset contexts to match
    Default: null
  labels (String) Changeset labels to match
    Default: null
  password (String) Password to use to connect to the database
    Default: null
  username (String) Username to use to connect to the database
    Default: null
"""

    run "Happy path with a simple changelog", {
        arguments = [
                changeLogFile: "changelogs/hsqldb/complete/simple.changelog.xml"
        ]

        expectedResults = [
                statusMessage: "Successfully executed update",
                statusCode   : 0
        ]

        expectedDatabaseContent = [
                "txt": [Pattern.compile(".*liquibase.structure.core.Table:.*ADDRESS.*", Pattern.MULTILINE|Pattern.DOTALL),
                        Pattern.compile(".*liquibase.structure.core.Table:.*ADDRESS.*columns:.*CITY.*", Pattern.MULTILINE|Pattern.DOTALL)]
        ]
    }

    run "No changelog argument results in an exception", {
        expectedException = CommandValidationException.class
    }

    run "Empty url argument results in an exception", {
        arguments = [
                url: "",
                changeLogFile: "changelogs/hsqldb/complete/simple.changelog.xml"
        ]

        expectedException = CommandValidationException.class
    }
}
