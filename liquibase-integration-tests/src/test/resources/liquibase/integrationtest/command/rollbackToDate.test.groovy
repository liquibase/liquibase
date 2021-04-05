package liquibase.integrationtest.command

CommandTest.define {
    command = ["rollbackToDate"]
    signature = """
Short Description: MISSING
Long Description: MISSING
Required Args:
  changeLogFile (String) MISSING DESCRIPTION
  date (LocalDateTime) MISSING DESCRIPTION
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
                date         : "2021-03-25T09:00:00",
                changeLogFile: "changelogs/hsqldb/complete/rollback.changelog.xml",
        ]

        setup {
            runChangelog "changelogs/hsqldb/complete/rollback.changelog.xml"
        }

        expectedResults = [
                statusMessage: "Successfully executed rollbackToDate",
                statusCode   : 0
        ]
    }
}
