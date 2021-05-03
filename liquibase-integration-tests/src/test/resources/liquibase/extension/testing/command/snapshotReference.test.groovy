package liquibase.extension.testing.command

import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.change.core.TagDatabaseChange

CommandTests.define {
    command = ["snapshotReference"]
    signature = """
Short Description: Capture the current state of the reference database
Long Description: Capture the current state of the reference database
Required Args:
  referenceUrl (String) The JDBC reference database connection URL
Optional Args:
  changeLogFile (String) The root changelog
    Default: null
  referencePassword (String) Reference password to use to connect to the database
    Default: null
  referenceUsername (String) Reference username to use to connect to the database
    Default: null
  snapshotFormat (String) Output format to use (JSON or YAML
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
                statusMessage: "Successfully executed snapshotReference",
                statusCode   : 0
        ]
    }
}
