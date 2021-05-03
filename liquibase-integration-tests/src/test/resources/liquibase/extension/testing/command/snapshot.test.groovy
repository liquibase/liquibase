package liquibase.extension.testing.command

import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.change.core.TagDatabaseChange

CommandTests.define {
    command = ["snapshot"]
    signature = """
Short Description: Capture the current state of the database
Long Description: Capture the current state of the database
Required Args:
  url (String) The JDBC database connection URL
Optional Args:
  changeLogFile (String) The root changelog
    Default: null
  password (String) Password to use to connect to the database
    Default: null
  snapshotFormat (String) Output format to use (JSON or YAML
    Default: null
  username (String) Username to use to connect to the database
    Default: null
"""

    run {
        arguments = [
            changeLogFile: "target/test-classes/changeLog-test.xml"
        ]
        setup {
            cleanResources("changeLog-test.xml")
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
                statusMessage: "Successfully executed snapshot",
                statusCode   : 0
        ]
    }
}
