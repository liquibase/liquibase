package liquibase.extension.testing.command

import liquibase.exception.CommandValidationException

CommandTests.define {
    command = ["updateToTagSql"]
    signature = """
Short Description: Generate the SQL to deploy changes up to the tag
Long Description: NOT SET
Required Args:
  changelogFile (String) The root changelog
  tag (String) The tag to genenerate SQL up to
  url (String) The JDBC database connection URL
Optional Args:
  contexts (String) Changeset contexts to match
    Default: null
  defaultCatalogName (String) The default catalog name to use for the database connection
    Default: null
  defaultSchemaName (String) The default schema name to use for the database connection
    Default: null
  labels (String) Changeset labels to match
    Default: null
  password (String) Password to use to connect to the database
    Default: null
  username (String) Username to use to connect to the database
    Default: null
"""
    run "Happy path", {
        arguments = [
                tag          : "version_2.0",
                changelogFile: "changelogs/hsqldb/complete/simple.tag.changelog.xml",
        ]

        expectedResults = [
                statusCode   : 0
        ]
    }

    run "Happy path with output file", {
        arguments = [
                tag          : "version_2.0",
                changelogFile: "changelogs/hsqldb/complete/simple.changelog.xml"
        ]

        setup {
            cleanResources("target/test-classes/updateToTag.sql")
        }

        outputFile = new File("target/test-classes/updateToTag.sql")

        expectedFileContent = [
                //
                // Find the " -- Release Database Lock" line
                //
                "target/test-classes/updateToTag.sql" : [CommandTests.assertContains("-- Release Database Lock")]
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