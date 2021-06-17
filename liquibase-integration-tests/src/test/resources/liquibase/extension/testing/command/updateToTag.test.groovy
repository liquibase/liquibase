package liquibase.extension.testing.command

import liquibase.exception.CommandValidationException

CommandTests.define {
    command = ["updateToTag"]
    signature = """
Short Description: Deploy changes from the changelog file to the specified tag
Long Description: NOT SET
Required Args:
  changelogFile (String) The root changelog
  tag (String) The tag to update to
  url (String) The JDBC database connection URL
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
  labels (String) Changeset labels to match
    Default: null
  password (String) Password to use to connect to the database
    Default: null
    OBFUSCATED
  username (String) Username to use to connect to the database
    Default: null
"""

    run "Happy path", {
        arguments = [
                url:        { it.url },
                username:   { it.username },
                password:   { it.password },
                tag          : "version_2.0",
                changelogFile: "changelogs/hsqldb/complete/simple.tag.changelog.xml",
        ]


        expectedResults = [
                statusCode   : 0
        ]
    }

    run "Run without a tag throws an exception", {
        arguments = [
                url          : "",
                changelogFile: "changelogs/hsqldb/complete/simple.tag.changelog.xml",
        ]
        expectedException = CommandValidationException.class
    }

    run "Run without a changeLogFile throws an exception", {
        arguments = [
                url          : "",
                tag          : "version_2.0",
        ]
        expectedException = CommandValidationException.class
    }

    run "Run without URL throws an exception", {
        arguments = [
                url          : "",
                tag          : "version_2.0",
                changelogFile: "changelogs/hsqldb/complete/simple.tag.changelog.xml",
        ]
        expectedException = CommandValidationException.class
    }

    run "Run without any arguments throws an exception", {
        arguments = [
                url          : "",
        ]
        expectedException = CommandValidationException.class
    }
}
