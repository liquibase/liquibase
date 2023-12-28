package liquibase.extension.testing.command

import liquibase.exception.CommandValidationException

CommandTests.define {
    command = ["status"]
    signature = """
Short Description: Generate a list of pending changesets
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
  verbose (Boolean) Verbose flag with optional values of 'True' or 'False'. The default is 'True'.
    Default: true
"""

    run "Happy path", {
        arguments = [
                url          : { it.url },
                username     : { it.username },
                password     : { it.password },
                verbose      : "true",
                changelogFile: "changelogs/h2/complete/rollback.tag.plus.changelog.xml"
        ]

        setup {
            runChangelog "changelogs/h2/complete/rollback.tag.plus.changelog.xml", "init"
        }

        expectedOutput = """
1 changeset has not been applied to LBUSER@jdbc:h2:mem:lbcat
     changelogs/h2/complete/rollback.tag.plus.changelog.xml::1.111::nvoxland
"""
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
