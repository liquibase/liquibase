package liquibase.extension.testing.command


import liquibase.exception.CommandValidationException
import liquibase.util.TestUtil

CommandTests.define {
    command = ["updateTestingRollback"]
    signature = """
Short Description: Updates database, then rolls back changes before updating again. Useful for testing rollback support
Long Description: NOT SET
Required Args:
  changelogFile (String) The root changelog file
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
  username (String) Username to use to connect to the database
    Default: null
"""

    run "Happy path with a simple changelog", {
        arguments = [
                url:        { it.url },
                username:   { it.username },
                password:   { it.password },
                changelogFile: "changelogs/h2/complete/rollback.changelog.xml"
        ]

        setup {
            runChangelog "changelogs/h2/complete/rollback.changelog.xml"
            rollback 5, "changelogs/h2/complete/rollback.changelog.xml"
        }

        expectations = {
            TestUtil.assertAllDeploymentIdsNonNull()
        }
    }

    run "Run without a URL throws an exception", {
        arguments = [
                url: ""
        ]
        expectedException = CommandValidationException.class
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
}
