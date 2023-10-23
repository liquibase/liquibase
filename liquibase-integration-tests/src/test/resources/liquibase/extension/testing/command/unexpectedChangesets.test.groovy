package liquibase.extension.testing.command

import liquibase.exception.CommandValidationException

CommandTests.define {
    command = ["unexpectedChangesets"]
    signature = """
Short Description: Generate a list of changesets that have been executed but are not in the current changelog
Long Description: NOT SET
Required Args:
  changelogFile (String) The root changelog file
  url (String) The JDBC database connection URL
    OBFUSCATED
Optional Args:
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
  verbose (Boolean) Verbose flag
    Default: false
"""

    run "Happy path", {
        arguments = [
                url:        { it.url },
                username:   { it.username },
                password:   { it.password },
                verbose      : "true",
                changelogFile: "changelogs/h2/complete/unexpected.tag.changelog.xml",
        ]

        setup {
            syncChangelog "changelogs/h2/complete/rollback.tag.changelog.xml"
        }

        expectedOutput = """
5 unexpected changes were found in LBUSER@jdbc:h2:mem:lbcat
     changelogs/h2/complete/rollback.tag.changelog.xml::1::nvoxland
     changelogs/h2/complete/rollback.tag.changelog.xml::1.1::nvoxland
     changelogs/h2/complete/rollback.tag.changelog.xml::2::nvoxland
     changelogs/h2/complete/rollback.tag.changelog.xml::13.1::testuser
     changelogs/h2/complete/rollback.tag.changelog.xml::14::nvoxland
"""
    }

    run "Verbose false", {
        arguments = [
                url:        { it.url },
                username:   { it.username },
                password:   { it.password },
                verbose      : "false",
                changelogFile: "changelogs/h2/complete/unexpected.tag.changelog.xml",
        ]

        setup {
            syncChangelog "changelogs/h2/complete/rollback.tag.changelog.xml"
        }

        expectedOutput = [
                CommandTests.assertContains("5 unexpected changes were found in LBUSER@jdbc:h2:mem:lbcat"),
                CommandTests.assertNotContains("changelogs/h2/complete/rollback.tag.changelog.xml::1::nvoxland"),
        ]
    }

    run "Run without a URL should throw an exception",  {
        arguments = [
                url          : "",
                verbose      : "true"
        ]
        expectedException = CommandValidationException.class
    }

    run "Run without a changeLogFile should throw an exception",  {
        arguments = [
                verbose      : "true"
        ]
        expectedException = CommandValidationException.class
    }

    run "Run without any arguments should throw an exception",  {
        arguments = [
                url: "",
        ]
        expectedException = CommandValidationException.class
    }
}
