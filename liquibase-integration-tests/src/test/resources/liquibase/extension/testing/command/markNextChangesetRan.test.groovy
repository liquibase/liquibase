package liquibase.extension.testing.command

import liquibase.exception.CommandValidationException
import liquibase.util.TestUtil

CommandTests.define {
    command = ["markNextChangesetRan"]
    signature = """
Short Description: Marks the next change you apply as executed in your database
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
"""

    run "Happy path", {
        arguments = [
                url      : { it.url },
                username : { it.username },
                password : { it.password },
                changelogFile: "changelogs/h2/complete/simple.changelog.xml"
        ]

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
}
