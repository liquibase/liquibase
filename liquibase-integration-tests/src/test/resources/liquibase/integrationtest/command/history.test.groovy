package liquibase.integrationtest.command

[
        new LiquibaseCommandTest.Spec(
                command: ["history"],

                expectedOutput: [
                        "Liquibase History for ",
                        """
- Database updated at 2/3/21 4:16 PM. Applied 5 changeSet(s) in 1.0s, DeploymentId: 2390606488
  db/changelog/db.changelog-master.xml::1::nvoxland
  db/changelog/sql/create_test2.sql::raw::includeAll
  db/changelog/sql/create_test3.sql::raw::includeAll
  db/changelog/changelog-x.xml::1571079854679-2::nathan (generated)
  db/changelog/changelog-x.xml::1571079854679-4::nathan (generated)
"""
                ],

                expectedResults: [
                        deployments: "1 past deployments"
                ]
        ),
] as LiquibaseCommandTest.Spec[]
