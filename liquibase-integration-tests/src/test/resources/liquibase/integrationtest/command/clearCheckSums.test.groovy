package liquibase.integrationtest.command

import liquibase.integrationtest.setup.HistoryEntry

CommandTest.define {
    command = ["clearCheckSums"]

    signature = """
Short Description: MISSING
Long Description: MISSING
Required Args:
  url (String) MISSING DESCRIPTION
Optional Args:
  NONE
"""

    run {
        setup {
            history = [
                    new HistoryEntry(
                            id: "1",
                            author: "test",
                            path: "com/example/changelog.xml"
                    ),
                    new HistoryEntry(
                            id: "2",
                            author: "test",
                            path: "com/example/changelog.xml"
                    ),
            ]
        }

        expectedResults = [
                statusMessage: "Successfully executed clearCheckSums",
                statusCode   : 0
        ]

    }
}
