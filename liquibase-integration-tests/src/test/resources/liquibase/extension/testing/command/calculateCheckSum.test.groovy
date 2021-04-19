package liquibase.extension.testing.command

CommandTests.define {

    command = ["calculateCheckSum"]

    signature = """
Short Description: Calculates and prints a checksum for the changeset
Long Description: Calculates and prints a checksum for the changeset with the given id in the format filepath::id::author
Required Args:
  changeLogFile (String) The root changelog file
  changeSetIdentifier (String) Change set ID identifier of form filepath::id::author
  url (String) The JDBC database connection URL
Optional Args:
  username (String) The database username
    Default: null
"""

    run {
        arguments = [
                changeSetIdentifier: "changelogs/hsqldb/complete/rollback.tag.changelog.xml::1::nvoxland",
                "changeLogFile"    : "changelogs/hsqldb/complete/rollback.tag.changelog.xml"
        ]

        expectedResults = [
                statusMessage: "Successfully executed calculateCheckSum",
                statusCode   : 0
        ]
    }
}
