package liquibase.extension.testing.command

import liquibase.exception.CommandValidationException

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
  password (String) Password to use to connect to the database
    Default: null
    OBFUSCATED
  username (String) Username to use to connect to the database
    Default: null
"""

    run "Run without a URL should throw an exception", {
        arguments = [
                url:  ""
        ]

        expectedException = CommandValidationException.class
    }
}
