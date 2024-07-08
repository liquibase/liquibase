package liquibase.extension.testing.command

import liquibase.exception.CommandValidationException

import java.util.regex.Pattern

CommandTests.define {
    command = ["rollbackCount"]
    signature = """
Short Description: Rollback the specified number of changes made to the database
Long Description: NOT SET
Required Args:
  changelogFile (String) The root changelog file
  count (Integer) The number of changes to rollback
  url (String) The JDBC database connection URL
    OBFUSCATED
Optional Args:
  changeExecListenerClass (String) Fully-qualified class which specifies a ChangeExecListener
    Default: null
  changeExecListenerPropertiesFile (String) Path to a properties file for the ChangeExecListenerClass
    Default: null
  contextFilter (String) Context string to use for filtering
    Default: null
  defaultCatalogName (String) The default catalog name to use for the database connection
    Default: null
  defaultSchemaName (String) The default schema name to use for the database connection
    Default: null
  driver (String) The JDBC driver class
    Default: null
  driverPropertiesFile (String) The JDBC driver properties file
    Default: null
  labelFilter (String) Label expression to use for filtering
    Default: null
  password (String) Password to use to connect to the database
    Default: null
    OBFUSCATED
  rollbackScript (String) Rollback script to execute
    Default: null
  username (String) Username to use to connect to the database
    Default: null
"""

    run "Happy path", {
        arguments = [
                url          : { it.url },
                username     : { it.username },
                password     : { it.password },
                count        : 1,
                changelogFile: "changelogs/h2/complete/rollback.changelog.xml"
        ]

        setup {
            runChangelog "changelogs/h2/complete/rollback.changelog.xml"
        }

        expectedDatabaseContent = [
                "txt": [Pattern.compile(".*liquibase.structure.core.Table.*FIRSTTABLE.*", Pattern.MULTILINE | Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
                        CommandTests.assertNotContains(".*liquibase.structure.core.Table.*SECONDTABLE.*", true)]
        ]

    }

    run "Run without any arguments should throw an exception", {
        arguments = [
                url: ""
        ]

        expectedException = CommandValidationException.class
    }

    run "Run without a count should throw an exception", {
        arguments = [
                changelogFile: "changelogs/h2/complete/rollback.tag.changelog.xml",
        ]
        expectedException = CommandValidationException.class
    }

    run "Run without a changeLogFile should throw an exception", {
        arguments = [
                count: 1
        ]
        expectedException = CommandValidationException.class
    }

    run "Run without a URL should throw an exception", {
        arguments = [
                url          : "",
                changelogFile: "changelogs/h2/complete/rollback.tag.changelog.xml",
                count        : 1
        ]
        expectedException = CommandValidationException.class
    }

    run "Should log message when no changesets are rolled back", {
        arguments = [
                url          : { it.url },
                username     : { it.username },
                password     : { it.password },
                count        : 100,
                changelogFile: "changelogs/h2/rollback/empty.rollback.changelog.sql"
        ]

        setup {
            runChangelog "changelogs/h2/complete/rollback.changelog.xml"
        }

        expectedUI = [
                CommandTests.assertContains("INFO: 0 changesets rolled back.")
        ]
    }
}
