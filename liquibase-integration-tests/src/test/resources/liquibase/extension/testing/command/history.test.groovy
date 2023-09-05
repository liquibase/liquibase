package liquibase.extension.testing.command

import liquibase.exception.CommandValidationException
import liquibase.extension.testing.setup.HistoryEntry


CommandTests.define {
    command = ["history"]
    signature = """
Short Description: List all deployed changesets and their deployment ID
Long Description: NOT SET
Required Args:
  url (String) The JDBC database connection URL
    OBFUSCATED
Optional Args:
  changelogFile (String) The root changelog
    Default: null
  defaultCatalogName (String) The default catalog name to use for the database connection
    Default: null
  defaultSchemaName (String) The default schema name to use for the database connection
    Default: null
  driver (String) The JDBC driver class
    Default: null
  driverPropertiesFile (String) The JDBC driver properties file
    Default: null
  format (HistoryFormat) History output format
    Default: TABULAR
  password (String) Password to use to connect to the database
    Default: null
    OBFUSCATED
  username (String) Username to use to connect to the database
    Default: null
"""
    run "Happy path", {
        arguments = [
                url : { it.url },
                username: { it.username },
                password: { it.password }
        ]
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
                """
Liquibase History for jdbc:h2:mem:lbcat
""",
~/[-+]+/,
"| db/changelog/db.changelog-master.xml | nvoxland | 1 | |",
"| db/changelog/sql/create_test2.sql | includeAll | raw | |",
"| db/changelog/sql/create_test3.sql | includeAll | raw | |",
"| db/changelog/changelog-x.xml | nathan (generated) | 1571079854679-2 | |"]

        expectedResults = [
                deployments: "1 past deployments",
                statusCode : 0
        ]
    }

    run "Happy path with tag", {
        arguments = [
                url : { it.url },
                username: { it.username },
                password: { it.password }
        ]
        setup {
            runChangelog "changelogs/h2/complete/rollback.tag.changelog.xml"
        }

        expectedOutput = [
                """
Liquibase History for jdbc:h2:mem:lbcat
""",
~/[-+]+/,
"| changelogs/h2/complete/rollback.tag.changelog.xml | nvoxland         | 1            |             |",
"| changelogs/h2/complete/rollback.tag.changelog.xml | nvoxland         | 1.1          |             |",
"| changelogs/h2/complete/rollback.tag.changelog.xml | nvoxland         | 2            |             |",
"| changelogs/h2/complete/rollback.tag.changelog.xml | testuser         | 13.1         | version_2.0 |",
"| changelogs/h2/complete/rollback.tag.changelog.xml | nvoxland         | 14           |             |"]

        expectedResults = [
                deployments: "1 past deployments",
                statusCode : 0
        ]
    }

    run "Happy path with an output file", {
        arguments = [
            url : { it.url },
            username: { it.username },
            password: { it.password }
        ]
        setup {
            cleanResources("target/test-classes/history.sql")
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

        outputFile = new File("target/test-classes/history.sql")

        expectedFileContent = [
                //
                // Find the " -- Release Database Lock" line
                //
                "target/test-classes/history.sql" : [
                        """
Liquibase History for jdbc:h2:mem:lbcat
""",
~/[-+]+/,
"| db/changelog/db.changelog-master.xml | nvoxland | 1 | |",
"| db/changelog/sql/create_test2.sql | includeAll | raw | |",
"| db/changelog/sql/create_test3.sql | includeAll | raw | |",
"| db/changelog/changelog-x.xml | nathan (generated) | 1571079854679-2 | |" ]
        ]

        expectedResults = [
                deployments: "1 past deployments",
                statusCode : 0
        ]
    }

    run "Run without any arguments should throw an exception",  {
        arguments = [
                url: ""
        ]
        expectedException = CommandValidationException.class
    }
}
