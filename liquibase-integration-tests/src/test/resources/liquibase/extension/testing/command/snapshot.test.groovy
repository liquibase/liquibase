package liquibase.extension.testing.command

import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.change.core.TagDatabaseChange
import liquibase.exception.CommandValidationException

CommandTests.define {
    command = ["snapshot"]
    signature = """
Short Description: Capture the current state of the database
Long Description: NOT SET
Required Args:
  url (String) The JDBC database connection URL
Optional Args:
  defaultCatalogName (String) The default catalog name to use for the database connection
    Default: null
  defaultSchemaName (String) The default schema name to use for the database connection
    Default: null
  password (String) Password to use to connect to the database
    Default: null
  snapshotFormat (String) Output format to use (JSON or YAML
    Default: null
  username (String) Username to use to connect to the database
    Default: null
"""

    run "Happy path", {
        setup {
            database = [
                    new CreateTableChange(
                            tableName: "FirstTable",
                            columns: [
                                    ColumnConfig.fromName("FirstColumn")
                                            .setType("VARCHAR(255)")
                            ]
                    ),
                    new CreateTableChange(
                            tableName: "SecondTable",
                            columns: [
                                    ColumnConfig.fromName("SecondColumn")
                                            .setType("VARCHAR(255)")
                            ]
                    ),
                    new TagDatabaseChange(
                            tag: "version_2.0"
                    ),
                    new CreateTableChange(
                            tableName: "liquibaseRunInfo",
                            columns: [
                                    ColumnConfig.fromName("timesRan")
                                            .setType("INT")
                            ]
                    ),
            ]
        }

        expectedResults = [
                statusCode   : 0
        ]
    }

    run "Happy path with an output file", {
        setup {
            cleanResources("target/test-classes/snapshot.txt")
            database = [
                    new CreateTableChange(
                            tableName: "FirstTable",
                            columns: [
                                    ColumnConfig.fromName("FirstColumn")
                                            .setType("VARCHAR(255)")
                            ]
                    ),
                    new CreateTableChange(
                            tableName: "SecondTable",
                            columns: [
                                    ColumnConfig.fromName("SecondColumn")
                                            .setType("VARCHAR(255)")
                            ]
                    ),
                    new TagDatabaseChange(
                            tag: "version_2.0"
                    ),
                    new CreateTableChange(
                            tableName: "liquibaseRunInfo",
                            columns: [
                                    ColumnConfig.fromName("timesRan")
                                            .setType("INT")
                            ]
                    ),
            ]
        }

        outputFile = new File("target/test-classes/snapshot.txt")

        expectedFileContent = [
                //
                // Find the " -- Release Database Lock" line
                //
                "target/test-classes/snapshot.txt" : [CommandTests.assertContains("Database snapshot for")]
        ]

        expectedResults = [
                statusCode   : 0
        ]
    }

    run "Run without any arguments should throw an exception",  {
        arguments = [
                url: ""
        ]
        expectedException = CommandValidationException.class
    }
}