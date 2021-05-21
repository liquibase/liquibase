package liquibase.extension.testing.command

import liquibase.exception.CommandValidationException

CommandTests.define {

    command = ["calculateChecksum"]

    signature = """
Short Description: Calculates and prints a checksum for the changeset
Long Description: Calculates and prints a checksum for the changeset with the given id in the format filepath::id::author
Required Args:
  changelogFile (String) The root changelog file
  changesetIdentifier (String) Change set ID identifier of form filepath::id::author
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
                url              : { it.altUrl },
                username         : { it.altUsername },
                password         : { it.altPassword },
                changesetIdentifier: "changelogs/hsqldb/complete/rollback.tag.changelog.xml::1::nvoxland",
                changelogFile    : "changelogs/hsqldb/complete/rollback.tag.changelog.xml"
        ]

        expectedResults = [
                statusCode   : 0
        ]
    }

    run "Run without changelogFile should throw an exception",  {
        arguments = [
                changesetIdentifier: "changelogs/hsqldb/complete/rollback.tag.changelog.xml::1::nvoxland",
        ]

        expectedException = CommandValidationException.class
    }

    run "Run without changesetIdentifier should throw an exception",  {
        arguments = [
                changelogFile    : "changelogs/hsqldb/complete/rollback.tag.changelog.xml"
        ]

        expectedException = CommandValidationException.class
    }

    run "Run without URL should throw an exception",  {
        arguments = [
                url: "",
                changelogFile    : "changelogs/hsqldb/complete/rollback.tag.changelog.xml"
        ]

        expectedException = CommandValidationException.class
    }
}
