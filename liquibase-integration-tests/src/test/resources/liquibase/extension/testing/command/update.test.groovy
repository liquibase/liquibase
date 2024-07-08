package liquibase.extension.testing.command

import liquibase.Scope
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.changelog.RanChangeSet
import liquibase.database.Database
import liquibase.exception.CommandExecutionException
import liquibase.exception.CommandValidationException

import java.util.regex.Pattern

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertNotNull

CommandTests.define {
    command = ["update"]
    signature = """
Short Description: Deploy any changes in the changelog file that have not been deployed
Long Description: NOT SET
Required Args:
  changelogFile (String) The root changelog
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

    run "Happy path with a simple changelog", {
        arguments = [
                url          : { it.url },
                username     : { it.username },
                password     : { it.password },
                changelogFile: "changelogs/h2/complete/simple.changelog.xml"
        ]

        expectedResults = [
                statusCode: 0,
                defaultChangeExecListener: 'not_null',
                updateReport: 'not_null'
        ]

        expectedDatabaseContent = [
                "txt": [Pattern.compile(".*liquibase.structure.core.Table:.*ADDRESS.*", Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
                        Pattern.compile(".*liquibase.structure.core.Table:.*ADDRESS.*columns:.*city.*", Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE)]
        ]

         expectations = {
             // Check that the order executed number increments by 1 for each changeset
             def database = (Database) Scope.getCurrentScope().get("database", null)
             def changelogHistoryService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database)
             List<RanChangeSet> ranChangeSets = changelogHistoryService.getRanChangeSets()
             int expectedOrder = 1
             for (RanChangeSet ranChangeSet : ranChangeSets) {
                 assertEquals(expectedOrder, ranChangeSet.getOrderExecuted())
                 expectedOrder++
                 assertNotNull(ranChangeSet.getDeploymentId())
             }
        }

    }

    run "Happy path with a simple changelog showing summary", {
        arguments = [
                url          : { it.url },
                username     : { it.username },
                password     : { it.password },
                changelogFile: "changelogs/h2/complete/simple.changelog.xml",
                showSummary: "SUMMARY"
        ]

        expectedResults = [
                statusCode: 0,
                defaultChangeExecListener: 'not_null',
                updateReport: 'not_null'
        ]

        expectedDatabaseContent = [
                "txt": [Pattern.compile(".*liquibase.structure.core.Table:.*ADDRESS.*", Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
                        Pattern.compile(".*liquibase.structure.core.Table:.*ADDRESS.*columns:.*city.*", Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE)]
        ]

        outputFile = new File("target/test-classes/happyPath.txt")

        expectedFileContent = [ "target/test-classes/happyPath.txt":
                [
                  "UPDATE SUMMARY",
                  "Run:                         41",
                  "Previously run:               0",
                  "Filtered out:                 0",
                  "-------------------------------",
                  "Total change sets:           41",
                ]
        ]
    }

    run "Happy path with an ignored change set", {
        arguments = [
                url          : { it.url },
                username     : { it.username },
                password     : { it.password },
                changelogFile: "changelogs/h2/complete/ignored.changelog.xml",
                showSummary  : "summary"
        ]

        expectedResults = [
                statusCode: 0,
                defaultChangeExecListener: 'not_null',
                updateReport: 'not_null'
        ]

        outputFile = new File("target/test-classes/ignoredChangeset.txt")

        expectedFileContent = [ "target/test-classes/ignoredChangeset.txt":
                [
                  "UPDATE SUMMARY",
                  "Run:                         40",
                  "Previously run:               0",
                  "Filtered out:                 1",
                  "-------------------------------",
                  "Total change sets:           41",
                  "FILTERED CHANGE SETS SUMMARY",
                  "Ignored:                      1"
                ]
        ]
    }

    run "Happy path with a change set that has labels", {
        arguments = [
                url          : { it.url },
                username     : { it.username },
                password     : { it.password },
                changelogFile: "changelogs/h2/complete/simple.changelog.labels.xml",
                labelFilter  : "first",
                showSummary  : "verbose"
        ]

        expectedResults = [
                statusCode: 0,
                defaultChangeExecListener: 'not_null',
                updateReport: 'not_null'
        ]

        outputFile = new File("target/test-classes/changeSetWithLabels.txt")

        expectedFileContent = [ "target/test-classes/changeSetWithLabels.txt":
                   [
                     """UPDATE SUMMARY
                     Run:                          1
                     Previously run:               0
                     Filtered out:                40
                     -------------------------------
                     Total change sets:           41
                     FILTERED CHANGE SETS SUMMARY
                     Label mismatch:              40
                     +--------------------------------------------------------------+-------------------------------+
                     | Changeset Info                                               | Reason Skipped                |
                     +--------------------------------------------------------------+-------------------------------+
                     | changelogs/h2/complete/simple.changelog.labels.xml::1.1::nvo | Labels does not match 'first' |
                     | xland                                                        |                               |
                     +--------------------------------------------------------------+-------------------------------+
                     | changelogs/h2/complete/simple.changelog.labels.xml::2::nvoxl | Labels does not match 'first' |
                     | and                                                          |                               |
                     +--------------------------------------------------------------+-------------------------------+
                     | changelogs/h2/complete/simple.changelog.labels.xml::3::nvoxl | Labels does not match 'first' |
                     | and                                                          |                               |
                     +--------------------------------------------------------------+-------------------------------+
                     | changelogs/h2/complete/simple.changelog.labels.xml::5::nvoxl | Labels does not match 'first' |
                     | and                                                          |                               |
                     +--------------------------------------------------------------+-------------------------------+"""
                     ]
        ]

    }

    run "Happy path with a change set that has labels and contexts", {
        arguments = [
                url          : { it.url },
                username     : { it.username },
                password     : { it.password },
                changelogFile: "changelogs/h2/complete/simple.changelog.labels.context.xml",
                labelFilter  : "first",
                contextFilter: "firstContext",
                showSummary  : "verbose"
        ]

        expectedResults = [
                statusCode: 0,
                defaultChangeExecListener: 'not_null',
                updateReport: 'not_null'
        ]

        outputFile = new File("target/test-classes/changeSetWithLabels.txt")

        expectedFileContent = [ "target/test-classes/changeSetWithLabels.txt":
           [
                """UPDATE SUMMARY
                Run:                          2
                Previously run:               0
                Filtered out:                 1
                -------------------------------
                Total change sets:            3
                FILTERED CHANGE SETS SUMMARY
                Context mismatch:             1
                Label mismatch:               1
                +--------------------------------------------------------------+------------------------------------------+
                | Changeset Info                                               | Reason Skipped                           |
                +--------------------------------------------------------------+------------------------------------------+
                | changelogs/h2/complete/simple.changelog.labels.context.xml:: | Context does not match 'firstcontext'    |
                | 2::nvoxland                                                  | Labels does not match 'first'            |
                +--------------------------------------------------------------+------------------------------------------+"""
           ]
        ]
    }

    run "Happy path with a change set that has complicated labels and contexts", {
        arguments = [
                url          : { it.url },
                username     : { it.username },
                password     : { it.password },
                changelogFile: "changelogs/h2/complete/summary-changelog.xml",
                labelFilter  : "testtable1",
                contextFilter: "none",
                showSummary  : "summary"
        ]

        expectedResults = [
                statusCode: 0,
                defaultChangeExecListener: 'not_null',
                updateReport: 'not_null'
        ]

        outputFile = new File("target/test-classes/changeSetWithComplicatedLabelsAndContext.txt")

        expectedFileContent = [ "target/test-classes/changeSetWithComplicatedLabelsAndContext.txt":
             [
               "UPDATE SUMMARY",
               "Run:                          2",
               "Previously run:               0",
               "Filtered out:                 4",
               "-------------------------------",
               "Total change sets:            6",
               "FILTERED CHANGE SETS SUMMARY",
               "Context mismatch:             2",
               "Label mismatch:               3",
               "DBMS mismatch:                1"
             ]
        ]
    }

    run "Happy path with a change set that has complicated labels and contexts with log output", {
        arguments = [
                url                 : { it.url },
                username            : { it.username },
                password            : { it.password },
                changelogFile       : "changelogs/h2/complete/summary-changelog.xml",
                labelFilter         : "testtable1",
                contextFilter       : "none",
                showSummary         : "summary",
                showSummaryOutput   : "log"
        ]

        expectedResults = [
                statusCode: 0,
                defaultChangeExecListener: 'not_null',
                updateReport: 'not_null'
        ]

        expectedLogs = [
                "UPDATE SUMMARY",
                "Run:                          2",
                "Previously run:               0",
                "Filtered out:                 4",
                "-------------------------------",
                "Total change sets:            6",
                "FILTERED CHANGE SETS SUMMARY",
                "Context mismatch:             2",
                "Label mismatch:               3",
                "DBMS mismatch:                1"
        ]
    }

    run "Happy path with skipped change sets propagated from an included changelog", {
        arguments = [
                url          : { it.url },
                username     : { it.username },
                password     : { it.password },
                changelogFile: "changelogs/h2/complete/summary.root.changelog.xml",
                labelFilter  : "testtable1",
                contextFilter: "none",
                showSummary  : "summary"
        ]

        expectedResults = [
                statusCode: 0,
                defaultChangeExecListener: 'not_null',
                updateReport: 'not_null'
        ]

        outputFile = new File("target/test-classes/changeSetSkippedPropagatedToRoot.txt")

        expectedFileContent = [ "target/test-classes/changeSetSkippedPropagatedToRoot.txt":
             [
               "UPDATE SUMMARY",
               "Run:                          2",
               "Previously run:               0",
               "Filtered out:                 4",
               "-------------------------------",
               "Total change sets:            6",
               "FILTERED CHANGE SETS SUMMARY",
               "Context mismatch:             2",
               "Label mismatch:               3",
               "DBMS mismatch:                1"
             ]
        ]
    }

    run "Mismatched DBMS causes not deployed summary message", {
        arguments = [
                url          : { it.url },
                username     : { it.username },
                password     : { it.password },
                showSummary  : "verbose",
                changelogFile: "changelogs/h2/complete/mismatchedDbms.changelog.xml",
        ]

        expectedResults = [
                statusCode: 0,
                defaultChangeExecListener: 'not_null',
                updateReport: 'not_null'
        ]

        outputFile = new File("target/test-classes/mismatchedDBMS.txt")

        expectedFileContent = [ "target/test-classes/mismatchedDBMS.txt":
            [
              """UPDATE SUMMARY
              Run:                          2
              Previously run:               0
              Filtered out:                 1
              -------------------------------
              Total change sets:            3
              FILTERED CHANGE SETS SUMMARY
              DBMS mismatch:                1
              +--------------------------------------------------------------+--------------------------------+
              | Changeset Info                                               | Reason Skipped                 |
              +--------------------------------------------------------------+--------------------------------+
              | changelogs/h2/complete/mismatchedDbms.changelog.xml::1::nvox | mismatched DBMS value of 'foo' |
              | land                                                         |                                |
              +--------------------------------------------------------------+--------------------------------+"""
           ]
        ]
    }

    run "Happy path with a simple changelog log message does not contain 'Executing with' message", {
        arguments = [
                url          : { it.url },
                username     : { it.username },
                password     : { it.password },
                changelogFile: "changelogs/h2/complete/simple.changelog.xml"
        ]

        expectedResults = [
                statusCode: 0,
                defaultChangeExecListener: 'not_null',
                updateReport: 'not_null'
        ]

        expectedDatabaseContent = [
                "txt": [Pattern.compile(".*liquibase.structure.core.Table:.*ADDRESS.*", Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
                        Pattern.compile(".*liquibase.structure.core.Table:.*ADDRESS.*columns:.*city.*", Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE)]
        ]
        expectedLogs = [
                CommandTests.assertNotContains("Executing with 'jdbc' executor")
        ]
    }

    run "Run without a URL throws an exception", {
        arguments = [
                url: ""
        ]
        expectedException = CommandValidationException.class
    }

    run "Run with a URL that has credentials", {
        arguments = [
                url          : { it.url + "?user=sa&password=\"\"" },
                changelogFile: "changelogs/h2/complete/simple.changelog.xml"
        ]
        expectedException = CommandExecutionException.class
        expectedExceptionMessage = Pattern.compile(".*Connection could not be created to jdbc:h2:mem:lbcat;DB_CLOSE_DELAY=-1\\?user=.*")
    }

    run "Run without a changeLogFile throws an exception", {
        arguments = [
                changelogFile: ""
        ]
        expectedException = CommandValidationException.class
    }

    run "Run without any argument throws an exception", {
        arguments = [
                url          : "",
                changelogFile: ""
        ]
        expectedException = CommandValidationException.class
    }

    run "Run with a bad show summary option throws an exception", {
        arguments = [
                url          : { it.url },
                username     : { it.username },
                password     : { it.password },
                changelogFile: 'changelogs/h2/complete/simple.changelog.xml',
                showSummary  : "foo"
        ]
        expectedException = IllegalArgumentException.class
    }

    run "Run with a bad global flag value throws an exception", {
        arguments = [
                url          : { it.url },
                username     : { it.username },
                password     : { it.password },
                changelogFile: "changelogs/h2/complete/simple.changelog.xml"
        ]
        globalArguments = ["liquibase.preserveSchemaCase": "foo"]

        expectedException = CommandExecutionException.class
        expectedExceptionMessage = Pattern.compile(".*WARNING:  The input for 'liquibase.preserveSchemaCase' is 'foo', which is not valid.  Options: 'true' or 'false'.*")
    }

    run "Should use LoggingChangeExecListener", {
        arguments = [
                url                    : { it.url },
                username               : { it.username },
                password               : { it.password },
                changelogFile          : 'changelogs/h2/complete/simple.changelog.xml',
                changeExecListenerClass: 'liquibase.changelog.visitor.LoggingChangeExecListener',
        ]

        expectedResults = [
                statusCode: 0,
                defaultChangeExecListener: 'not_null',
                updateReport: 'not_null'
        ]

        expectedLogs = [
                'EVENT: willRun fired',
                'EVENT: ran fired',
        ]
    }

    run "Should fall back to jdbc executor when runWith is an empty string", {
        arguments = [
                url                    : { it.url },
                username               : { it.username },
                password               : { it.password },
                changelogFile          : 'changelogs/h2/complete/empty.runWith.changelog.xml',
        ]

        expectedResults = [
                statusCode: 0,
                defaultChangeExecListener: 'not_null',
                updateReport: 'not_null'
        ]
    }

    run "Deployment fails on first changeset", {
        arguments = [
                url          : { it.url },
                username     : { it.username },
                password     : { it.password },
                changelogFile: "changelogs/common/invalid.sql.changelog.xml",
                showSummary: "VERBOSE"
        ]

        outputFile = new File("target/test-classes/happyPath.txt")

        expectedFileContent = [ "target/test-classes/happyPath.txt":
                                        [
                                                """
UPDATE SUMMARY
Run:                          0
Previously run:               0
Filtered out:                 0
-------------------------------
Total change sets:            1
"""
                                        ]
        ]
        expectedException = CommandExecutionException
    }

    run "Ignore on includeAll", {
        arguments = [
                url          : { it.url },
                username     : { it.username },
                password     : { it.password },
                changelogFile: "changelogs/common/includerelative/pathinclude1.ignored.changelog.xml",
                showSummary: "VERBOSE"
        ]

        expectedOutput = """
UPDATE SUMMARY
Run:                          1
Previously run:               0
Filtered out:                 1
-------------------------------
Total change sets:            2


FILTERED CHANGE SETS SUMMARY
Ignored:                      1

+--------------------------------------------------------------+----------------------+
| Changeset Info                                               | Reason Skipped       |
+--------------------------------------------------------------+----------------------+
| changelogs/common/includerelative/includeAll/pathinclude2.ch | Changeset is ignored |
| angelog.xml::1::nvoxland                                     |                      |
+--------------------------------------------------------------+----------------------+
"""
    }
}
