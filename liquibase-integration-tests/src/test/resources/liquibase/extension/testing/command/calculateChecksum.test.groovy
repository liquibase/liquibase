package liquibase.extension.testing.command

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

    run {
        arguments = [
                changesetIdentifier: "changelogs/hsqldb/complete/rollback.tag.changelog.xml::1::nvoxland",
                "changelogFile"    : "changelogs/hsqldb/complete/rollback.tag.changelog.xml"
        ]

        expectedResults = [
                statusMessage: "Successfully executed calculateChecksum",
                statusCode   : 0
        ]
    }
}
