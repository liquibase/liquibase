package liquibase.extension.testing.command

import liquibase.exception.CommandValidationException

CommandTests.define {

    command = ["calculateChecksum"]

    signature = """
Short Description: Calculates and prints a checksum for the changeset
Long Description: Calculates and prints a checksum for the changeset with the given id in the format filepath::id::author
Required Args:
  changelogFile (String) The root changelog file
  url (String) The JDBC database connection URL
    OBFUSCATED
Optional Args:
  changesetAuthor (String) ChangeSet Author attribute
    Default: null
  changesetId (String) ChangeSet ID attribute
    Default: null
  changesetIdentifier (String) ChangeSet identifier of form filepath::id::author
    Default: null
  changesetPath (String) Changelog path in which the changeSet is included
    Default: null
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

    run "Happy path using changeSetIdentifier", {
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

    run "Happy path using changeSetPath, ChangeSetId and ChangeSetPath", {
        arguments = [
                url              : { it.altUrl },
                username         : { it.altUsername },
                password         : { it.altPassword },
                changesetPath    : "changelogs/h2/complete/rollback.tag.changelog.xml",
                changesetId      : "1",
                changesetAuthor  : "nvoxland",
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
