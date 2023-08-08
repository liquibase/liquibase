package liquibase.extension.testing.command

import liquibase.Scope
import liquibase.changelog.ChangeLogHistoryServiceFactory
import liquibase.changelog.RanChangeSet
import liquibase.database.Database
import liquibase.exception.CommandValidationException
import liquibase.extension.testing.setup.HistoryEntry

import static org.junit.Assert.assertEquals

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

        expectations = {
            def database = (Database) Scope.getCurrentScope().get("database", null)
            def changelogHistoryService = Scope.getCurrentScope().getSingleton(ChangeLogHistoryServiceFactory.class).getChangeLogService(database)
            List<RanChangeSet> ranChangeSets = changelogHistoryService.getRanChangeSets()
            for (RanChangeSet ranChangeSet : ranChangeSets) {
                assertEquals(ranChangeSet.getLastCheckSum(), null)
            }
        }

    }

    run "Run without a URL should throw an exception", {
        arguments = [
                url:  ""
        ]

        expectedException = CommandValidationException.class
    }
}
