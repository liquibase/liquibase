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
  password (String) The database password
    Default: null
  username (String) The database username
    Default: null
"""

    run "Happy path", {
        arguments = [
                changesetIdentifier: "changelogs/hsqldb/complete/rollback.tag.changelog.xml::1::nvoxland",
                "changelogFile"    : "changelogs/hsqldb/complete/rollback.tag.changelog.xml"
        ]

        expectedResults = [
                statusMessage: "Successfully executed calculateChecksum",
                statusCode   : 0
        ]
    }

    run "Run without changeLogFile should throw an exception",  {
        arguments = [
                changeSetIdentifier: "changelogs/hsqldb/complete/rollback.tag.changelog.xml::1::nvoxland",
        ]

        expectedException = CommandValidationException.class
    }

    run "Run without changeSetIdentifier should throw an exception",  {
        arguments = [
                "changeLogFile"    : "changelogs/hsqldb/complete/rollback.tag.changelog.xml"
        ]

        expectedException = CommandValidationException.class
    }

    run "Run without URL should throw an exception",  {
        arguments = [
                "url": "",
                "changeLogFile"    : "changelogs/hsqldb/complete/rollback.tag.changelog.xml"
        ]

        expectedException = CommandValidationException.class
    }
}
