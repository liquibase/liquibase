package liquibase.integrationtest.command

import liquibase.change.ColumnConfig
import liquibase.change.core.CreateTableChange
import liquibase.change.core.TagDatabaseChange

CommandTest.define {
    command = ["generateChangeLog"]
    signature = """
Short Description: MISSING
Long Description: MISSING
Required Args:
  url (String) MISSING DESCRIPTION
Optional Args:
  changeLogFile (String) MISSING DESCRIPTION
    Default: null
  password (String) MISSING DESCRIPTION
    Default: null
  username (String) MISSING DESCRIPTION
    Default: null
"""

    run {
        arguments = [
            changeLogFile: "target/test-classes/changeLog-test.xml"
        ]
        setup {
            cleanTempResource("changeLog-test.xml")
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
                statusMessage: "Successfully executed generateChangeLog",
                statusCode   : 0
        ]
    }
}