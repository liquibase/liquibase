package liquibase.extension.testing.command

import liquibase.exception.CommandValidationException

CommandTests.define {

    command = ["calculateChecksum"]

    signature = """
Short Description: Calculates and prints a checksum for the changeset
Long Description: Calculates and prints a checksum for the changeset with the given id in the format filepath::id::author
Required Args:
  changelogFile (String) The root changelog file
  changesetIdentifier (String) Changeset ID identifier of form filepath::id::author
  url (String) The JDBC database connection URL
    OBFUSCATED
Optional Args:
  defaultCatalogName (String) The default catalog name to use for the database connection
    Default: null
  defaultSchemaName (String) The default schema name to use for the database connection
    Default: null
  driver (String) The JDBC driver class
    Default: null
  driverPropertiesFile (String) The JDBC driver properties file
    Default: null
  password (String) Password to use to connect to the database
    Default: null
    OBFUSCATED
  username (String) Username to use to connect to the database
    Default: null
"""

    run "Happy path", {
        arguments = [
                url              : { it.altUrl },
                username         : { it.altUsername },
                password         : { it.altPassword },
                changesetIdentifier: "changelogs/h2/complete/rollback.tag.changelog.xml::1::nvoxland",
                changelogFile    : "changelogs/h2/complete/rollback.tag.changelog.xml"
        ]

        expectedResults = [
                checksumResult   : "9:10de8cd690aed1d88d837cbe555d1684"
        ]
    }

    run "Run without changelogFile should throw an exception",  {
        arguments = [
                changesetIdentifier: "changelogs/h2/complete/rollback.tag.changelog.xml::1::nvoxland",
        ]

        expectedException = CommandValidationException.class
        expectedExceptionMessage = 'Invalid argument \'changelogFile\': missing required argument'
    }

    run "Run without changesetIdentifier should throw an exception",  {
        arguments = [
                changelogFile    : "changelogs/h2/complete/rollback.tag.changelog.xml"
        ]

        expectedException = CommandValidationException.class
        expectedExceptionMessage = "Invalid argument \'changesetIdentifier\': missing required argument"
    }

    run "Run without URL should throw an exception",  {
        arguments = [
                url: "",
                changelogFile    : "changelogs/h2/complete/rollback.tag.changelog.xml",
                changesetIdentifier: "changelogs/h2/complete/rollback.tag.changelog.xml::1::nvoxland",
        ]

        expectedException = CommandValidationException.class
        expectedExceptionMessage = "Invalid argument \'url\': missing required argument"
    }
}
