package liquibase.extension.testing.command

CommandTests.define {
    command = ["executeSql"]
    signature = """
Short Description: Execute a SQL string or file
Long Description: NOT SET
Required Args:
  url (String) The JDBC database connection URL
    OBFUSCATED
Optional Args:
  defaultCatalogName (String) The default catalog name to use for the database connection
    Default: null
  defaultSchemaName (String) The default schema name to use for the database connection
    Default: null
  delimiter (String) Delimiter to use when executing SQL script
    Default: null
  driver (String) The JDBC driver class
    Default: null
  driverPropertiesFile (String) The JDBC driver properties file
    Default: null
  password (String) Password to use to connect to the database
    Default: null
    OBFUSCATED
  sql (String) SQL string to execute
    Default: null
  sqlFile (String) SQL script to execute
    Default: null
  username (String) Username to use to connect to the database
    Default: null
"""

    run "Can use execute-sql to select", {
        arguments = [
                url     : { it.url },
                username: { it.username },
                password: { it.password },
                sql     : 'select * from databasechangelog'
        ]

        setup {
            runChangelog "changelogs/h2/complete/example.changelog.sql"
        }
        expectedOutput = [
                'Output of select * from databasechangelog:',
                'ID | AUTHOR | FILENAME | DATEEXECUTED | ORDEREXECUTED | EXECTYPE | MD5SUM | DESCRIPTION | COMMENTS | TAG | LIQUIBASE | CONTEXTS | LABELS | DEPLOYMENT_ID |'
        ]
    }

    run "Can use execute-sql to insert", {
        arguments = [
                url     : { it.url },
                username: { it.username },
                password: { it.password },
                sql     : "insert into person values (1, 'Wes Anderson', '1 Way', '2 Go', 'Budapest', 'US')"
        ]

        setup {
            runChangelog "changelogs/h2/complete/example.changelog.sql"
        }
        expectedOutput = [
                "Successfully Executed: insert into person values (1, 'Wes Anderson', '1 Way', '2 Go', 'Budapest', 'US')"
        ]
    }
}
