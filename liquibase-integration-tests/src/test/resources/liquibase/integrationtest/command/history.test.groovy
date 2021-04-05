package liquibase.integrationtest.command

import liquibase.integrationtest.setup.HistoryEntry

import java.util.regex.Pattern

CommandTest.define {
    command = ["history"]
    signature = """
Short Description: MISSING
Long Description: MISSING
Required Args:
  database (Database) MISSING DESCRIPTION
Optional Args:
  dateFormat (DateFormat) MISSING DESCRIPTION
    Default: Platform specific 'short' format
"""

    run {
        setup {
            history = [
                    new HistoryEntry(
                            id: "1",
                            author: "nvoxland",
                            path: "db/changelog/db.changelog-master.xml",
                    ),
                    new HistoryEntry(
                            id: "raw",
                            author: "includeAll",
                            path: "db/changelog/sql/create_test2.sql",
                    ),
                    new HistoryEntry(
                            id: "raw",
                            author: "includeAll",
                            path: "db/changelog/sql/create_test3.sql",
                    ),
                    new HistoryEntry(
                            id: "1571079854679-2",
                            author: "nathan (generated)",
                            path: "db/changelog/changelog-x.xml",
                    ),
            ]
        }

        expectedOutput = [
                Pattern.compile("""
- Database updated at \\d+/\\d+.+. Applied 4 changeSet\\(s\\) in \\d+.\\d+s, DeploymentId: \\d+
  db/changelog/db.changelog-master.xml::1::nvoxland
  db/changelog/sql/create_test2.sql::raw::includeAll
  db/changelog/sql/create_test3.sql::raw::includeAll
  db/changelog/changelog-x.xml::1571079854679-2::nathan \\(generated\\)
""".replace("\r", "").trim())
        ]

        expectedResults = [
                deployments: "1 past deployments",
                statusCode : 0
        ]
    }
}

