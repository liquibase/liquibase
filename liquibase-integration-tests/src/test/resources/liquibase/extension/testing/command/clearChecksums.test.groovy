package liquibase.extension.testing.command

import liquibase.exception.CommandValidationException
import liquibase.extension.testing.setup.HistoryEntry

CommandTests.define {
    command = ["clearChecksums"]

    signature = """
Short Description: Clears all checksums
Long Description: Clears all checksums and nullifies the MD5SUM column of the DATABASECHANGELOG table so that they will be re-computed on the next database update
Required Args:
  url (String) The JDBC database connection URL
    OBFUSCATED
Optional Args:
  defaultCatalogName (String) The default catalog name to use for the database connection
    Default: null
  defaultSchemaName (String) The default schema name to use for the database connection
    Default: null
  driver (String) The JDBC driver class
    Default: null
  driverPropertiesFile (String) The JDBC driver properties file
    Default: null
  password (String) The database password
    Default: null
    OBFUSCATED
  username (String) The database username
    Default: null
"""

    run "Happy path", {
        arguments = [
            url:        { it.url },
            username:   { it.username },
            password:   { it.password }
        ]
        setup {
            history = [
                    new HistoryEntry(
                            id: "1",
                            author: "test",
                            path: "com/example/changelog.xml"
                    ),
                    new HistoryEntry(
                            id: "2",
                            author: "test",
                            path: "com/example/changelog.xml"
                    ),
            ]
        }

        expectedResults = [
                statusCode   : 0
        ]

    }

    run "Run without a URL should throw an exception", {
        arguments = [
                url:  ""
        ]

        expectedException = CommandValidationException.class
    }
}
