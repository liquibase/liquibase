package liquibase.extension.testing.command

import liquibase.extension.testing.setup.HistoryEntry

CommandTests.define {
    command = ["clearCheckSums"]

    signature = """
Short Description: Clears all checksums
Long Description: Clears all checksums and nullifies the MD5SUM column of the DATABASECHANGELOG table so that they will be re-computed on the next database update
Required Args:
  url (String) The JDBC database connection URL
Optional Args:
  password (String) The database password
    Default: null
  username (String) The database username
    Default: null
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
