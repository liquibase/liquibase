package liquibase.integrationtest.command

CommandTest.define {
    command = ["unexpectedChangeSets"]
    signature = """
Short Description: MISSING
Long Description: MISSING
Required Args:
  changeLogFile (String) MISSING DESCRIPTION
  url (String) MISSING DESCRIPTION
Optional Args:
  contexts (String) MISSING DESCRIPTION
    Default: null
  verbose (String) MISSING DESCRIPTION
    Default: null
"""

    run {
        arguments = [
                verbose      : "true",
                changeLogFile: "changelogs/hsqldb/complete/unexpected.tag.changelog.xml",
        ]

        setup {
            syncChangelog "changelogs/hsqldb/complete/rollback.tag.changelog.xml"
        }

        expectedResults = [
                statusMessage: "Successfully executed unexpectedChangeSets",
                statusCode   : 0
        ]
    }
}
