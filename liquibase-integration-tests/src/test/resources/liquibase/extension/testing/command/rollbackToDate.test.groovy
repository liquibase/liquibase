package liquibase.extension.testing.command

import liquibase.exception.CommandValidationException

CommandTests.define {
    command = ["rollbackToDate"]
    signature = """
Short Description: Rollback changes made to the database based on the specific date
Long Description: NOT SET
Required Args:
  changelogFile (String) The root changelog file
  date (Date) Date to rollback changes to
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
                url      : { it.url },
                username : { it.username },
                password : { it.password },
                date         : "2021-03-25T09:00:00",
                changelogFile: "changelogs/h2/complete/rollback.changelog.xml",
        ]

        setup {
            runChangelog "changelogs/h2/complete/rollback.changelog.xml"
        }

    }

    run "Run without any arguments should throw an exception",  {
        arguments = [
                url:  ""
        ]

        expectedException = CommandValidationException.class
    }

    run "Run without a date should throw an exception",  {
        arguments = [
                changelogFile: "changelogs/h2/complete/rollback.tag.changelog.xml",
        ]
        expectedException = CommandValidationException.class
    }

    run "Run without a changeLogFile should throw an exception",  {
        arguments = [
                date         : "2021-03-25T09:00:00",
        ]
        expectedException = CommandValidationException.class
    }

    run "Run without a URL should throw an exception",  {
        arguments = [
                url          : "",
                changelogFile: "changelogs/h2/complete/rollback.tag.changelog.xml",
                date         : "2021-03-25T09:00:00"
        ]
        expectedException = CommandValidationException.class
    }
}
