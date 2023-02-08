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
  showSummary (UpdateSummaryEnum) Type of update results summary to show.  Values can be 'off', 'summary', or 'verbose'.
    Default: SUMMARY
  username (String) Username to use to connect to the database
    Default: null
"""

    run "Happy path with a simple changelog", {
        arguments = [
                url:        { it.url },
                username:   { it.username },
                password:   { it.password },
                changelogFile: "changelogs/h2/complete/simple.changelog.xml"
        ]

        expectedResults = [
                statusCode   : 0
        ]

        expectedDatabaseContent = [
                "txt": [Pattern.compile(".*liquibase.structure.core.Table:.*ADDRESS.*", Pattern.MULTILINE|Pattern.DOTALL|Pattern.CASE_INSENSITIVE),
                        Pattern.compile(".*liquibase.structure.core.Table:.*ADDRESS.*columns:.*city.*", Pattern.MULTILINE|Pattern.DOTALL|Pattern.CASE_INSENSITIVE)]
        ]
        expectedUI = [
"""
UPDATE SUMMARY
Run:                         41
Previously run:               0
DBMS mismatch:                0
Not in filter:                0
-------------------------------
Total change sets:           41
"""
]

    }

    run "Mismatched DBMS causes not deployed summary message", {
        arguments = [
                url:        { it.url },
                username:   { it.username },
                password:   { it.password },
                showSummary: "verbose",
                changelogFile: "changelogs/h2/complete/mismatchedDbms.changelog.xml"
        ]

        expectedResults = [
                statusCode   : 0
        ]

        expectedUI = [
"""
UPDATE SUMMARY
Run:                          2
Previously run:               0
DBMS mismatch:                1
Not in filter:                0
-------------------------------
Total change sets:            3

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

    run "Run without a URL throws an exception", {
        arguments = [
                url: ""
        ]
        expectedException = CommandValidationException.class
    }

    run "Run with a URL that has credentials", {
        arguments = [
                url:        { it.url + "?user=sa&password=\"\"" },
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
                url: "",
                changelogFile: ""
        ]
        expectedException = CommandValidationException.class
    }

    run "Run with a bad show summary option throws an exception", {
        arguments = [
                url                    : { it.url },
                username               : { it.username },
                password               : { it.password },
                changelogFile          : 'changelogs/h2/complete/simple.changelog.xml',
                showSummary: "foo"
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
