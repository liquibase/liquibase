package liquibase.integrationtest.command

CommandTest.define {

    command = ["calculateCheckSum"]

    signature = """
Short Description: MISSING
Long Description: MISSING
Required Args:
  changeLogFile (String) MISSING DESCRIPTION
  changeSetIdentifier (String) MISSING DESCRIPTION
  url (String) MISSING DESCRIPTION
Optional Args:
  NONE
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
