package liquibase.extension.testing.command

import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.change.core.TagDatabaseChange
import liquibase.exception.CommandValidationException

CommandTests.define {
    command = ["snapshotReference"]
    signature = """
Short Description: Capture the current state of the reference database
Long Description: NOT SET
Required Args:
  referenceUrl (String) The JDBC reference database connection URL
Optional Args:
  driver (String) The JDBC driver class
    Default: null
  driverPropertiesFile (String) The JDBC driver properties file
    Default: null
  referenceDefaultCatalogName (String) The default catalog name to use for the database connection
    Default: null
  referenceDefaultSchemaName (String) The default schema name to use for the database connection
    Default: null
  referencePassword (String) Reference password to use to connect to the database
    Default: null
    OBFUSCATED
  referenceUsername (String) Reference username to use to connect to the database
    Default: null
  snapshotFormat (String) Output format to use (JSON or YAML
    Default: null
"""

    run "Happy path", {
        arguments = [
                referenceUrl     : { it.url },
                referenceUsername: { it.username },
                referencePassword: { it.password }
        ]
        setup {
            cleanResources("changeset-test.xml")
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
        arguments = [
                referenceUrl     : { it.url },
                referenceUsername: { it.username },
                referencePassword: { it.password }
        ]
        setup {
            cleanResources("target/test-classes/snapshotReference.txt")
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

        outputFile = new File("target/test-classes/snapshotReference.txt")

        expectedFileContent = [
                //
                // Find the " -- Release Database Lock" line
                //
                "target/test-classes/snapshotReference.txt" : [CommandTests.assertContains("Database snapshot for")]
        ]

        expectedResults = [
                statusCode   : 0
        ]
    }

    run "Run without any arguments should throw an exception",  {
        arguments = [
                referenceUrl:   ""
        ]
        expectedException = CommandValidationException.class
    }
}
