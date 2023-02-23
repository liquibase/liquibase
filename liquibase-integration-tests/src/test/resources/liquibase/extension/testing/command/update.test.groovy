package liquibase.extension.testing.command

import liquibase.exception.CommandExecutionException
import liquibase.exception.CommandValidationException

import java.util.regex.Pattern

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
  contexts (String) Changeset contexts to match
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
                statusCode: 0
        ]

        expectedDatabaseContent = [
                "txt": [Pattern.compile(".*liquibase.structure.core.Table:.*ADDRESS.*", Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
                        Pattern.compile(".*liquibase.structure.core.Table:.*ADDRESS.*columns:.*city.*", Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE)]
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
                statusCode: 0
        ]

        expectedUI = [
                """
UPDATE SUMMARY
Run:                         40
Previously run:               0
Filtered out:                 1
-------------------------------
Total change sets:           41

FILTERED CHANGE SETS SUMMARY

Ignored:                      1
"""
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
                statusCode: 0
        ]

        expectedUI = [
                """
UPDATE SUMMARY
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
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::1.1::nvo |                               |
| xland                                                        |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::2::nvoxl |                               |
| and                                                          |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::3::nvoxl |                               |
| and                                                          |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::5::nvoxl |                               |
| and                                                          |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::6::nvoxl |                               |
| and                                                          |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::7::nvoxl |                               |
| and                                                          |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::7::bjohn |                               |
| son                                                          |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::7a::nvox |                               |
| land                                                         |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::8::bjohn |                               |
| son                                                          |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::9::nvoxl |                               |
| and                                                          |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::10::nvox |                               |
| land                                                         |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::11::nvox |                               |
| land                                                         |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::12::nvox |                               |
| land                                                         |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::13::nvox |                               |
| land                                                         |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::14::nvox |                               |
| land                                                         |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::15::nvox |                               |
| land                                                         |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::16::nvox |                               |
| land                                                         |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::16a::nvo |                               |
| xland                                                        |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::17::nvox |                               |
| land                                                         |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::18a::nvo |                               |
| xland                                                        |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::18::nvox |                               |
| land                                                         |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::19::nvox |                               |
| land                                                         |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::20::nvox |                               |
| land                                                         |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::22::nvox |                               |
| land                                                         |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::23::nvox |                               |
| land                                                         |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::24::nvox |                               |
| land                                                         |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::25::nvox |                               |
| land                                                         |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::25.1::nv |                               |
| oxland                                                       |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::25.2::nv |                               |
| oxland                                                       |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::25.3::nv |                               |
| oxland                                                       |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::26::nvox |                               |
| land                                                         |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::28::nvox |                               |
| land                                                         |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::29::nvox |                               |
| land                                                         |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::30::nvox |                               |
| land                                                         |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::31::nvox |                               |
| land                                                         |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::32::nvox |                               |
| land                                                         |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::33::nvox |                               |
| land                                                         |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::34::nvox |                               |
| land                                                         |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::35::nvox |                               |
| land                                                         |                               |
+--------------------------------------------------------------+-------------------------------+
|                                                              | Labels does not match 'first' |
| changelogs/h2/complete/simple.changelog.labels.xml::50::nvox |                               |
| land                                                         |                               |
+--------------------------------------------------------------+-------------------------------+
"""
        ]

    }

    run "Happy path with a change set that has labels and contexts", {
        arguments = [
                url          : { it.url },
                username     : { it.username },
                password     : { it.password },
                changelogFile: "changelogs/h2/complete/simple.changelog.labels.context.xml",
                labelFilter  : "first",
                contexts     : "firstContext",
                showSummary  : "verbose"
        ]

        expectedResults = [
                statusCode: 0
        ]

        expectedUI = [
                """
UPDATE SUMMARY
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
|                                                              | Context does not match 'firstcontext'    |
| changelogs/h2/complete/simple.changelog.labels.context.xml:: | Labels does not match 'first'            |
| 2::nvoxland                                                  |                                          |
+--------------------------------------------------------------+------------------------------------------+
"""
        ]

    }

    run "Happy path with a change set that has complicated labels and contexts", {
        arguments = [
                url          : { it.url },
                username     : { it.username },
                password     : { it.password },
                changelogFile: "changelogs/h2/complete/summary-changelog.xml",
                labelFilter  : "testtable1",
                contexts     : "none",
                showSummary  : "summary"
        ]

        expectedResults = [
                statusCode: 0
        ]

        expectedUI = [
                """
UPDATE SUMMARY
Run:                          2
Previously run:               0
Filtered out:                 4
-------------------------------
Total change sets:            6

FILTERED CHANGE SETS SUMMARY

Context mismatch:             2
Label mismatch:               3
DBMS mismatch:                1
"""
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
                statusCode: 0
        ]

        expectedUI = [
                """
UPDATE SUMMARY
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
|                                                              | mismatched DBMS value of 'foo' |
| changelogs/h2/complete/mismatchedDbms.changelog.xml::1::nvox |                                |
| land                                                         |                                |
+--------------------------------------------------------------+--------------------------------+
"""
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
                statusCode: 0
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

    run "Should use LoggingChangeExecListener", {
        arguments = [
                url                    : { it.url },
                username               : { it.username },
                password               : { it.password },
                changelogFile          : 'changelogs/h2/complete/simple.changelog.xml',
                changeExecListenerClass: 'liquibase.changelog.visitor.LoggingChangeExecListener',
        ]

        expectedResults = [
                statusCode: 0
        ]

        expectedLogs = [
                'EVENT: willRun fired',
                'EVENT: ran fired',
        ]
    }
}
