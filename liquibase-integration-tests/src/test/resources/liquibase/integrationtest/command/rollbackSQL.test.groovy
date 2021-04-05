package liquibase.integrationtest.command

CommandTest.define {
    command = ["rollbackSQL"]
    signature = """
Short Description: MISSING
Long Description: MISSING
Required Args:
  changeLogFile (String) MISSING DESCRIPTION
  tag (String) MISSING DESCRIPTION
  url (String) MISSING DESCRIPTION
Optional Args:
  contexts (String) MISSING DESCRIPTION
    Default: null
  labels (String) MISSING DESCRIPTION
    Default: null
  rollbackScript (String) MISSING DESCRIPTION
    Default: null
"""

    run {
        arguments = [
                tag          : "version_2.0",
                changeLogFile: "changelogs/hsqldb/complete/rollback.tag.changelog.xml",
        ]

        setup {
            runChangelog "changelogs/hsqldb/complete/rollback.tag.changelog.xml"
        }

        expectedResults = [
                statusMessage: "Successfully executed rollbackSQL",
                statusCode   : 0
        ]
    }
}
