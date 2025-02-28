package liquibase.extension.testing.command

import liquibase.exception.CommandValidationException

CommandTests.define {
    command = ["rollbackSql"]
    signature = """
Short Description: Generate the SQL to rollback changes made to the database based on the specific tag
Long Description: NOT SET
Required Args:
  changelogFile (String) The root changelog file
  tag (String) Tag to rollback to
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
  outputDefaultCatalog (Boolean) Control whether names of objects in the default catalog are fully qualified or not. If true they are. If false, only objects outside the default catalog are fully qualified
    Default: true
  outputDefaultSchema (Boolean) Control whether names of objects in the default schema are fully qualified or not. If true they are. If false, only objects outside the default schema are fully qualified
    Default: true
  password (String) Password to use to connect to the database
    Default: null
    OBFUSCATED
  rollbackScript (String) Rollback script to execute
    Default: null
  tagVersion (String) Tag version to use for multiple occurrences of a tag
    Default: OLDEST
  username (String) Username to use to connect to the database
    Default: null
"""

    run "Happy path", {
        arguments = [
                url          : { it.url },
                username     : { it.username },
                password     : { it.password },
                tag          : "version_2.0",
                tagVersion   : "OLDEST",
                changelogFile: "changelogs/h2/complete/rollback.tag.changelog.xml",
        ]

        setup {
            runChangelog "changelogs/h2/complete/rollback.tag.changelog.xml"
        }

    }

    run "Happy path with an output file", {
        arguments = [
                url          : { it.url },
                username     : { it.username },
                password     : { it.password },
                tag          : "version_2.0",
                changelogFile: "changelogs/h2/complete/rollback.tag.changelog.xml",
        ]

        setup {
            cleanResources("target/test-classes/rollback.sql")
            runChangelog "changelogs/h2/complete/rollback.tag.changelog.xml"
        }

        outputFile = new File("target/test-classes/rollback.sql")

        expectedFileContent = [
                //
                // Find the " -- Release Database Lock" line
                //
                "target/test-classes/rollback.sql" : [CommandTests.assertContains("-- Release Database Lock")]
        ]
    }

    run "Run without any arguments should throw an exception",  {
        arguments = [
                url:  ""
        ]

        expectedException = CommandValidationException.class
    }

    run "Run without a tag should throw an exception",  {
        arguments = [
                changelogFile: "changelogs/h2/complete/rollback.tag.changelog.xml",
                tag          : ""
        ]
        expectedException = CommandValidationException.class
    }

    run "Run without a changeLogFile should throw an exception",  {
        arguments = [
                tag          : "version_2.0"
        ]
        expectedException = CommandValidationException.class
    }

    run "Run without a URL should throw an exception",  {
        arguments = [
                url          : "",
                changelogFile: "changelogs/h2/complete/rollback.tag.changelog.xml",
                tag          : "version_2.0"
        ]
        expectedException = CommandValidationException.class
    }
}
