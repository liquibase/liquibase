package liquibase.integrationtest.command

import java.util.regex.Pattern

import static liquibase.integrationtest.setup.SetupChangelogHistory.setupChangelogHistory

CommandTest.define {
    command = ["history" ]

    run {
        setup setupChangelogHistory(
                {
                    id = "1"
                    author = "nvoxland"
                    path = "db/changelog/db.changelog-master.xml"
                },
                {
                    id = "raw"
                    author = "includeAll"
                    path = "db/changelog/sql/create_test2.sql"
                },
                {
                    id = "raw"
                    author = "includeAll"
                    path = "db/changelog/sql/create_test3.sql"
                },
                {
                    id = "1571079854679-2"
                    author = "nathan (generated)"
                    path = "db/changelog/changelog-x.xml"
                },
        )

        expectedOutput(
                Pattern.compile("""
- Database updated at \\d+/\\d+.+. Applied 4 changeSet\\(s\\) in \\d+.\\d+s, DeploymentId: \\d+
  db/changelog/db.changelog-master.xml::1::nvoxland
  db/changelog/sql/create_test2.sql::raw::includeAll
  db/changelog/sql/create_test3.sql::raw::includeAll
  db/changelog/changelog-x.xml::1571079854679-2::nathan \\(generated\\)
""".replace("\r", "").trim())
        )

        expectedResults([
                deployments: "1 past deployments",
                statusCode : 0
        ])
    }
}

