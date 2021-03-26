package liquibase.integrationtest.command

import liquibase.integrationtest.command.CommandTest
import liquibase.integrationtest.setup.SetupChangelogHistory

import java.util.regex.Pattern

[
        new CommandTest.Spec(
                command: ["history"],

                setup: [
                        new SetupChangelogHistory([
                                [
                                        id    : "1",
                                        author: "nvoxland",
                                        path  : "db/changelog/db.changelog-master.xml",
                                ] as SetupChangelogHistory.Entry,
                                [
                                        id    : "raw",
                                        author: "includeAll",
                                        path  : "db/changelog/sql/create_test2.sql",
                                ] as SetupChangelogHistory.Entry,
                                [
                                        id    : "raw",
                                        author: "includeAll",
                                        path  : "db/changelog/sql/create_test3.sql",
                                ] as SetupChangelogHistory.Entry,
                                [
                                        id    : "1571079854679-2",
                                        author: "nathan (generated)",
                                        path  : "db/changelog/changelog-x.xml",
                                ] as SetupChangelogHistory.Entry,
                        ])
                ],

                expectedOutput: [
//                        Pattern.compile("""
//Liquibase History for \\w+
//  db/changelog/db.changelog-master.xml::1::nvoxland
//  db/changelog/sql/create_test2.sql::raw::includeAll
//  db/changelog/sql/create_test3.sql::raw::includeAll
//  db/changelog/changelog-x.xml::1571079854679-2::nathan (generated)
//""")
                        Pattern.compile("""
- Database updated at \\d+/\\d+.+. Applied 4 changeSet\\(s\\) in \\d+.\\d+s, DeploymentId: \\d+
  db/changelog/db.changelog-master.xml::1::nvoxland
  db/changelog/sql/create_test2.sql::raw::includeAll
  db/changelog/sql/create_test3.sql::raw::includeAll
  db/changelog/changelog-x.xml::1571079854679-2::nathan \\(generated\\)
""".replace("\r", "").trim())
                ],

                expectedResults: [
                        deployments: "1 past deployments",
                        statusCode: 0
                ]
        ),
] as CommandTest.Spec[]
