package liquibase.extension.testing.command


import liquibase.exception.CommandValidationException
import liquibase.util.TestUtil

CommandTests.define {
    command = ["updateCount"]
    signature = """
Short Description: Deploy the specified number of changes from the changelog file
Long Description: NOT SET
Required Args:
  changelogFile (String) The root changelog
  count (Integer) The number of changes in the changelog to deploy
  url (String) The JDBC database connection URL
    OBFUSCATED
Optional Args:
  changeExecListenerClass (String) Fully-qualified class which specifies a ChangeExecListener
    Default: null
  changeExecListenerPropertiesFile (String) Path to a properties file for the ChangeExecListenerClass
    Default: null
  contextFilter (String) Changeset contexts to match
    Default: null
  defaultCatalogName (String) The default catalog name to use for the database connection
    Default: null
  defaultSchemaName (String) The default schema name to use for the database connection
    Default: null
  driver (String) The JDBC driver class
    Default: null
  driverPropertiesFile (String) The JDBC driver properties file
    Default: null
  labelFilter (String) Changeset labels to match
    Default: null
  password (String) Password to use to connect to the database
    Default: null
    OBFUSCATED
  showSummary (UpdateSummaryEnum) Type of update results summary to show.  Values can be 'off', 'summary', or 'verbose'.
    Default: SUMMARY
  showSummaryOutput (UpdateSummaryOutputEnum) Summary output to report update summary results. Values can be 'log', 'console', or 'all'.
    Default: ALL
  username (String) Username to use to connect to the database
    Default: null
"""

    run "Happy path", {
        arguments = [
                url:      { it.url},
                username: { it.username },
                password: { it.password },
                count        : 1,
                changelogFile: "changelogs/h2/complete/simple.changelog.xml",
        ]

        expectedResults = [
                statusCode   : 0,
                defaultChangeExecListener: 'not_null',
                updateReport: 'not_null'
        ]

        expectations = {
            TestUtil.assertAllDeploymentIdsNonNull()
        }
    }

    run "Happy path with verbose summary output", {
        arguments = [
                url:      { it.url },
                username: { it.username },
                password: { it.password },
                count        : 1,
                showSummary  : "verbose",
                changelogFile: "changelogs/h2/complete/simple.changelog.xml",
        ]

        expectedResults = [
                statusCode   : 0,
                defaultChangeExecListener: 'not_null',
                updateReport: 'not_null'
        ]
    }

    run "Happy path with a change set that has complicated labels and contexts", {
        arguments = [
                url:        { it.url },
                username:   { it.username },
                password:   { it.password },
                changelogFile: "changelogs/h2/complete/summary-changelog.xml",
                count: "1",
                labelFilter: "testtable4,tagit and !testtable2",
                contextFilter: "none",
                showSummary: "summary"
        ]

        outputFile = new File("target/test-classes/labelsAndContent.txt")

        expectedFileContent = [ "target/test-classes/labelsAndContent.txt":
                    [
                      "UPDATE SUMMARY",
                      "Run:                          1",
                      "Previously run:               0",
                      "Filtered out:                 5",
                      "-------------------------------",
                      "Total change sets:            6",
                      "FILTERED CHANGE SETS SUMMARY",
                      "Label mismatch:               2",
                      "Context mismatch:             2",
                      "After count:                  1",
                      "DBMS mismatch:                1"
                    ]
        ]

        expectedResults = [
                statusCode   : 0,
                defaultChangeExecListener: 'not_null',
                updateReport: 'not_null'
        ]

        expectedUI = [
            "Running Changeset: changelogs/h2/complete/summary-changelog.xml::4-table::lbuser"
        ]

    }

    run "Happy path with a change set that has complicated labels and contexts with log output", {
        arguments = [
                url:        { it.url },
                username:   { it.username },
                password:   { it.password },
                changelogFile: "changelogs/h2/complete/summary-changelog.xml",
                count: "1",
                labelFilter: "testtable4,tagit and !testtable2",
                contextFilter: "none",
                showSummary: "summary",
                showSummaryOutput: "log"
        ]

        expectedLogs = [
                "UPDATE SUMMARY",
                "Run:                          1",
                "Previously run:               0",
                "Filtered out:                 5",
                "-------------------------------",
                "Total change sets:            6",
                "FILTERED CHANGE SETS SUMMARY",
                "Label mismatch:               2",
                "Context mismatch:             2",
                "After count:                  1",
                "DBMS mismatch:                1"
        ]

        expectedResults = [
                statusCode   : 0,
                defaultChangeExecListener: 'not_null',
                updateReport: 'not_null'
        ]

        expectedUI = [
            "Running Changeset: changelogs/h2/complete/summary-changelog.xml::4-table::lbuser"
        ]

    }

    run "Happy path with skipped change sets propagated from an included changelog", {
        arguments = [
                url:        { it.url },
                username:   { it.username },
                password:   { it.password },
                changelogFile: "changelogs/h2/complete/summary-changelog.xml",
                count: "1",
                labelFilter: "testtable4,tagit and !testtable2",
                contextFilter: "none",
                showSummary: "summary"
        ]

        outputFile = new File("target/test-classes/skippedPropagatedToRoot.txt")

        expectedFileContent = [ "target/test-classes/skippedPropagatedToRoot.txt":
                    [
                      "UPDATE SUMMARY",
                      "Run:                          1",
                      "Previously run:               0",
                      "Filtered out:                 5",
                      "-------------------------------",
                      "Total change sets:            6",
                      "FILTERED CHANGE SETS SUMMARY",
                      "Label mismatch:               2",
                      "Context mismatch:             2",
                      "After count:                  1",
                      "DBMS mismatch:                1"
                    ]
        ]

        expectedResults = [
                statusCode   : 0,
                defaultChangeExecListener: 'not_null',
                updateReport: 'not_null'
        ]

        expectedUI = [
            "Running Changeset: changelogs/h2/complete/summary-changelog.xml::4-table::lbuser"
        ]

    }

    run "Mismatched DBMS causes not deployed summary message", {
        arguments = [
                url:        { it.url },
                username:   { it.username },
                password:   { it.password },
                count        : 1,
                showSummary  : "verbose",
                changelogFile: "changelogs/h2/complete/mismatchedDbms.changelog.xml"
        ]

        expectedResults = [
                statusCode   : 0,
                defaultChangeExecListener: 'not_null',
                updateReport: 'not_null'
        ]

        outputFile = new File("target/test-classes/mismatchedDBMS.txt")
        expectedFileContent = [ "target/test-classes/mismatchedDBMS.txt":
                 [
                   """UPDATE SUMMARY
                   Run:                          1
                   Previously run:               0
                   Filtered out:                 2
                   -------------------------------
                   Total change sets:            3
                   FILTERED CHANGE SETS SUMMARY
                   After count:                  1
                   DBMS mismatch:                1
                   +--------------------------------------------------------------+--------------------------------+
                   | Changeset Info                                               | Reason Skipped                 |
                   +--------------------------------------------------------------+--------------------------------+
                   | changelogs/h2/complete/mismatchedDbms.changelog.xml::1::nvox | mismatched DBMS value of 'foo' |
                   | land                                                         |                                |
                   +--------------------------------------------------------------+--------------------------------+
                   | changelogs/h2/complete/mismatchedDbms.changelog.xml::13.1::t | Only running 1 changeset       |
                   | estuser                                                      |                                |
                   +--------------------------------------------------------------+--------------------------------+"""
                 ]
        ]
    }

    run "Run without a URL throws an exception", {
        arguments = [
                url: "",
                count : 1
        ]
        expectedException = CommandValidationException.class
    }

    run "Run with a bad show summary option throws an exception", {
        arguments = [
                url                    : { it.url },
                username               : { it.username },
                password               : { it.password },
                changelogFile          : 'changelogs/h2/complete/simple.changelog.xml',
                showSummary: "foo",
                count : 1
        ]
        expectedException = IllegalArgumentException.class
    }

    run "Run without a changeLogFile throws an exception", {
        arguments = [
                changelogFile: "",
                count: 1
        ]
        expectedException = CommandValidationException.class
    }

    run "Run without count throws an exception", {
        arguments = [
                changelogFile: "changelogs/h2/complete/simple.changelog.xml"
        ]
        expectedException = CommandValidationException.class
    }

    run "Run without any argument throws an exception", {
        arguments = [
                url: "",
        ]
        expectedException = CommandValidationException.class
    }
}
