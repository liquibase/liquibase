package liquibase.integrationtest.command

CommandTest.define {
    command = ["updateToTagSQL"]
    signature = """
Short Description: MISSING
Long Description: MISSING
Required Args:
  changeLogFile (String) MISSING DESCRIPTION
  url (String) MISSING DESCRIPTION
Optional Args:
  contexts (String) MISSING DESCRIPTION
    Default: null
  labels (String) MISSING DESCRIPTION
    Default: null
  tag (String) MISSING DESCRIPTION
    Default: null
"""

    run {
        arguments = [
                tag          : "version_2.0",
                changeLogFile: "changelogs/hsqldb/complete/simple.tag.changelog.xml",
        ]

        expectedResults = [
                statusMessage: "Successfully executed updateToTagSQL",
                statusCode   : 0
        ]
    }
}
